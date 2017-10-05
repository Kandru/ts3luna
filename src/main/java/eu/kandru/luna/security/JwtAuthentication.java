package eu.kandru.luna.security;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.CredentialsContainer;
import org.springframework.security.core.authority.AuthorityUtils;


/**
 * {@link org.springframework.security.core.Authentication}-Wrapper around {@link JwtIdentity}.
 *
 * @author jko
 */
public class JwtAuthentication extends AbstractAuthenticationToken implements CredentialsContainer {

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
        super(AuthorityUtils.createAuthorityList(principal.getRoles().stream().map(role -> "ROLE_" + role).toArray(String[]::new)));
        this.principal = principal;
        this.jwtToken = jwtToken;
    }

    public JwtAuthentication(String jwtToken) {
        super(null);
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
