package eu.kandru.luna.security;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.MalformedClaimException;
import org.jose4j.jwt.consumer.InvalidJwtException;
import org.jose4j.lang.JoseException;

/**
 * Created by jko on 15.04.2017.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthChallenge {

    private static final String ID_CLAIM_NAME = "pwd";
    private Long clientDbId;
    private String identification;

    public static AuthChallenge fromJwt(String jwt, JwtService jwtService) throws
            InvalidJwtException,
            MalformedClaimException {
        JwtClaims claims = jwtService.parseJwt(jwt);
        return AuthChallenge.builder()
                            .clientDbId(Long.parseLong(claims.getSubject()))
                            .identification(claims.getClaimValue(ID_CLAIM_NAME, String.class))
                            .build();
    }

    public String toJwt(JwtService jwtService) throws JoseException {
        JwtClaims claims = new JwtClaims();
        claims.setSubject(clientDbId.toString());
        claims.setStringClaim(ID_CLAIM_NAME, identification);
        return jwtService.generateJwt(claims);
    }
}
