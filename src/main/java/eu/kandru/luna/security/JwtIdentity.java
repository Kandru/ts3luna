package eu.kandru.luna.security;

import lombok.Builder;
import lombok.Value;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.MalformedClaimException;
import org.jose4j.jwt.consumer.InvalidJwtException;
import org.jose4j.lang.JoseException;

import java.security.Principal;
import java.util.List;

/**
 * Representation of the JWT returned by {@link eu.kandru.luna.model.json.AuthenticateResponse}.
 * This class is supposed to be used as verified {@link Principal} to identify the client throughout the app.
 * Also keep in mind that the JWT representation of this class is transmitted in every request, so keep this class
 * as small as possible!
 *
 * @author jko
 */
@Value
@Builder
public class JwtIdentity implements Principal {
    private static final String CLAIM_UNIQUEID = "uid";
    private static final String CLAIM_ROLES = "rol";
    // NOTE: Right now this is a stub. Add content as needed.
    private Integer clientDbId;
    private String uniqueId;
    private List<String> roles;


    /**
     * Parse the given JWT to construct an instance of this class.
     *
     * @param jwt        jwt to parse
     * @param jwtService jwtService (bean)
     * @return parsed instance
     * @throws InvalidJwtException     The jwt can not be trusted. This can mean it was tempered with.
     * @throws MalformedClaimException Looks like we screwed up creating the token.
     */
    public static JwtIdentity fromJwt(String jwt, JwtService jwtService) throws
            InvalidJwtException,
            MalformedClaimException {
        JwtClaims claims = jwtService.parseJwt(jwt);
        return JwtIdentity.builder()
                .clientDbId(Integer.parseInt(claims.getSubject()))
                .uniqueId(claims.getStringClaimValue(CLAIM_UNIQUEID))
                .roles(claims.getStringListClaimValue(CLAIM_ROLES))
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
        claims.setSubject(clientDbId.toString());
        claims.setStringClaim(CLAIM_UNIQUEID, uniqueId);
        claims.setStringListClaim(CLAIM_ROLES, roles);
        return jwtService.generateJwt(claims);
    }

    @Override
    public String getName() {
        return "emptyName";
    }
}
