package eu.kandru.luna.model.json;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response to {@link AuthChallengeRequest}
 *
 * @author jko
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthChallengeResponse {
    /**
     * Temporary identifier which needs to be used in the next authentication step only.
     */
    private String challenge;
    /**
     * The time in minutes until the challenge expires.
     */
    private Integer expires; // minutes
}
