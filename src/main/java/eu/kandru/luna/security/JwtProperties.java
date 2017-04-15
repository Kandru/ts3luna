package eu.kandru.luna.security;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Created by jko on 12.04.2017.
 */
@Component
@ConfigurationProperties("jwt")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class JwtProperties {
    private Float expiration;
    private Float notBefore;
    private Integer clockSkew;
    private String issuer;
}
