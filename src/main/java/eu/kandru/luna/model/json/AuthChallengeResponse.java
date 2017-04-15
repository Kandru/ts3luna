package eu.kandru.luna.model.json;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by jko on 12.04.2017.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthChallengeResponse {
    private String challenge;
    private Long expires;
}
