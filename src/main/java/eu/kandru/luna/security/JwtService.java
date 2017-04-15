package eu.kandru.luna.security;

import org.jose4j.jwa.AlgorithmConstraints;
import org.jose4j.jwe.ContentEncryptionAlgorithmIdentifiers;
import org.jose4j.jwe.JsonWebEncryption;
import org.jose4j.jwe.KeyManagementAlgorithmIdentifiers;
import org.jose4j.jwt.JwtClaims;
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
 * Created by jko on 12.04.2017.
 */
@Component
public class JwtService {

    private static Key privateKey = new AesKey(ByteUtil.randomBytes(16));

    @Autowired
    JwtProperties properties;

    private JwtClaims updateClaims(JwtClaims claims) {
        claims.setIssuer(properties.getIssuer());
        claims.setExpirationTimeMinutesInTheFuture(properties.getExpiration());
        claims.setGeneratedJwtId();
        claims.setIssuedAtToNow();
        claims.setNotBeforeMinutesInThePast(properties.getNotBefore());
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

    public String generateJwt(JwtClaims claims) throws JoseException {
        updateClaims(claims);
        return encryptJwt(claims);
    }

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
