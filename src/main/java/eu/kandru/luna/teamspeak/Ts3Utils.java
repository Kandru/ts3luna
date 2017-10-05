package eu.kandru.luna.teamspeak;

import com.github.theholywaffle.teamspeak3.TS3ApiAsync;
import com.github.theholywaffle.teamspeak3.api.CommandFuture;
import com.github.theholywaffle.teamspeak3.api.wrapper.ClientInfo;
import com.github.theholywaffle.teamspeak3.api.wrapper.DatabaseClientInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Component
public class Ts3Utils {
    private final TS3ApiAsync ts3api;
    private final TS3Properties properties;

    /**
     * Constructor
     *
     * @param ts3Manager {@link TS3Manager} that provides the {@link TS3ApiAsync} for this class.
     * @param properties Configuration.
     */
    @Autowired
    public Ts3Utils(TS3Manager ts3Manager, TS3Properties properties) {
        this.ts3api = ts3Manager.getTs3ApiAsync();
        this.properties = properties;
    }

    public TS3Properties getProperties() {
        return properties;
    }

    public CommandFuture<DatabaseClientInfo> getForDatabaseId(int clDbId) {
        return ts3api.getDatabaseClientInfo(clDbId);
    }

    public CommandFuture<ClientInfo> getForUniqueId(String uniqueId) {
        return ts3api.getClientByUId(uniqueId);
    }

    public <V> V timedGet(CommandFuture<V> command) throws TimeoutException {
        return command.getUninterruptibly(properties.getRequestTimeout(), TimeUnit.MILLISECONDS);
    }
}
