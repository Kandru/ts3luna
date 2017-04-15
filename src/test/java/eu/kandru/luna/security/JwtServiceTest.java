package eu.kandru.luna.security;

import org.assertj.core.api.AutoCloseableSoftAssertions;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.MalformedClaimException;
import org.jose4j.jwt.NumericDate;
import org.jose4j.jwt.consumer.InvalidJwtException;
import org.jose4j.lang.IntegrityException;
import org.jose4j.lang.JoseException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.autoconfigure.liquibase.LiquibaseAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Created by jko on 14.04.2017.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {JwtProperties.class, JwtService.class}, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@EnableAutoConfiguration(exclude = {DataSourceAutoConfiguration.class, DataSourceTransactionManagerAutoConfiguration.class, HibernateJpaAutoConfiguration.class, LiquibaseAutoConfiguration.class})
public class JwtServiceTest {

    @Autowired
    private JwtService systemUnderTest;

    @Test
    public void generateJwt() throws Exception {
        String jwt = createJwt();
    }

    @Test
    public void parseJwt() throws Exception {
        String jwt = createJwt();

        JwtClaims result = verifyJwt(jwt);
    }

    private String createJwt() throws JoseException {
        JwtClaims claims = new JwtClaims();
        claims.setSubject("test");
        String jwt = systemUnderTest.generateJwt(claims);
        assertThat(jwt).isNotEmpty();
        assertThat(jwt).as("this JWE should be a concatenation of 4 strings joined by a dot")
                       .matches(".+\\..+\\..+\\..+");
        return jwt;
    }

    private JwtClaims verifyJwt(String jwt) throws InvalidJwtException, MalformedClaimException {
        try (AutoCloseableSoftAssertions softly = new AutoCloseableSoftAssertions()) {
            JwtClaims result = systemUnderTest.parseJwt(jwt);
            softly.assertThat(result.getIssuer()).isEqualTo("ts3luna");
            softly.assertThat(result.getExpirationTime()).isNotNull();
            softly.assertThat(result.getExpirationTime().isOnOrAfter(NumericDate.now()))
                  .as("NotBefore should be after current time")
                  .isNotNull();

            softly.assertThat(result.getSubject()).isEqualTo("test");
            softly.assertThat(result.getJwtId()).isNotNull();
            softly.assertThat(result.getNotBefore()).isNotNull();
            softly.assertThat(result.getNotBefore().isBefore(NumericDate.now()))
                  .as("NotBefore should be before current time")
                  .isNotNull();
            return result;
        }
    }

    @Test
    public void manipulateJwt() throws Exception {
        String jwt = createJwt();

        int endContent = jwt.lastIndexOf('.');
        int startContent = jwt.lastIndexOf('.', endContent);
        int changedChar = startContent + (endContent - startContent) / 2;
        char prevChar = jwt.charAt(changedChar);
        String manipulatedJwt = jwt.substring(0, changedChar - 1) + (prevChar + 1) + jwt.substring(changedChar);

        assertThatThrownBy(() -> verifyJwt(manipulatedJwt)).as("manipulated jwt should not be valid")
                                                           .isInstanceOf(InvalidJwtException.class)
                                                           .hasCauseInstanceOf(IntegrityException.class)
                                                           .hasMessageContaining("Authentication tag check failed");
    }

    @Test
    public void expiredJwt() throws Exception {
        systemUnderTest.properties.setExpiration(0.05f);
        systemUnderTest.properties.setClockSkew(0);
        String jwt = createJwt();
        Thread.sleep(2500);
        assertThatThrownBy(() -> verifyJwt(jwt)).as("expired jwt should not be valid")
                                                .isInstanceOf(InvalidJwtException.class)
                                                .hasNoCause()
                                                .hasMessageContaining("rejected")
                                                .hasMessageContaining("is on or after the Expiration Time");
    }


}