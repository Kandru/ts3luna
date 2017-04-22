package eu.kandru.luna.model.json;

import lombok.Builder;
import lombok.Data;

/**
 * Simple POJO just so that the /auth/check can respons something.
 */
@Data
@Builder
public class AuthCheckResponse {
    private boolean success;
}
