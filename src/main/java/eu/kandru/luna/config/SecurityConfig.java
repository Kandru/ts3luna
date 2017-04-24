package eu.kandru.luna.config;

import eu.kandru.luna.security.JwtAuthFilter;
import eu.kandru.luna.security.JwtAuthProvider;
import eu.kandru.luna.security.JwtUnauthorizedEntryPoint;
import eu.kandru.luna.util.OneTimePasswordGenerator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.security.SecureRandom;
import java.util.Arrays;

/**
 * Configures security/authorization and provides the required beans.
 * <p>
 * Right now there is no actual access control. As soon as you are authenticated you can basically do everything.
 *
 * @author jko
 */
@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
@Slf4j
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    private JwtUnauthorizedEntryPoint unauthorizedEntryPoint;

    @Bean
    public JwtAuthFilter getJwtAuthFilter() {
        return new JwtAuthFilter();
    }

    @Bean
    public JwtAuthProvider getJwtAuthProvider() {
        return new JwtAuthProvider();
    }

    @Bean
    public OneTimePasswordGenerator getPasswordGenerator() {
        return new OneTimePasswordGenerator() {
            private static final int PASSWORD_LENGTH = 6;
            private SecureRandom random = new SecureRandom();

            @Override
            public String generatePassword() {
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < PASSWORD_LENGTH; i++) {
                    sb.append(random.nextInt(10));
                }
                return sb.toString();
            }
        };
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(Arrays.asList("http://127.0.0.1:3000", "http://localhost:3000"));
        config.setAllowedHeaders(Arrays.asList("Content-Type"));
        config.setAllowedMethods(Arrays.asList("GET", "POST"));
        UrlBasedCorsConfigurationSource urlConfig = new UrlBasedCorsConfigurationSource();
        urlConfig.registerCorsConfiguration("/**", config);
        return urlConfig;
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.authenticationProvider(getJwtAuthProvider());
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.cors().and().csrf().disable()
            .exceptionHandling().authenticationEntryPoint(unauthorizedEntryPoint).and()
            .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).and()
            .authorizeRequests().antMatchers("/auth/**").permitAll()
            .antMatchers(HttpMethod.OPTIONS, "**").permitAll()
            .anyRequest().authenticated().and()
            .httpBasic().disable()
            .addFilterBefore(getJwtAuthFilter(), UsernamePasswordAuthenticationFilter.class)
            .headers().cacheControl();
    }
}
