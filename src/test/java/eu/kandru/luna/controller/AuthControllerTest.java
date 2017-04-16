package eu.kandru.luna.controller;


import com.jayway.jsonpath.JsonPath;
import eu.kandru.luna.model.json.AuthChallengeRequest;
import eu.kandru.luna.model.json.AuthenticateRequest;
import eu.kandru.luna.util.OneTimePasswordGenerator;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;

import static org.hamcrest.Matchers.*;
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

    @MockBean
    private OneTimePasswordGenerator pwGenerator;

    @Before
    public void prepare() throws Exception {
        when(pwGenerator.generatePassword()).thenReturn("123456");
    }

    @Test
    public void testInaccessible() throws Exception {
        mockMvc.perform(get("/test")).andExpect(status().isUnauthorized());
    }

    @Test
    public void testChallenge() throws Exception {
        challenge();
    }

    @Test
    public void testAuthentication() throws Exception {
        String challenge = challenge();
        authenticateSuccessful(challenge);
    }

    @Test
    public void testAccessible() throws Exception {
        String token = authenticateSuccessful(challenge());
        mockMvc.perform(get("/test").header("Authorization", "Bearer " + token)).andExpect(status().isOk());
    }

    private String challenge() throws Exception {
        AuthChallengeRequest request = AuthChallengeRequest.builder().clientDbId(5L).build();
        MvcResult result = mockMvc.perform(post("/auth/challenge").contentType(contentType).content(json(request)))
                                  .andExpect(status().isOk())
                                  .andExpect(jsonPath("$.challenge", not(isEmptyString())))
                                  .andExpect(jsonPath("$.expires", is(5)))
                                  .andReturn();
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
        MvcResult result = authenticate(challenge, "123456").andExpect(status().isOk())
                                                            .andExpect(jsonPath("$.authToken", not(isEmptyString())))
                                                            .andExpect(jsonPath("$.expires", is(greaterThan(5))))
                                                            .andExpect(jsonPath("$.success", is(true))).andReturn();
        return JsonPath.read(result.getResponse().getContentAsString(), "$.authToken");
    }
}
