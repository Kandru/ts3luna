package eu.kandru.luna.teamspeak.modules.login;

import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is; // only is(Class) is deprecated
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.context.junit4.SpringRunner;

import com.github.theholywaffle.teamspeak3.TS3ApiAsync;
import com.github.theholywaffle.teamspeak3.api.CommandFuture;
import com.github.theholywaffle.teamspeak3.api.wrapper.Client;

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
	
	String ipToCheck = "127.0.0.1";
	
	List<Client> clients;
	
	@InjectMocks
	TS3LoginModule systemUnderTest;
	
	@Before
	public void setUp() throws InterruptedException, TimeoutException{
		MockitoAnnotations.initMocks(this);
		
		clients = new ArrayList<>();
		clients.add(matchingClient);
		clients.add(missmatchingClient);
		
		when(matchingClient.getIp()).thenReturn(ipToCheck);
		when(missmatchingClient.getIp()).thenReturn("wrong ip");
		when(manager.getTs3api()).thenReturn(ts3api);
		when(ts3api.getClients()).thenReturn(dbClients);
		when(dbClients.get(anyLong(),any())).thenReturn(clients);
		when(properties.getRequestTimeout()).thenReturn(500);
	}
	
	@Test
	public void testGetClientsByIp() throws TimeoutException{
		List<Client> foundClients = systemUnderTest.getClientsByIp(ipToCheck);
		assertThat(foundClients, is(not(empty())));
		assertThat(foundClients.size(), is(equalTo(1)));
		assertThat(foundClients.get(0), is(equalTo(matchingClient)));
	}
	
	
}
