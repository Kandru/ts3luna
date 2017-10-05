package eu.kandru.luna.controller;


import com.github.theholywaffle.teamspeak3.api.CommandFuture;
import com.github.theholywaffle.teamspeak3.api.wrapper.ClientInfo;
import com.jayway.jsonpath.JsonPath;
import eu.kandru.luna.model.json.AuthChallengeRequest;
import eu.kandru.luna.model.json.AuthenticateRequest;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Created by jko on 16.04.2017.
 */
@RunWith(SpringRunner.class)
@WebAppConfiguration
@ContextConfiguration
@SpringBootTest
@EnableWebSecurity
public class AuthControllerTest extends RestControllerTest {

    public static final String TEST_URL = "/protected/test";
    public static final String ADMIN_TEST_URL = "/admin/test";
    @Captor
    private ArgumentCaptor<String> passwordTextCaptor;
	private CommandFuture<Boolean> privateMessageFuture;

    private static final int CREATE_CLIENT_COUNT = 10;
    private ClientInfo client;
    private ClientInfo admin;
    // 0 < CLIENT_ID < CREATE_CLIENT_COUNT

    @Before
    public void prepare() throws Exception {
        addClients(CREATE_CLIENT_COUNT);
        client = addDefaultClient();
        admin = addAdminClient();
        privateMessageFuture = new CommandFuture<>();
        
        when(ts3api.sendPrivateMessage(anyInt(), any())).thenReturn(privateMessageFuture);
    }

    @Test
    public void testInaccessible() throws Exception {
        mockMvc.perform(get(TEST_URL)).andExpect(status().isUnauthorized());
    }

    @Test
    public void testChallenge() throws Exception {
        challenge(client);
    }

    @Test
    public void testAuthentication() throws Exception {
        String challenge = challenge(client);
        authenticateSuccessful(challenge);
    }

    @Test
    public void testAccessible() throws Exception {
        String token = authenticateSuccessful(challenge(client));
        mockMvc.perform(get(TEST_URL).header("Authorization", "Bearer " + token)).andExpect(status().isOk());
    }

    @Test
    public void testAdminUrlAccessible() throws Exception {
        String adminChallenge = challenge(admin);
        String adminToken = authenticateSuccessful(adminChallenge);
        mockMvc.perform(get(ADMIN_TEST_URL).header("Authorization", "Bearer " + adminToken)).andExpect(status().isOk());
    }

    @Test
    public void testAdminUrlInaccessible() throws Exception {
        String challenge = challenge(client);
        String token = authenticateSuccessful(challenge);
        mockMvc.perform(get(ADMIN_TEST_URL).header("Authorization", "Bearer " + token)).andExpect(status().is4xxClientError());
    }

    private String challenge(ClientInfo client) throws Exception {
        AuthChallengeRequest request = AuthChallengeRequest.builder().clientDbId(client.getDatabaseId()).build();
        MvcResult result = mockMvc.perform(post("/auth/challenge").contentType(contentType).content(json(request)))
                                  .andExpect(status().isOk())
                                  .andExpect(jsonPath("$.challenge", not(isEmptyString())))
                                  .andExpect(jsonPath("$.expires", is(5)))
                                  .andReturn();
        int index = mockedClientInfos.indexOf(client);
        dbClientInfoFutures.get(index).set(mockedDbClientInfos.get(index));
        clientInfoFutures.get(index).set(mockedClientInfos.get(index));
        return JsonPath.read(result.getResponse().getContentAsString(), "$.challenge");
    }

    private ResultActions authenticate(String challenge, String password) throws Exception {
        AuthenticateRequest request = AuthenticateRequest.builder()
                                                         .challenge(challenge)
                                                         .identification(password)
                                                         .build();
        return mockMvc.perform(post("/auth/authenticate").contentType(contentType).content(json(request)));
    }

    private String authenticateSuccessful(String challenge) throws Exception {
        verify(ts3api).sendPrivateMessage(anyInt(), passwordTextCaptor.capture());
        Pattern p = Pattern.compile("[0-9]{6}");
        Matcher m = p.matcher(passwordTextCaptor.getValue());
        assertThat(m.find()).isTrue();
        String sentPassword = m.group();
        
        MvcResult result = authenticate(challenge, sentPassword).andExpect(status().isOk())
                                                            .andExpect(jsonPath("$.authToken", not(isEmptyString())))
                                                            .andExpect(jsonPath("$.expires", is(greaterThan(5))))
                                                            .andExpect(jsonPath("$.success", is(true))).andReturn();
        return JsonPath.read(result.getResponse().getContentAsString(), "$.authToken");
    }
}
