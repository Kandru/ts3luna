package eu.kandru.luna.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.Collections;

@Configuration
@Profile("dev")
@EnableSwagger2
public class SwaggerConfig {

    @Bean
    public Docket swaggerApi() {
        return new Docket(DocumentationType.SWAGGER_2).
                select()
                .paths(PathSelectors.any())
                .apis(RequestHandlerSelectors.basePackage("eu.kandru.luna"))
                .build()
                .apiInfo(apiInfo());
    }

    private ApiInfo apiInfo() {
        return new ApiInfo(
                "TS3 Luna API",
                "Internal API for TS3Luna backend.",
                "1.0",
                "",
                new Contact("Jannik Kolodziej", "https://github.com/Kandru/ts3luna", "ts3luna@jkolodziej.de"),
                "Apache 2.0 License",
                "https://www.apache.org/licenses/LICENSE-2.0",
                Collections.emptyList());
    }
}
