package eu.kandru.luna.security;

import lombok.extern.slf4j.Slf4j;
import org.jose4j.jwt.MalformedClaimException;
import org.jose4j.jwt.consumer.InvalidJwtException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

/**
 * Authenticates JwtAuthentication instances.
 *
 * @author jko
 */
@Slf4j
public class JwtAuthProvider implements AuthenticationProvider {

    @Autowired
    private JwtService jwtService;


    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        JwtAuthentication auth = (JwtAuthentication) authentication;
        String jwt = auth.getCredentials().toString();
        try {
            JwtIdentity identity = JwtIdentity.fromJwt(jwt, jwtService);
            JwtAuthentication result = new JwtAuthentication(identity, jwt);
            result.setAuthenticated(true);
            return result;
        } catch (InvalidJwtException | MalformedClaimException e) {
            throw new BadCredentialsException("failed to verify jwt", e);
        }
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return JwtAuthentication.class.isAssignableFrom(authentication);
    }
}
