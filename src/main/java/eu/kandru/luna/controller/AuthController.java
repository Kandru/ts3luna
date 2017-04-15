package eu.kandru.luna.controller;

import eu.kandru.luna.model.json.AuthChallengeRequest;
import eu.kandru.luna.model.json.AuthChallengeResponse;
import eu.kandru.luna.security.AuthChallenge;
import eu.kandru.luna.security.JwtProperties;
import eu.kandru.luna.security.JwtService;
import lombok.extern.slf4j.Slf4j;
import org.jose4j.lang.JoseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.security.SecureRandom;

/**
 * Created by jko on 15.04.2017.
 */
@RestController
@Slf4j
public class AuthController {

    private static final int PASSWORD_LENGTH = 6;
    @Autowired
    private JwtService jwtService;
    @Autowired
    private JwtProperties jwtProps;

    private SecureRandom random = new SecureRandom();

    @RequestMapping(value = "/auth/challenge", method = RequestMethod.POST)
    public AuthChallengeResponse challenge(@RequestBody AuthChallengeRequest request) throws JoseException {
        String jwt = AuthChallenge.builder()
                                  .clientDbId(request.getClientDbId())
                                  .identification(generatePassword())
                                  .build()
                                  .toJwt(jwtService);
        return AuthChallengeResponse.builder()
                                    .challenge(jwt)
                                    .expires((long) jwtProps.getExpiration().floatValue())
                                    .build();
    }

    private String generatePassword() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < PASSWORD_LENGTH; i++) {
            sb.append(random.nextInt(10));
        }
        return sb.toString();
    }
}
