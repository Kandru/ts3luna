package eu.kandru.luna.security;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Value;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.MalformedClaimException;
import org.jose4j.jwt.NumericDate;
import org.jose4j.jwt.consumer.InvalidJwtException;
import org.jose4j.lang.JoseException;

/**
 * Representation of the JWT in {@link eu.kandru.luna.model.json.AuthChallengeResponse}.
 *
 * @author jko
 */
@Value
@Builder
@Getter
@AllArgsConstructor
public class AuthTicket {

    private static final String IDENTIFICATION_CLAIM_NAME = "pwd";

    /**
     * DatabaseID given by the client.
     */
    private Integer clientDbId;

    /**
     * Password that we expect the client to give.
     */
    private String identification;

    /**
     * Time when this ticket expires.
     */
    private NumericDate expiration;

    /**
     * Parse the given JWT to construct an instance of this class.
     *
     * @param jwt        jwt to parse
     * @param jwtService jwtService (bean)
     * @return parsed instance
     * @throws InvalidJwtException     The jwt can not be trusted. This can mean it was tempered with.
     * @throws MalformedClaimException Looks like we screwed up creating the token.
     */
    public static AuthTicket fromJwt(String jwt, JwtService jwtService) throws
            InvalidJwtException,
            MalformedClaimException {
        JwtClaims claims = jwtService.parseJwt(jwt);
        return AuthTicket.builder()
                         .clientDbId(Integer.parseInt(claims.getSubject()))
                         .identification(claims.getClaimValue(IDENTIFICATION_CLAIM_NAME, String.class))
                         .expiration(claims.getExpirationTime())
                         .build();
    }

    /**
     * Serializes this instance to an encrypted jwt.
     *
     * @param jwtService jwtService (bean)
     * @return the serialized jwt. This can can be given to the client via a secured medium.
     * @throws JoseException Exception during jwt creation.
     */
    public String toJwt(JwtService jwtService) throws JoseException {
        JwtClaims claims = new JwtClaims();
        claims.setExpirationTime(expiration);
        claims.setSubject(clientDbId.toString());
        claims.setStringClaim(IDENTIFICATION_CLAIM_NAME, identification);
        return jwtService.generateJwt(claims);
    }
}
