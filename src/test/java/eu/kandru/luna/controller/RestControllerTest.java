package eu.kandru.luna.controller;

import eu.kandru.luna.teamspeak.AbstractTS3ApiMock;
import org.junit.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.mock.http.MockHttpOutputMessage;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import javax.servlet.Filter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;


/**
 * Created by jko on 16.04.2017.
 */
public class RestControllerTest extends AbstractTS3ApiMock {

    protected MockMvc mockMvc;
    protected MediaType contentType = new MediaType(MediaType.APPLICATION_JSON.getType(),
                                                    MediaType.APPLICATION_JSON.getSubtype(),
                                                    StandardCharsets.UTF_8);
    @Autowired
    private WebApplicationContext webApplicationContext;
    @Autowired
    private Filter springSecurityFilterChain;
    private HttpMessageConverter<Object> httpConverter;

    @Autowired
    void setConverters(HttpMessageConverter<Object>[] converters) {

        httpConverter = Arrays.stream(converters)
                .filter(MappingJackson2HttpMessageConverter.class::isInstance)
                              .findAny()
                              .orElse(null);

        assertThat(httpConverter).isNotNull();
    }

    @Before
    public void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                                 .addFilter(springSecurityFilterChain)
                                 .build();
    }

    protected String json(Object o) throws IOException {
        MockHttpOutputMessage output = new MockHttpOutputMessage();
        httpConverter.write(o, contentType, output);
        return output.getBodyAsString();
    }

}
