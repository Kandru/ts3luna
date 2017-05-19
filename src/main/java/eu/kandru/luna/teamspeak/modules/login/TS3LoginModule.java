package eu.kandru.luna.teamspeak.modules.login;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.github.theholywaffle.teamspeak3.TS3ApiAsync;
import com.github.theholywaffle.teamspeak3.api.CommandFuture;
import com.github.theholywaffle.teamspeak3.api.wrapper.Client;
import com.github.theholywaffle.teamspeak3.api.wrapper.ClientInfo;
import com.github.theholywaffle.teamspeak3.api.wrapper.DatabaseClientInfo;

import eu.kandru.luna.i18n.MessageBuilder;
import eu.kandru.luna.teamspeak.TS3Manager;
import eu.kandru.luna.teamspeak.TS3Properties;
import lombok.extern.slf4j.Slf4j;

/**
 * Handles authentication with ts3.
 * Used to send a password to the user and get a list of users by ip.
 * @author Jonsen
 *
 */
@Slf4j
@Component
public class TS3LoginModule {
    private TS3ApiAsync ts3api;
    private TS3Properties properties;

    /**
     * Constructor
     * @param ts3Manager {@link TS3Manager} that provides the {@link TS3ApiAsync} for this class.
     * @param properties Configuration.
     */
    @Autowired
    public TS3LoginModule(TS3Manager ts3Manager, TS3Properties properties) {
        this.ts3api = ts3Manager.getTs3api();
        this.properties = properties;
    }
    
    /**
     * Empty constructor.
     */
    public TS3LoginModule(){};

    /**
     * Gets a list of clients by their ip.
     * @param ip IP of the clients to get.
     * @return List of clients with connected with the specific ip.
     * @throws TimeoutException Thrown when the request takes to long.
     */
    public List<Client> getClientsByIp(String ip) throws TimeoutException {
        CommandFuture<List<Client>> dbClients = ts3api.getClients();
        List<Client> clients = new ArrayList<>();
        try {
            List<Client> allClients = dbClients.get(properties.getRequestTimeout(), TimeUnit.MILLISECONDS);
            if (allClients == null)
                return null; //just a null check
            for (Client client : allClients) {
                if (client.getIp().equals(ip)) {
                    clients.add(client);
                }
            }
        } catch (InterruptedException e) {
            log.warn("Interrupted while requesting clients.", e);
            return null;
        }
        return clients;
    }

    /**
     * Sends a password to the user with the given database id.
     * Password will be send with a generated text from the i18n module.
     * This method is non blocking.
     * @param dbID Database id of the user the password should be send to.
     * @param password Password that should be send.
     */
    // non blocking
    //TODO: this method with an on success and on failure method as a parameter.
    public void sendPasswordToUser(int dbID, String password){
        CommandFuture<DatabaseClientInfo> client = ts3api.getDatabaseClientInfo(dbID);
        client.onSuccess(dbClientInfo -> {sendPasswordToDBClient(dbClientInfo, password);});
        client.onFailure( result -> {log.warn("Failed to get db client info for db id \""+dbID+"\"");});
    }
    
    private void sendPasswordToDBClient(DatabaseClientInfo dbClientInfo, String password){
        if (dbClientInfo == null){
            log.warn("Returned null db client info. Can't send password.");
            return;
        }
        CommandFuture<ClientInfo> clientInfoFuture = ts3api.getClientByUId(dbClientInfo.getUniqueIdentifier());
        clientInfoFuture.onSuccess( clientInfo -> {sendPasswordToClient(clientInfo, password);});
        clientInfoFuture.onFailure( result -> {log.warn("Failed to get client info for client uid \""+dbClientInfo.getUniqueIdentifier()+"\"");});
    }

    private void sendPasswordToClient(ClientInfo clientInfo, String password) {
        if (clientInfo == null){
            log.warn("Returned null client info. Can't send password.");
            return;
        }
        CommandFuture<Boolean> privateMessageFuture = ts3api.sendPrivateMessage(clientInfo.getId(), MessageBuilder.generatePasswordMessage(password, clientInfo.getNickname()));
        privateMessageFuture.onFailure( result -> {log.warn("Failed to send private message to user with uid \""+clientInfo.getUniqueIdentifier()+"\"");});
    }
}
