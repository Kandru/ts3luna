package eu.kandru.luna.model.json;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request to finish an authentication
 *
 * @author jko
 */
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class AuthenticateRequest {
    /**
     * The challenge returned by {@link AuthChallengeResponse}.
     */
    private String challenge;

    /**
     * The password provided by an external medium.
     */
    private String identification;
}
