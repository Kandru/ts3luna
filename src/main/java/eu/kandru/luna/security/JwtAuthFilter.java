package eu.kandru.luna.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This filter will inspect every request done to protected resources. It then extracts the authorization header and
 * provides the jwt to {@link JwtAuthProvider} to authenticate. Finally the
 * {@link org.springframework.security.core.context.SecurityContext} is authenticated
 * or invalidated based on the result.
 *
 * @author jko
 */
@Slf4j
public class JwtAuthFilter extends OncePerRequestFilter {

    private static final String AUTH_HEADER = "Authorization";
    private static final Pattern EXTRACT_JWT = Pattern.compile("Bearer (.+)$");

    @Autowired
    private JwtAuthProvider jwtAuthProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String header = request.getHeader(AUTH_HEADER);
        if (header != null && !header.isEmpty()) {
            Matcher jwtMatcher = EXTRACT_JWT.matcher(header);
            if (jwtMatcher.matches()) {
                String jwt = jwtMatcher.group(1);
                try {
                    Authentication auth = new JwtAuthentication(null, jwt);
                    auth = jwtAuthProvider.authenticate(auth);
                    SecurityContextHolder.getContext().setAuthentication(auth);
                } catch (AuthenticationException e) {
                    SecurityContextHolder.clearContext();
                    log.warn("rejected invalid authentication", e);
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                    return;
                }
            } else
                log.warn("Invalid auth header found:" + header);
        }
        filterChain.doFilter(request, response);
    }
}
