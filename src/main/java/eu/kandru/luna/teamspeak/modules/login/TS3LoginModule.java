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
import lombok.extern.slf4j.Slf4j;

/**
 * TODO
 * @author Jonsen
 *
 */
@Slf4j
@Component
public class TS3LoginModule {
	private TS3ApiAsync ts3api;
	private final int REQUEST_TIMEOUT = 10000;

	/**
	 * TODO
	 * @param ts3Manager
	 */
	@Autowired
	public TS3LoginModule(TS3Manager ts3Manager) {
		this.ts3api = ts3Manager.getTs3api();
	}
	
	/**
	 * Empty constructor.
	 */
	public TS3LoginModule(){};

	/**
	 * TODO
	 * @param ip
	 * @return
	 * @throws TimeoutException
	 */
	public List<Client> getClientsByIp(String ip) throws TimeoutException {
		CommandFuture<List<Client>> dbClients = ts3api.getClients();
		List<Client> clients = new ArrayList<>();
		try {
			List<Client> allClients = dbClients.get(REQUEST_TIMEOUT, TimeUnit.MILLISECONDS);
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
	 * TODO
	 * @param dbID
	 * @param password
	 */
	// non blocking
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
