package eu.kandru.luna.security;

import org.springframework.security.authentication.AbstractAuthenticationToken;

/**
 * {@link org.springframework.security.core.Authentication}-Wrapper around {@link JwtIdentity}.
 *
 * @author jko
 */
public class JwtAuthentication extends AbstractAuthenticationToken {

    /**
     * The authorized client
     */
    private JwtIdentity principal;

    /**
     * The jwt that was used to create this authentication.
     */
    private String jwtToken;

    /**
     * Constructor
     */
    public JwtAuthentication(JwtIdentity principal, String jwtToken) {
        super(null);
        this.principal = principal;
        this.jwtToken = jwtToken;
    }

    /**
     * Other constructor
     */
    public JwtAuthentication() {
        super(null);
    }

    @Override
    public Object getCredentials() {
        return jwtToken;
    }

    @Override
    public Object getPrincipal() {
        return principal;
    }
}
