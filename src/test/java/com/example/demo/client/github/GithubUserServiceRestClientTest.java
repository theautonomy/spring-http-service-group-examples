package com.example.demo.client.github;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import com.example.demo.model.GithubUser;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.restclient.test.autoconfigure.RestClientTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

@RestClientTest
class GithubUserServiceRestClientTest {

    @Autowired private GithubUserService service;

    @Autowired private MockRestServiceServer server;

    @Configuration
    static class TestClientConfig {
        @Bean
        GithubUserService githubUserService(RestClient.Builder builder) {
            RestClient restClient = builder.build();
            RestClientAdapter adapter = RestClientAdapter.create(restClient);
            HttpServiceProxyFactory factory = HttpServiceProxyFactory.builderFor(adapter).build();
            return factory.createClient(GithubUserService.class);
        }
    }

    @Test
    void getAuthenticatedUser_shouldReturnGithubUser() {
        // Given
        String jsonResponse =
                """
            {
                "login": "octocat",
                "id": 12345,
                "name": "The Octocat"
            }
            """;

        server.expect(requestTo("/user"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(jsonResponse, MediaType.APPLICATION_JSON));

        // When
        GithubUser user = service.getAuthenticatedUser();

        // Then
        assertThat(user).isNotNull();
        assertThat(user.login()).isEqualTo("octocat");
        assertThat(user.id()).isEqualTo(12345);
        assertThat(user.name()).isEqualTo("The Octocat");
        server.verify();
    }

    @Test
    void getAuthenticatedUser_shouldHandleUserWithoutName() {
        // Given
        String jsonResponse =
                """
            {
                "login": "testuser",
                "id": 54321,
                "name": null
            }
            """;

        server.expect(requestTo("/user"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(jsonResponse, MediaType.APPLICATION_JSON));

        // When
        GithubUser user = service.getAuthenticatedUser();

        // Then
        assertThat(user).isNotNull();
        assertThat(user.login()).isEqualTo("testuser");
        assertThat(user.id()).isEqualTo(54321);
        assertThat(user.name()).isNull();
        server.verify();
    }

    @Test
    void getAuthenticatedUser_shouldHandleCompleteUserProfile() {
        // Given
        String jsonResponse =
                """
            {
                "login": "johndoe",
                "id": 99999,
                "name": "John Doe",
                "email": "john@example.com",
                "bio": "Software Developer",
                "location": "San Francisco",
                "company": "Acme Corp"
            }
            """;

        server.expect(requestTo("/user"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(jsonResponse, MediaType.APPLICATION_JSON));

        // When
        GithubUser user = service.getAuthenticatedUser();

        // Then
        assertThat(user).isNotNull();
        assertThat(user.login()).isEqualTo("johndoe");
        assertThat(user.id()).isEqualTo(99999);
        assertThat(user.name()).isEqualTo("John Doe");
        server.verify();
    }
}
