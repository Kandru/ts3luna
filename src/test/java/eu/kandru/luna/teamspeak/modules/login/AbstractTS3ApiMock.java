package eu.kandru.luna.teamspeak.modules.login;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;

import org.junit.Before;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.springframework.boot.test.mock.mockito.MockBean;

import com.github.theholywaffle.teamspeak3.TS3ApiAsync;
import com.github.theholywaffle.teamspeak3.api.CommandFuture;
import com.github.theholywaffle.teamspeak3.api.wrapper.Client;
import com.github.theholywaffle.teamspeak3.api.wrapper.ClientInfo;
import com.github.theholywaffle.teamspeak3.api.wrapper.DatabaseClient;
import com.github.theholywaffle.teamspeak3.api.wrapper.DatabaseClientInfo;

import eu.kandru.luna.teamspeak.TS3Manager;
import eu.kandru.luna.teamspeak.TS3Properties;


public abstract class AbstractTS3ApiMock {

    @MockBean protected TS3Manager manager;
    @MockBean protected TS3Properties properties;
    @MockBean protected TS3ApiAsync ts3api;
	
    @Spy protected CommandFuture<List<Client>> clientList;
    protected List<CommandFuture<ClientInfo>> clientInfoFutures;
    protected List<ClientInfo> mockedClientInfos;
    protected List<Client> mockedClients;
    
    @Spy protected CommandFuture<List<DatabaseClient>> dbClientList;
    protected List<CommandFuture<DatabaseClientInfo>> dbClientInfoFutures;
    protected List<DatabaseClientInfo> mockedDbClientInfos;
    protected List<DatabaseClient> mockedDbClients;    
    
    protected static final int DEFAULT_REQUEST_TIMEOUT = 500;
    protected static final int DEFAULT_SERVER_PORT = 4242;
    protected static final String DEFAULT_TS3_IP = "192.168.42.42";
    
    
    @Before
    public void mockTS3Api() throws InterruptedException, TimeoutException{
    	//MockitoAnnotations.initMocks(this);
    	
    	mockedClients  = new ArrayList<>();
    	mockedClientInfos  = new ArrayList<>();
    	mockedDbClients = new ArrayList<>();
    	mockedDbClientInfos = new ArrayList<>();
    	clientInfoFutures = new ArrayList<>();
    	dbClientInfoFutures = new ArrayList<>();
    	
        when(manager.getTs3api()).thenReturn(ts3api);
        when(ts3api.getClients()).thenReturn(clientList);
        when(ts3api.getDatabaseClients()).thenReturn(dbClientList);
        
        //Mock spies
        doReturn(mockedClients).when(clientList).get();
        doReturn(mockedClients).when(clientList).get(anyLong(),any());
        doReturn(mockedDbClients).when(dbClientList).get();
        doReturn(mockedDbClients).when(dbClientList).get(anyLong(),any());
        
        when(properties.getRequestTimeout()).thenReturn(DEFAULT_REQUEST_TIMEOUT);
        when(properties.getIp()).thenReturn(DEFAULT_TS3_IP);
        when(properties.getPort()).thenReturn(DEFAULT_SERVER_PORT);
    }
    
    
    //Clients will be named Client0, Client1 ...
    //Clients ips will be 192.168.0.i with i as the client nr 0,1,...
    //Clients id will be 0,1,...
    //Clients uids are UniqueIdentifier0, UniqueIdentifier1, ...
    //This will also call add db clients
    protected void addClients(int count) throws InterruptedException, TimeoutException{
    	for (int i = 0; i < count; i++){    		
    		ClientInfo mockClient = mock(ClientInfo.class);
    		mockedClients.add(mockClient);
    		mockedClientInfos.add(mockClient);
    		
    		when(mockClient.getId()).thenReturn(i);
    		when(mockClient.getIp()).thenReturn("192.168.0."+i);
    		when(mockClient.getNickname()).thenReturn("Client"+i);
    		when(mockClient.getUniqueIdentifier()).thenReturn("UniqueIdentifier"+i);
    		
			integrateClient(mockClient);
    	}
    	addDatabaseClients(count);
    }
    
  //Clients db id is 0,1,...
  //Clients last ips are 192.168.0.i with i as the client nr 0,1,...
  //Clients names are Client0, Client1 ...
  //Clients uids are UniqueIdentifier0, UniqueIdentifier1, ... 
    protected void addDatabaseClients(int count) throws InterruptedException, TimeoutException{
    	for (int i = 0; i < count; i++){
    		DatabaseClientInfo mockClient = mock(DatabaseClientInfo.class);
    		mockedDbClients.add(mockClient);
    		mockedDbClientInfos.add(mockClient);
    		
    		when(mockClient.getDatabaseId()).thenReturn(i);
    		when(mockClient.getLastIp()).thenReturn("192.168.0."+i);
    		when(mockClient.getNickname()).thenReturn("Client"+i);
    		when(mockClient.getUniqueIdentifier()).thenReturn("UniqueIdentifier"+i);
    		
    		integrateDatabaseClient(mockClient);
    		
    		}
    }
    


	protected void integrateClient(ClientInfo mockedClient) throws InterruptedException, TimeoutException {
		@SuppressWarnings("unchecked")
		CommandFuture<ClientInfo> spyCommandFuture = spy(CommandFuture.class);
		
		//you can use those to get the client info and trigger the success method with set
		//TODO: probably we shouldn't mock those to check whether they are used or not
		doReturn(mockedClient).when(spyCommandFuture).get();
		doReturn(mockedClient).when(spyCommandFuture).get(anyLong(),any());
		
		clientInfoFutures.add(spyCommandFuture);
		
		when(ts3api.getClientInfo(mockedClient.getId())).thenReturn(spyCommandFuture);
		when(ts3api.getClientByUId(mockedClient.getUniqueIdentifier())).thenReturn(spyCommandFuture);
	}
    
    protected void integrateDatabaseClient(DatabaseClientInfo mockedClient) throws InterruptedException, TimeoutException{
    	@SuppressWarnings("unchecked")
		CommandFuture<DatabaseClientInfo> spyCommandFuture = spy(CommandFuture.class);
    	
		//you can use those to get the client info and trigger the success method with set
		//TODO: probably we shouldn't mock those to check whether they are used or not
		doReturn(mockedClient).when(spyCommandFuture).get();
		doReturn(mockedClient).when(spyCommandFuture).get(anyLong(),any());
		
		dbClientInfoFutures.add(spyCommandFuture);
		
		when(ts3api.getDatabaseClientInfo(mockedClient.getDatabaseId())).thenReturn(spyCommandFuture);
		when(ts3api.getDatabaseClientByUId(mockedClient.getUniqueIdentifier())).thenReturn(spyCommandFuture);
	
    }
}
