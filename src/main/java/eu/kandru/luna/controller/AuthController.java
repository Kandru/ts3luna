package eu.kandru.luna.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jose4j.jwt.MalformedClaimException;
import org.jose4j.jwt.NumericDate;
import org.jose4j.jwt.consumer.InvalidJwtException;
import org.jose4j.lang.JoseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.github.theholywaffle.teamspeak3.api.wrapper.Client;

import eu.kandru.luna.model.json.AuthChallengeRequest;
import eu.kandru.luna.model.json.AuthChallengeResponse;
import eu.kandru.luna.model.json.AuthCheckResponse;
import eu.kandru.luna.model.json.AuthenticateRequest;
import eu.kandru.luna.model.json.AuthenticateResponse;
import eu.kandru.luna.security.AuthTicket;
import eu.kandru.luna.security.JwtIdentity;
import eu.kandru.luna.security.JwtProperties;
import eu.kandru.luna.security.JwtService;
import eu.kandru.luna.teamspeak.modules.login.TS3LoginModule;
import eu.kandru.luna.util.OneTimePasswordGenerator;
import lombok.extern.slf4j.Slf4j;

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
    


    /**
     * Constructor
     */
    @Autowired
    public AuthController(JwtService jwtService, JwtProperties jwtProps, OneTimePasswordGenerator passwordGenerator, TS3LoginModule ts3LoginModule) {
        this.jwtService = jwtService;
        this.jwtProps = jwtProps;
        this.passwordGenerator = passwordGenerator;
        this.ts3LoginModule = ts3LoginModule;
    }
    
    /**
     * Gets a list with all users from a specific ip
     */
    @GetMapping("/auth/userlist")
    public Map<Integer, String> userlist(HttpServletRequest request, HttpServletResponse response) {
        String clientIP = request.getRemoteAddr();
        Map<Integer, String> clientMap = new HashMap<>();
        try {
            List<Client> clients = ts3LoginModule.getClientsByIp(clientIP);
            for (Client client : clients){
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
    @PostMapping("/auth/challenge")
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
    @PostMapping("/auth/authenticate")
    public AuthenticateResponse authenticate(@RequestBody AuthenticateRequest request,
                                             HttpServletResponse response) throws
            MalformedClaimException,
            InvalidJwtException,
            JoseException {
        AuthTicket ticket = AuthTicket.fromJwt(request.getChallenge(), jwtService);

        if (!ticket.getIdentification().equals(request.getIdentification())) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return AuthenticateResponse.builder().success(false).build();
        }

        JwtIdentity identity = JwtIdentity.builder().clientDbId(ticket.getClientDbId()).build();
        return AuthenticateResponse.builder()
                                   .success(true)
                                   .authToken(identity.toJwt(jwtService))
                                   .expires(jwtProps.getExpiration())
                                   .build();
    }

    /**
     * Allows a client to check his current jwt for validity.
     */
    @GetMapping("/auth/check")
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
