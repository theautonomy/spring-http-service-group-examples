package com.example.demo.client.httpbin;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import com.example.demo.model.BasicAuthResponse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

class HttpBinClientTest {

    private HttpBinClient client;
    private MockRestServiceServer server;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder();
        server = MockRestServiceServer.bindTo(builder).build();

        RestClient restClient = builder.build();
        RestClientAdapter adapter = RestClientAdapter.create(restClient);
        HttpServiceProxyFactory factory = HttpServiceProxyFactory.builderFor(adapter).build();

        client = factory.createClient(HttpBinClient.class);
    }

    @Test
    void testBasicAuth_shouldReturnAuthenticationResponse() {
        // Given
        String user = "testuser";
        String password = "testpass";
        String jsonResponse =
                """
            {
                "authenticated": true,
                "user": "testuser"
            }
            """;

        server.expect(requestTo("/basic-auth/testuser/testpass"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(jsonResponse, MediaType.APPLICATION_JSON));

        // When
        BasicAuthResponse response = client.testBasicAuth(user, password);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.authenticated()).isTrue();
        assertThat(response.user()).isEqualTo("testuser");
        server.verify();
    }

    @Test
    void testBasicAuth_shouldHandleFailedAuthentication() {
        // Given
        String user = "wronguser";
        String password = "wrongpass";
        String jsonResponse =
                """
            {
                "authenticated": false,
                "user": "wronguser"
            }
            """;

        server.expect(requestTo("/basic-auth/wronguser/wrongpass"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(jsonResponse, MediaType.APPLICATION_JSON));

        // When
        BasicAuthResponse response = client.testBasicAuth(user, password);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.authenticated()).isFalse();
        server.verify();
    }

    @Test
    void getHtml_shouldReturnHtmlContent() {
        // Given
        String htmlResponse =
                """
            <!DOCTYPE html>
            <html>
            <head><title>Test Page</title></head>
            <body><h1>Hello World</h1></body>
            </html>
            """;

        server.expect(requestTo("/html"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(htmlResponse, MediaType.TEXT_HTML));

        // When
        String html = client.getHtml();

        // Then
        assertThat(html).isNotNull();
        assertThat(html).contains("<!DOCTYPE html>");
        assertThat(html).contains("Hello World");
        server.verify();
    }

    @Test
    void getUuid_shouldReturnUuidString() {
        // Given
        String uuidResponse = "550e8400-e29b-41d4-a716-446655440000";

        server.expect(requestTo("/uuid"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(uuidResponse, MediaType.TEXT_PLAIN));

        // When
        String uuid = client.getUuid();

        // Then
        assertThat(uuid).isNotNull();
        assertThat(uuid).matches("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}");
        server.verify();
    }
}
