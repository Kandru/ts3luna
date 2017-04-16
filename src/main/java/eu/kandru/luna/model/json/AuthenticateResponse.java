package eu.kandru.luna.model.json;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * The response given to a {@link AuthenticateRequest}.
 *
 * @author jko
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthenticateResponse {
    /**
     * Indicates whether the authentication was successful.
     */
    private Boolean success;

    /**
     * This token can be used to identify the client.
     */
    private String authToken;

    /**
     * Time in minutes how long the given authToken is valid. The client is expected to
     * acquire a new token using a new {@link AuthChallengeRequest} when this one runs out.
     */
    private Integer expires; // minutes
}
