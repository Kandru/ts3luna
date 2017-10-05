package eu.kandru.luna.controller;

import com.github.theholywaffle.teamspeak3.api.wrapper.Client;
import com.github.theholywaffle.teamspeak3.api.wrapper.ClientInfo;
import com.github.theholywaffle.teamspeak3.api.wrapper.DatabaseClientInfo;
import eu.kandru.luna.model.json.*;
import eu.kandru.luna.security.AuthTicket;
import eu.kandru.luna.security.JwtIdentity;
import eu.kandru.luna.security.JwtProperties;
import eu.kandru.luna.security.JwtService;
import eu.kandru.luna.teamspeak.Ts3Utils;
import eu.kandru.luna.teamspeak.modules.login.TS3LoginModule;
import eu.kandru.luna.util.OneTimePasswordGenerator;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.jose4j.jwt.MalformedClaimException;
import org.jose4j.jwt.NumericDate;
import org.jose4j.jwt.consumer.InvalidJwtException;
import org.jose4j.lang.JoseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;

/**
 * Handles the authentication of individual clients.
 * A client is expected to first POST to /auth/challenge and then confirm his identity
 * using the given token and the externally provided password.
 *
 * @author jko
 */
@RestController
@Slf4j
public class AuthController {

    private static final int CHALLENGE_TIMEOUT = 300; //seconds

    private JwtService jwtService;
    private JwtProperties jwtProps;
    private OneTimePasswordGenerator passwordGenerator;
    private TS3LoginModule ts3LoginModule;
    private Ts3Utils ts3Utils;


    /**
     * Constructor
     */
    @Autowired
    public AuthController(JwtService jwtService, JwtProperties jwtProps, OneTimePasswordGenerator passwordGenerator, TS3LoginModule ts3LoginModule, Ts3Utils ts3Utils) {
        this.jwtService = jwtService;
        this.jwtProps = jwtProps;
        this.passwordGenerator = passwordGenerator;
        this.ts3LoginModule = ts3LoginModule;
        this.ts3Utils = ts3Utils;
    }

    /**
     * Gets a list with all users from a specific ip
     */
    @ApiOperation(value = "Gets a list with all users from a specific ip",
            notes = "Response is a JSON mapping ClientID to client nickname.")
    @GetMapping(value = "/auth/userlist", produces = "application/json")
    public Map<Integer, String> userlist(HttpServletRequest request, HttpServletResponse response) {
        String clientIP = request.getRemoteAddr();
        Map<Integer, String> clientMap = new HashMap<>();
        try {
            List<Client> clients = ts3LoginModule.getClientsByIp(clientIP);
            for (Client client : clients) {
                clientMap.put(client.getDatabaseId(), client.getNickname());
            }
        } catch (TimeoutException e) {
            log.warn("Receiving client list from ip timed out.");
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            //TODO: send some better response
        }
        return clientMap;
    }

    /**
     * Starts the authentication process by creating a ticket for the client and providing an identification password.
     */
    @ApiOperation(value = "Starts the atuhentication process",
            notes = "The given clientId should be acquired via /auth/userlist")
    @PostMapping(value = "/auth/challenge", produces = "application/json")
    public AuthChallengeResponse challenge(@RequestBody AuthChallengeRequest request) throws JoseException {
        String password = passwordGenerator.generatePassword();
        String jwt = AuthTicket.builder()
                .clientDbId(request.getClientDbId())
                .identification(password)
                .expiration(NumericDate.fromMilliseconds(System.currentTimeMillis() + CHALLENGE_TIMEOUT * 1000))
                .build()
                .toJwt(jwtService);
        log.info("generated password " + password);
        ts3LoginModule.sendPasswordToUser(request.getClientDbId(), password);
        return AuthChallengeResponse.builder().challenge(jwt).expires(CHALLENGE_TIMEOUT / 60).build();
    }

    /**
     * Finishes the authentication process by creating the actual jwt the client can use to identify himself.
     */
    @ApiOperation(value = "Finishes the authentication process", notes = "result.expire is minutes from now")
    @PostMapping(value = "/auth/authenticate", produces = "application/json")
    public AuthenticateResponse authenticate(@RequestBody AuthenticateRequest request,
                                             HttpServletResponse response) throws
            MalformedClaimException,
            InvalidJwtException,
            JoseException,
            TimeoutException {
        AuthTicket ticket = AuthTicket.fromJwt(request.getChallenge(), jwtService);

        if (!ticket.getIdentification().equals(request.getIdentification())) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return AuthenticateResponse.builder().success(false).build();
        }

        DatabaseClientInfo clientDBInfo = ts3Utils.timedGet(ts3Utils.getForDatabaseId(ticket.getClientDbId()));
        ClientInfo clientInfo = ts3Utils.timedGet(ts3Utils.getForUniqueId(clientDBInfo.getUniqueIdentifier()));
        String uniqueId = clientDBInfo.getUniqueIdentifier();
        JwtIdentity identity = JwtIdentity.builder()
                .clientDbId(ticket.getClientDbId())
                .uniqueId(uniqueId)
                .roles(new ArrayList<>(ts3LoginModule.getRoles(clientInfo)))
                .build();
        return AuthenticateResponse.builder()
                .success(true)
                .authToken(identity.toJwt(jwtService))
                .expires(jwtProps.getExpiration())
                .build();
    }

    /**
     * Allows a client to check his current jwt for validity.
     */
    @ApiOperation(value = "Allows a client to check his current jwt for validity")
    @GetMapping(value = "/auth/check", produces = "application/json")
    public AuthCheckResponse checkAuth(@RequestParam String jwt, HttpServletResponse response) {
        try {
            JwtIdentity.fromJwt(jwt, jwtService);
        } catch (InvalidJwtException | MalformedClaimException e) {
            log.info("checkAuth was provided with an invalid jwt", e);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return AuthCheckResponse.builder().success(false).build();
        }
        response.setStatus(HttpServletResponse.SC_OK);
        return AuthCheckResponse.builder().success(true).build();
    }


}
