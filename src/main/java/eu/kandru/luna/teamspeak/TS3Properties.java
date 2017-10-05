package eu.kandru.luna.teamspeak;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@ConfigurationProperties("teamspeak")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TS3Properties {
    
    private String ip;
    private Integer port;
    private String login;
    private String password;
    private Integer serverId;
    private String nickname;
    private Integer requestTimeout;
    private List<String> admins;

}
