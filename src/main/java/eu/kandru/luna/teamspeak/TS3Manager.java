package eu.kandru.luna.teamspeak;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import com.github.theholywaffle.teamspeak3.TS3ApiAsync;
import com.github.theholywaffle.teamspeak3.TS3Config;
import com.github.theholywaffle.teamspeak3.TS3Query;
import com.github.theholywaffle.teamspeak3.api.CommandFuture;
import com.github.theholywaffle.teamspeak3.api.exception.TS3ConnectionFailedException;
import com.github.theholywaffle.teamspeak3.api.reconnect.ReconnectStrategy;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * Handles the ts3 connection. Provides the ts3 async api.
 * @author Jonsen
 *
 */
@Component
@Slf4j
public class TS3Manager {
    private TS3Config ts3config;
    private TS3Query ts3query;
    @Getter
    private TS3ApiAsync ts3api;
    private final int RECONNECTION_TIMEOUT = 10000;

    /**
     * Constructor
     * @param ts3props Configuration used to connect to the ts3 server.
     */
    @Autowired
    public TS3Manager(TS3Properties ts3props) {
        ts3config = new TS3Config();

        ts3config.setHost(ts3props.getIp()).setQueryPort(ts3props.getPort())
                .setReconnectStrategy(ReconnectStrategy.constantBackoff(RECONNECTION_TIMEOUT));

        ts3query = new TS3Query(ts3config);
        try {
            ts3query.connect();

            ts3api = ts3query.getAsyncApi();

            CommandFuture<Boolean> loginCommand = ts3api.login(ts3props.getLogin(), ts3props.getPassword());
            loginCommand.onSuccess(result -> {
                if(result){
                    log.info("Connected to ts3 server.");
                } else {
                    log.error("Couldn't connect to ts3 server.");
                }
            });
            loginCommand.onFailure(result -> {
                log.error("Failure with login command. Connected: {}",result);
            });
            
            ts3api.selectVirtualServerById(ts3props.getServerId());
            ts3api.setNickname(ts3props.getNickname());
            
            CommandFuture<Boolean> registerCommand = ts3api.registerAllEvents();
            registerCommand.onFailure(result -> {
                log.error("Register all events command failed. Events registered {}",result);
            });
            registerCommand.onSuccess(result -> {
                if(result){
                    log.info("Events registered.");
                } else {
                    log.error("Couldnt register ts3 events.");
                }
            });
            
        } catch (TS3ConnectionFailedException e) {
            log.error("Couldn't connect to ts3 server.", e);
            // TODO: reconnect
        }
    }
    
   @Bean
    public TS3ApiAsync getTs3ApiAsync(){
    	return ts3api;
    }

}
