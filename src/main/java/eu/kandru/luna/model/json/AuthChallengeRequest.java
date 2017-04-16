package eu.kandru.luna.model.json;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request to start an authentication.
 *
 * @author jko
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthChallengeRequest {
    /**
     * The ID that identifies the client in the database.
     */
    private Long clientDbId;
}
