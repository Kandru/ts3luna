package eu.kandru.luna.teamspeak.modules.login;

import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is; // only is(Class) is deprecated
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.context.junit4.SpringRunner;

import com.github.theholywaffle.teamspeak3.TS3ApiAsync;
import com.github.theholywaffle.teamspeak3.api.CommandFuture;
import com.github.theholywaffle.teamspeak3.api.wrapper.Client;
import com.github.theholywaffle.teamspeak3.api.wrapper.ClientInfo;
import com.github.theholywaffle.teamspeak3.api.wrapper.DatabaseClientInfo;

import eu.kandru.luna.teamspeak.TS3Manager;
import eu.kandru.luna.teamspeak.TS3Properties;

@RunWith(SpringRunner.class)
public class TS3LoginModuleTest {

    @Mock TS3Manager manager;
    @Mock TS3Properties properties;
    @Mock TS3ApiAsync ts3api;
    @Mock CommandFuture<List<Client>> dbClients;
    @Mock Client matchingClient;
    @Mock Client missmatchingClient;
    @Mock DatabaseClientInfo dbClientInfo;
    @Mock ClientInfo clientInfo;
    
    @Captor ArgumentCaptor<String> messageCaptor;
    
    //test get by ip
    String ipToCheck = "127.0.0.1";
    List<Client> clients;
    
    //test send password
    CommandFuture<DatabaseClientInfo> dbClientInfoFuture;
    CommandFuture<ClientInfo> clientInfoFuture;
    CommandFuture<Boolean> privateMessageFuture;
    String clientUid = "some_uid";
    int clientDbId = 1337;
    int clientId = 42;
    String clientNickname = "Clientnick";
    String password = "123456";
    
    @InjectMocks
    TS3LoginModule systemUnderTest;
    
    @Before
    public void setUp() throws InterruptedException, TimeoutException{
        MockitoAnnotations.initMocks(this);
        
        clients = new ArrayList<>();
        clients.add(matchingClient);
        clients.add(missmatchingClient);
        
        dbClientInfoFuture = new CommandFuture<>();
        clientInfoFuture = new CommandFuture<>();
        privateMessageFuture = new CommandFuture<>();
        
        when(matchingClient.getIp()).thenReturn(ipToCheck);
        when(missmatchingClient.getIp()).thenReturn("wrong ip");
        when(manager.getTs3api()).thenReturn(ts3api);
        when(ts3api.getClients()).thenReturn(dbClients);
        when(dbClients.get(anyLong(),any())).thenReturn(clients);
        when(properties.getRequestTimeout()).thenReturn(500);
        
        when(ts3api.getDatabaseClientInfo(eq(clientDbId))).thenReturn(dbClientInfoFuture);
        when(ts3api.getClientByUId(eq(clientUid))).thenReturn(clientInfoFuture);
        when(dbClientInfo.getUniqueIdentifier()).thenReturn(clientUid);
        when(clientInfo.getId()).thenReturn(clientId);
        when(clientInfo.getUniqueIdentifier()).thenReturn(clientUid);
        when(clientInfo.getNickname()).thenReturn(clientNickname);
        when(ts3api.sendPrivateMessage(anyInt(), any())).thenReturn(privateMessageFuture);
    }
    
    @Test
    public void testGetClientsByIp() throws TimeoutException{
        List<Client> foundClients = systemUnderTest.getClientsByIp(ipToCheck);
        assertThat(foundClients, is(not(empty())));
        assertThat(foundClients.size(), is(equalTo(1)));
        assertThat(foundClients.get(0), is(equalTo(matchingClient)));
    }
    
    @Test
    public void testSendPasswordToUser() {
        systemUnderTest.sendPasswordToUser(clientDbId, password);
        
        dbClientInfoFuture.set(dbClientInfo);
        clientInfoFuture.set(clientInfo);
        privateMessageFuture.set(null);
        
        verify(ts3api).sendPrivateMessage(eq(42), messageCaptor.capture());
        
        if(!messageCaptor.getValue().contains(password)){
            fail("Password message did not contain password. Expected password \""+password+"\", received message \""+messageCaptor.getValue()+"\".");
        }
    }
    
}
