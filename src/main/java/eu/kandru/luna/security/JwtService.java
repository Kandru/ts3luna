package eu.kandru.luna.security;

import lombok.extern.slf4j.Slf4j;
import org.jose4j.jwa.AlgorithmConstraints;
import org.jose4j.jwe.ContentEncryptionAlgorithmIdentifiers;
import org.jose4j.jwe.JsonWebEncryption;
import org.jose4j.jwe.KeyManagementAlgorithmIdentifiers;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.MalformedClaimException;
import org.jose4j.jwt.consumer.InvalidJwtException;
import org.jose4j.jwt.consumer.JwtConsumer;
import org.jose4j.jwt.consumer.JwtConsumerBuilder;
import org.jose4j.keys.AesKey;
import org.jose4j.lang.ByteUtil;
import org.jose4j.lang.JoseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.security.Key;

/**
 * Deals with (de)serialization of JWTs
 *
 * @author jko
 */
@Component
@Slf4j
public class JwtService {

    private static Key privateKey = new AesKey(ByteUtil.randomBytes(16));

    JwtProperties properties;

    /**
     * Constructor
     *
     * @param properties bean
     */
    @Autowired
    public JwtService(JwtProperties properties) {
        this.properties = properties;
    }

    private JwtClaims updateClaims(JwtClaims claims) {
        try {
            if (claims.getIssuer() == null)
                claims.setIssuer(properties.getIssuer());
            if (claims.getExpirationTime() == null)
                claims.setExpirationTimeMinutesInTheFuture(properties.getExpiration());
            if (claims.getJwtId() == null)
                claims.setGeneratedJwtId();
            if (claims.getIssuedAt() == null)
                claims.setIssuedAtToNow();
            if (claims.getNotBefore() == null)
                claims.setNotBeforeMinutesInThePast(properties.getNotBefore());
        } catch (MalformedClaimException e) {
            log.error("Our own JwtClaims were malformed. Something went really bad here.", e);
            throw new RuntimeException(e);
        }
        return claims;
    }

    private String encryptJwt(JwtClaims claims) throws JoseException {
        JsonWebEncryption encryption = new JsonWebEncryption();
        encryption.setPayload(claims.toJson());
        encryption.setAlgorithmHeaderValue(KeyManagementAlgorithmIdentifiers.A128KW);
        encryption.setEncryptionMethodHeaderParameter(ContentEncryptionAlgorithmIdentifiers.AES_128_CBC_HMAC_SHA_256);
        encryption.setKey(privateKey);
        return encryption.getCompactSerialization();
    }

    /**
     * Generates a JWT out of the given claims. Missing claims are added but never overwritten.
     *
     * @param claims to be included in the jwt
     * @return the encrypted jwt
     * @throws JoseException if crypto went wrong
     */
    public String generateJwt(JwtClaims claims) throws JoseException {
        updateClaims(claims);
        return encryptJwt(claims);
    }

    /**
     * Parses a {@link JwtClaims} out of the given JWT. If this method does not throw an {@link InvalidJwtException}
     * the JWT was validated, verified and can be trusted.
     * @param jwt to parse
     * @return parsed claims
     * @throws InvalidJwtException invalid jwt
     */
    public JwtClaims parseJwt(String jwt) throws InvalidJwtException {
        JwtConsumer consumer = new JwtConsumerBuilder().setRequireExpirationTime()
                                                       .setEnableRequireEncryption()
                                                       .setRequireNotBefore()
                                                       .setAllowedClockSkewInSeconds(properties.getClockSkew())
                                                       .setRequireSubject()
                                                       .setExpectedIssuer(properties.getIssuer())
                                                       .setDecryptionKey(privateKey)
                                                       .setJweAlgorithmConstraints(new AlgorithmConstraints(
                                                               AlgorithmConstraints.ConstraintType.WHITELIST,
                                                               KeyManagementAlgorithmIdentifiers.A128KW))
                                                       .setJweContentEncryptionAlgorithmConstraints(new AlgorithmConstraints(
                                                               AlgorithmConstraints.ConstraintType.WHITELIST,
                                                               ContentEncryptionAlgorithmIdentifiers.AES_128_CBC_HMAC_SHA_256))
                                                       .setDisableRequireSignature()
                                                       .build();
        return consumer.processToClaims(jwt);
    }

}
