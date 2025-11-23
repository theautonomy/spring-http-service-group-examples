package com.example.demo.client.jph;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import java.util.List;

import com.example.demo.model.Comment;
import com.example.demo.model.Post;
import com.example.demo.model.User;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.ApiVersionInserter;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

class JsonPlaceholderClientTest {

    private JsonPlaceholderClient client;
    private MockRestServiceServer server;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder();
        server = MockRestServiceServer.bindTo(builder).build();

        // Purposely use "X-API-VERSION2"
        // Line 92 needs to match
        RestClient restClient =
                builder.apiVersionInserter(ApiVersionInserter.useHeader("X-API-VERSION2")).build();
        RestClientAdapter adapter = RestClientAdapter.create(restClient);
        HttpServiceProxyFactory factory = HttpServiceProxyFactory.builderFor(adapter).build();

        client = factory.createClient(JsonPlaceholderClient.class);
    }

    @Test
    void getAllPosts_shouldReturnListOfPosts() {
        // Given
        String jsonResponse =
                """
            [
                {
                    "id": 1,
                    "userId": 1,
                    "title": "Test Post 1",
                    "body": "Test body 1"
                },
                {
                    "id": 2,
                    "userId": 1,
                    "title": "Test Post 2",
                    "body": "Test body 2"
                }
            ]
            """;

        server.expect(requestTo("/posts"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(jsonResponse, MediaType.APPLICATION_JSON));

        // When
        List<Post> posts = client.getAllPosts();

        // Then
        assertThat(posts).hasSize(2);
        assertThat(posts.get(0).id()).isEqualTo(1L);
        assertThat(posts.get(0).title()).isEqualTo("Test Post 1");
        assertThat(posts.get(1).id()).isEqualTo(2L);
        server.verify();
    }

    @Test
    void getPostById_shouldReturnSinglePost() {
        // Given
        Long postId = 1L;
        String jsonResponse =
                """
            {
                "id": 1,
                "userId": 1,
                "title": "Test Post",
                "body": "Test body"
            }
            """;

        server.expect(header("X-API-VERSION2", "2.0.0"))
                .andExpect(requestTo("/posts/1"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(jsonResponse, MediaType.APPLICATION_JSON));

        // When
        Post post = client.getPostById(postId);

        // Then
        assertThat(post).isNotNull();
        assertThat(post.id()).isEqualTo(1L);
        assertThat(post.title()).isEqualTo("Test Post");
        assertThat(post.body()).isEqualTo("Test body");
        server.verify();
    }

    @Test
    void getCommentsByPostId_shouldReturnListOfComments() {
        // Given
        Long postId = 1L;
        String jsonResponse =
                """
            [
                {
                    "id": 1,
                    "postId": 1,
                    "name": "Test Comment",
                    "email": "test@example.com",
                    "body": "Test comment body"
                }
            ]
            """;

        server.expect(requestTo("/posts/1/comments"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(jsonResponse, MediaType.APPLICATION_JSON));

        // When
        List<Comment> comments = client.getCommentsByPostId(postId);

        // Then
        assertThat(comments).hasSize(1);
        assertThat(comments.get(0).postId()).isEqualTo(1L);
        assertThat(comments.get(0).email()).isEqualTo("test@example.com");
        server.verify();
    }

    @Test
    void createPost_shouldReturnCreatedPost() {
        // Given
        Post newPost = new Post(null, 1L, "New Post", "New body");
        String jsonResponse =
                """
            {
                "id": 101,
                "userId": 1,
                "title": "New Post",
                "body": "New body"
            }
            """;

        server.expect(requestTo("/posts"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess(jsonResponse, MediaType.APPLICATION_JSON));

        // When
        Post createdPost = client.createPost(newPost);

        // Then
        assertThat(createdPost).isNotNull();
        assertThat(createdPost.id()).isEqualTo(101L);
        assertThat(createdPost.title()).isEqualTo("New Post");
        server.verify();
    }

    @Test
    void updatePost_shouldReturnUpdatedPost() {
        // Given
        Long postId = 1L;
        Post updatedPost = new Post(1L, 1L, "Updated Post", "Updated body");
        String jsonResponse =
                """
            {
                "id": 1,
                "userId": 1,
                "title": "Updated Post",
                "body": "Updated body"
            }
            """;

        server.expect(requestTo("/posts/1"))
                .andExpect(method(HttpMethod.PUT))
                .andRespond(withSuccess(jsonResponse, MediaType.APPLICATION_JSON));

        // When
        Post result = client.updatePost(postId, updatedPost);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.title()).isEqualTo("Updated Post");
        server.verify();
    }

    @Test
    void deletePost_shouldCompleteSuccessfully() {
        // Given
        Long postId = 1L;

        server.expect(requestTo("/posts/1"))
                .andExpect(method(HttpMethod.DELETE))
                .andRespond(withSuccess());

        // When
        client.deletePost(postId);

        // Then
        server.verify();
    }

    @Test
    void getAllUsers_shouldReturnListOfUsers() {
        // Given
        String jsonResponse =
                """
            [
                {
                    "id": 1,
                    "name": "John Doe",
                    "username": "johndoe",
                    "email": "john@example.com",
                    "address": {
                        "street": "Main St",
                        "suite": "Apt 1",
                        "city": "New York",
                        "zipcode": "10001",
                        "geo": {
                            "lat": "40.7128",
                            "lng": "-74.0060"
                        }
                    },
                    "phone": "123-456-7890",
                    "website": "example.com",
                    "company": {
                        "name": "Acme Corp",
                        "catchPhrase": "Innovate",
                        "bs": "synergy"
                    }
                }
            ]
            """;

        server.expect(requestTo("/users"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(jsonResponse, MediaType.APPLICATION_JSON));

        // When
        List<User> users = client.getAllUsers();

        // Then
        assertThat(users).hasSize(1);
        assertThat(users.get(0).name()).isEqualTo("John Doe");
        assertThat(users.get(0).email()).isEqualTo("john@example.com");
        assertThat(users.get(0).address().city()).isEqualTo("New York");
        server.verify();
    }

    @Test
    void getUserById_shouldReturnSingleUser() {
        // Given
        Long userId = 1L;
        String jsonResponse =
                """
            {
                "id": 1,
                "name": "John Doe",
                "username": "johndoe",
                "email": "john@example.com",
                "address": {
                    "street": "Main St",
                    "suite": "Apt 1",
                    "city": "New York",
                    "zipcode": "10001",
                    "geo": {
                        "lat": "40.7128",
                        "lng": "-74.0060"
                    }
                },
                "phone": "123-456-7890",
                "website": "example.com",
                "company": {
                    "name": "Acme Corp",
                    "catchPhrase": "Innovate",
                    "bs": "synergy"
                }
            }
            """;

        server.expect(requestTo("/users/1"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(jsonResponse, MediaType.APPLICATION_JSON));

        // When
        User user = client.getUserById(userId);

        // Then
        assertThat(user).isNotNull();
        assertThat(user.id()).isEqualTo(1L);
        assertThat(user.name()).isEqualTo("John Doe");
        assertThat(user.company().name()).isEqualTo("Acme Corp");
        server.verify();
    }

    @Test
    void getPostsByUserId_shouldReturnListOfPosts() {
        // Given
        Long userId = 1L;
        String jsonResponse =
                """
            [
                {
                    "id": 1,
                    "userId": 1,
                    "title": "User Post 1",
                    "body": "Body 1"
                },
                {
                    "id": 2,
                    "userId": 1,
                    "title": "User Post 2",
                    "body": "Body 2"
                }
            ]
            """;

        server.expect(requestTo("/users/1/posts"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(jsonResponse, MediaType.APPLICATION_JSON));

        // When
        List<Post> posts = client.getPostsByUserId(userId);

        // Then
        assertThat(posts).hasSize(2);
        assertThat(posts.get(0).userId()).isEqualTo(1L);
        assertThat(posts.get(1).userId()).isEqualTo(1L);
        server.verify();
    }
}
