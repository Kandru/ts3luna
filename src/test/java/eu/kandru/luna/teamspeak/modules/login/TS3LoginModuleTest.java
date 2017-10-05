package eu.kandru.luna.teamspeak.modules.login;

import com.github.theholywaffle.teamspeak3.api.CommandFuture;
import com.github.theholywaffle.teamspeak3.api.wrapper.Client;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
public class TS3LoginModuleTest extends AbstractTS3ApiMock{

	@Captor protected ArgumentCaptor<String> messageCaptor;
	
    private TS3LoginModule systemUnderTest;

	private CommandFuture<Boolean> privateMessageFuture;
	
	private static final int CLIENT_COUNT_TO_TEST = 10;
	private static final String PASSWORD = "some_password";
	private static final int DEFAULT_CLIENT_TO_TEST = 3; 
	//0 < DEFAULT_CLIENT_TO_TEST < CLIENT_COUNT_TO_TEST
    
    @Before
    public void setUp() throws Exception {
        systemUnderTest = new TS3LoginModule(ts3api, properties);
    	
        privateMessageFuture = new CommandFuture<>();
        
        when(ts3api.sendPrivateMessage(anyInt(), any())).thenReturn(privateMessageFuture);
    }
    
    @Test
    public void testGetClientsByIp() throws Exception {
        addClients(CLIENT_COUNT_TO_TEST);
        List<Client> foundClients = systemUnderTest.getClientsByIp(mockedClients.get(DEFAULT_CLIENT_TO_TEST).getIp());
        assertThat(foundClients).isNotEmpty();
        assertThat(foundClients.size()).isEqualTo(1);
        assertThat(foundClients.get(0)).isEqualTo(mockedClients.get(DEFAULT_CLIENT_TO_TEST));
    }
    
    @Test
    public void testSendPasswordToUser() throws Exception {
        addClients(CLIENT_COUNT_TO_TEST);
    	systemUnderTest.sendPasswordToUser(mockedDbClients.get(DEFAULT_CLIENT_TO_TEST).getDatabaseId(), PASSWORD);
        
    	verify(ts3api).getDatabaseClientInfo(mockedDbClients.get(DEFAULT_CLIENT_TO_TEST).getDatabaseId());
    	
        dbClientInfoFutures.get(DEFAULT_CLIENT_TO_TEST).set(mockedDbClientInfos.get(DEFAULT_CLIENT_TO_TEST));
        clientInfoFutures.get(DEFAULT_CLIENT_TO_TEST).set(mockedClientInfos.get(DEFAULT_CLIENT_TO_TEST));
        privateMessageFuture.set(true);
        
        verify(ts3api).sendPrivateMessage(eq(mockedClients.get(DEFAULT_CLIENT_TO_TEST).getId()), messageCaptor.capture());
        
        assertThat(messageCaptor.getValue()).contains(PASSWORD);
    }
    
}
