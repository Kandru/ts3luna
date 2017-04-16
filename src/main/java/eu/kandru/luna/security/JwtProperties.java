package eu.kandru.luna.security;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * jwt configuration from application.yml
 *
 * @author jko
 */
@Component
@ConfigurationProperties("jwt")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class JwtProperties {
    private Integer expiration; // minutes
    private Integer notBefore; // minutes
    private Integer clockSkew; // seconds
    private String issuer;
}
