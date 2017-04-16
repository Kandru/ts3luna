package eu.kandru.luna.controller;

import eu.kandru.luna.model.json.AuthChallengeRequest;
import eu.kandru.luna.model.json.AuthChallengeResponse;
import eu.kandru.luna.model.json.AuthenticateRequest;
import eu.kandru.luna.model.json.AuthenticateResponse;
import eu.kandru.luna.security.AuthTicket;
import eu.kandru.luna.security.JwtIdentity;
import eu.kandru.luna.security.JwtProperties;
import eu.kandru.luna.security.JwtService;
import eu.kandru.luna.util.OneTimePasswordGenerator;
import lombok.extern.slf4j.Slf4j;
import org.jose4j.jwt.MalformedClaimException;
import org.jose4j.jwt.NumericDate;
import org.jose4j.jwt.consumer.InvalidJwtException;
import org.jose4j.lang.JoseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;

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


    /**
     * Constructor
     */
    @Autowired
    public AuthController(JwtService jwtService, JwtProperties jwtProps, OneTimePasswordGenerator passwordGenerator) {
        this.jwtService = jwtService;
        this.jwtProps = jwtProps;
        this.passwordGenerator = passwordGenerator;
    }

    /**
     * Starts the authentication process by creating a ticket for the client and providing an identification password.
     */
    @RequestMapping(value = "/auth/challenge", method = RequestMethod.POST)
    public AuthChallengeResponse challenge(@RequestBody AuthChallengeRequest request) throws JoseException {
        String password = passwordGenerator.generatePassword();
        String jwt = AuthTicket.builder()
                               .clientDbId(request.getClientDbId())
                               .identification(password)
                               .expiration(NumericDate.fromMilliseconds(System.currentTimeMillis() + CHALLENGE_TIMEOUT * 1000))
                               .build()
                               .toJwt(jwtService);
        log.info("generated password " + password);
        return AuthChallengeResponse.builder().challenge(jwt).expires(CHALLENGE_TIMEOUT / 60).build();
    }

    /**
     * Finishes the authentication process by creating the actual jwt the client can use to identify himself.
     */
    @RequestMapping(value = "auth/authenticate", method = RequestMethod.POST)
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


}
