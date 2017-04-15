package eu.kandru.luna;

import eu.kandru.luna.security.JwtProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({JwtProperties.class})
public class Ts3lunaApplication {

    public static void main(String[] args) {
        SpringApplication.run(Ts3lunaApplication.class, args);
    }
}
