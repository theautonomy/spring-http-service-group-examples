package com.example.demo.client.ara;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.queryParam;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import java.util.List;
import java.util.Map;

import com.example.demo.model.ApiObject;
import com.example.demo.model.ApiObjectRequest;

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
class RestfulApiClientRestClientTest {

    @Autowired private RestfulApiClient client;

    @Autowired private MockRestServiceServer server;

    @Configuration
    static class TestClientConfig {
        @Bean
        RestfulApiClient restfulApiClient(RestClient.Builder builder) {
            RestClient restClient = builder.build();
            RestClientAdapter adapter = RestClientAdapter.create(restClient);
            HttpServiceProxyFactory factory = HttpServiceProxyFactory.builderFor(adapter).build();
            return factory.createClient(RestfulApiClient.class);
        }
    }

    @Test
    void getAllObjects_shouldReturnListOfObjects() {
        // Given
        String jsonResponse =
                """
            [
                {
                    "id": "1",
                    "name": "Object 1",
                    "data": {
                        "color": "red",
                        "size": "large"
                    },
                    "createdAt": "2025-01-01T10:00:00Z",
                    "updatedAt": "2025-01-01T10:00:00Z"
                },
                {
                    "id": "2",
                    "name": "Object 2",
                    "data": {
                        "color": "blue",
                        "size": "small"
                    },
                    "createdAt": "2025-01-02T10:00:00Z",
                    "updatedAt": "2025-01-02T10:00:00Z"
                }
            ]
            """;

        server.expect(requestTo("/objects"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(jsonResponse, MediaType.APPLICATION_JSON));

        // When
        List<ApiObject> objects = client.getAllObjects();

        // Then
        assertThat(objects).hasSize(2);
        assertThat(objects.get(0).id()).isEqualTo("1");
        assertThat(objects.get(0).name()).isEqualTo("Object 1");
        assertThat(objects.get(0).data()).containsEntry("color", "red");
        assertThat(objects.get(1).id()).isEqualTo("2");
        server.verify();
    }

    @Test
    void getObjectsByIds_shouldReturnFilteredObjects() {
        // Given
        List<String> ids = List.of("1", "2");
        String jsonResponse =
                """
            [
                {
                    "id": "1",
                    "name": "Object 1",
                    "data": {},
                    "createdAt": "2025-01-01T10:00:00Z",
                    "updatedAt": "2025-01-01T10:00:00Z"
                },
                {
                    "id": "2",
                    "name": "Object 2",
                    "data": {},
                    "createdAt": "2025-01-02T10:00:00Z",
                    "updatedAt": "2025-01-02T10:00:00Z"
                }
            ]
            """;

        server.expect(requestTo("/objects?id=1&id=2"))
                .andExpect(method(HttpMethod.GET))
                .andExpect(queryParam("id", "1", "2"))
                .andRespond(withSuccess(jsonResponse, MediaType.APPLICATION_JSON));

        // When
        List<ApiObject> objects = client.getObjectsByIds(ids);

        // Then
        assertThat(objects).hasSize(2);
        assertThat(objects.get(0).id()).isEqualTo("1");
        assertThat(objects.get(1).id()).isEqualTo("2");
        server.verify();
    }

    @Test
    void getObjectById_shouldReturnSingleObject() {
        // Given
        String objectId = "123";
        String jsonResponse =
                """
            {
                "id": "123",
                "name": "Test Object",
                "data": {
                    "key": "value"
                },
                "createdAt": "2025-01-01T10:00:00Z",
                "updatedAt": "2025-01-01T10:00:00Z"
            }
            """;

        server.expect(requestTo("/objects/123"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(jsonResponse, MediaType.APPLICATION_JSON));

        // When
        ApiObject object = client.getObjectById(objectId);

        // Then
        assertThat(object).isNotNull();
        assertThat(object.id()).isEqualTo("123");
        assertThat(object.name()).isEqualTo("Test Object");
        assertThat(object.data()).containsEntry("key", "value");
        server.verify();
    }

    @Test
    void createObject_shouldReturnCreatedObject() {
        // Given
        ApiObjectRequest request =
                new ApiObjectRequest("New Object", Map.of("color", "green", "size", "medium"));
        String jsonResponse =
                """
            {
                "id": "new-123",
                "name": "New Object",
                "data": {
                    "color": "green",
                    "size": "medium"
                },
                "createdAt": "2025-01-10T10:00:00Z",
                "updatedAt": "2025-01-10T10:00:00Z"
            }
            """;

        server.expect(requestTo("/objects"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess(jsonResponse, MediaType.APPLICATION_JSON));

        // When
        ApiObject createdObject = client.createObject(request);

        // Then
        assertThat(createdObject).isNotNull();
        assertThat(createdObject.id()).isEqualTo("new-123");
        assertThat(createdObject.name()).isEqualTo("New Object");
        assertThat(createdObject.data()).containsEntry("color", "green");
        server.verify();
    }

    @Test
    void updateObject_shouldReturnUpdatedObject() {
        // Given
        String objectId = "123";
        ApiObjectRequest request =
                new ApiObjectRequest("Updated Object", Map.of("status", "active"));
        String jsonResponse =
                """
            {
                "id": "123",
                "name": "Updated Object",
                "data": {
                    "status": "active"
                },
                "createdAt": "2025-01-01T10:00:00Z",
                "updatedAt": "2025-01-15T10:00:00Z"
            }
            """;

        server.expect(requestTo("/objects/123"))
                .andExpect(method(HttpMethod.PUT))
                .andRespond(withSuccess(jsonResponse, MediaType.APPLICATION_JSON));

        // When
        ApiObject updatedObject = client.updateObject(objectId, request);

        // Then
        assertThat(updatedObject).isNotNull();
        assertThat(updatedObject.id()).isEqualTo("123");
        assertThat(updatedObject.name()).isEqualTo("Updated Object");
        assertThat(updatedObject.data()).containsEntry("status", "active");
        server.verify();
    }

    @Test
    void partialUpdateObject_shouldReturnPartiallyUpdatedObject() {
        // Given
        String objectId = "123";
        ApiObjectRequest request = new ApiObjectRequest(null, Map.of("temperature", "warm"));
        String jsonResponse =
                """
            {
                "id": "123",
                "name": "Existing Object",
                "data": {
                    "color": "red",
                    "temperature": "warm"
                },
                "createdAt": "2025-01-01T10:00:00Z",
                "updatedAt": "2025-01-16T10:00:00Z"
            }
            """;

        server.expect(requestTo("/objects/123"))
                .andExpect(method(HttpMethod.PATCH))
                .andRespond(withSuccess(jsonResponse, MediaType.APPLICATION_JSON));

        // When
        ApiObject patchedObject = client.partialUpdateObject(objectId, request);

        // Then
        assertThat(patchedObject).isNotNull();
        assertThat(patchedObject.id()).isEqualTo("123");
        assertThat(patchedObject.data()).containsEntry("temperature", "warm");
        assertThat(patchedObject.data()).containsEntry("color", "red");
        server.verify();
    }

    @Test
    void deleteObject_shouldCompleteSuccessfully() {
        // Given
        String objectId = "123";

        server.expect(requestTo("/objects/123"))
                .andExpect(method(HttpMethod.DELETE))
                .andRespond(withSuccess());

        // When
        client.deleteObject(objectId);

        // Then
        server.verify();
    }
}
