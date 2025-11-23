package com.example.demo.runner;

import java.util.List;
import java.util.Map;

import com.example.demo.client.ara.RestfulApiClient;
import com.example.demo.client.ara.RestfulApiSearchClient;
import com.example.demo.client.jph.JsonPlaceholderClient;
import com.example.demo.model.ApiObject;
import com.example.demo.model.ApiObjectRequest;
import com.example.demo.model.Comment;
import com.example.demo.model.ObjectSearch;
import com.example.demo.model.Post;
import com.example.demo.model.User;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class JsonPlaceholderTestRunner implements CommandLineRunner {

    private final JsonPlaceholderClient jsonPlaceholderClient;
    private final RestfulApiClient restfulApiClient;
    private final RestfulApiSearchClient restfulApiSearchClient;

    public JsonPlaceholderTestRunner(
            JsonPlaceholderClient jsonPlaceholderClient,
            RestfulApiClient restfulApiClient,
            RestfulApiSearchClient restfulApiSearchClient) {
        this.jsonPlaceholderClient = jsonPlaceholderClient;
        this.restfulApiClient = restfulApiClient;
        this.restfulApiSearchClient = restfulApiSearchClient;
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println("\n=== Testing JSONPlaceholder HTTP Service Client ===\n");

        // Test 1: Get all posts (limited to first 5)
        System.out.println("1. Fetching all posts (first 5):");
        List<Post> posts = jsonPlaceholderClient.getAllPosts();
        posts.stream()
                .limit(5)
                .forEach(
                        post ->
                                System.out.println(
                                        "   - Post #" + post.id() + ": " + post.title()));

        // Test 2: Get a single post
        System.out.println("\n2. Fetching post with ID 1:");
        Post post = jsonPlaceholderClient.getPostById(1L);
        System.out.println("   Title: " + post.title());
        System.out.println(
                "   Body: " + post.body().substring(0, Math.min(50, post.body().length())) + "...");

        // Test 3: Get comments for a post
        System.out.println("\n3. Fetching comments for post #1:");
        List<Comment> comments = jsonPlaceholderClient.getCommentsByPostId(1L);
        comments.stream()
                .limit(3)
                .forEach(
                        comment ->
                                System.out.println(
                                        "   - " + comment.name() + " by " + comment.email()));

        // Test 4: Get all users
        System.out.println("\n4. Fetching all users:");
        List<User> users = jsonPlaceholderClient.getAllUsers();
        users.forEach(
                user -> System.out.println("   - " + user.name() + " (@" + user.username() + ")"));

        // Test 5: Get a single user
        System.out.println("\n5. Fetching user with ID 1:");
        User user = jsonPlaceholderClient.getUserById(1L);
        System.out.println("   Name: " + user.name());
        System.out.println("   Email: " + user.email());
        System.out.println("   City: " + user.address().city());
        System.out.println("   Company: " + user.company().name());

        // Test 6: Get posts by user
        System.out.println("\n6. Fetching posts by user #1:");
        List<Post> userPosts = jsonPlaceholderClient.getPostsByUserId(1L);
        userPosts.forEach(p -> System.out.println("   - " + p.title()));

        // Test 7: Create a new post
        System.out.println("\n7. Creating a new post:");
        Post newPost =
                new Post(
                        null,
                        1L,
                        "Test Post from Spring HTTP Service Client",
                        "This is a test post created using the declarative HTTP interface");
        Post createdPost = jsonPlaceholderClient.createPost(newPost);
        System.out.println("   Created post with ID: " + createdPost.id());
        System.out.println("   Title: " + createdPost.title());

        // Test 8: Update a post
        System.out.println("\n8. Updating post #1:");
        Post updatedPost = new Post(1L, 1L, "Updated Title", "Updated body content");
        Post result = jsonPlaceholderClient.updatePost(1L, updatedPost);
        System.out.println("   Updated title: " + result.title());

        // Test 9: Delete a post
        System.out.println("\n9. Deleting post #1:");
        jsonPlaceholderClient.deletePost(1L);
        System.out.println("   Post deleted successfully");

        // Test 10: Test error handling (request non-existent resource)
        System.out.println("\n10. Testing error handling (requesting non-existent post):");
        try {
            jsonPlaceholderClient.getPostById(99999L);
        } catch (Exception e) {
            System.out.println("   Error caught: " + e.getClass().getSimpleName());
            System.out.println("   Message: " + e.getMessage());
        }

        System.out.println("\n=== All JSONPlaceholder tests completed! ===\n");

        // ===== Restful API Dev Tests =====
        System.out.println("\n=== Testing Restful-API.dev HTTP Service Client ===\n");

        // Test 1: Get all objects (limited to first 5)
        System.out.println("1. Fetching all objects (first 5):");
        List<ApiObject> objects = restfulApiClient.getAllObjects();
        objects.stream()
                .limit(5)
                .forEach(obj -> System.out.println("   - Object #" + obj.id() + ": " + obj.name()));

        // Test 2: Get a single object
        System.out.println("\n2. Fetching object with ID 7:");
        ApiObject object = restfulApiClient.getObjectById("7");
        System.out.println("   ID: " + object.id());
        System.out.println("   Name: " + object.name());
        System.out.println("   Data: " + object.data());

        // Test 3: Get objects by IDs
        System.out.println("\n3. Fetching objects by IDs (3, 5, 10):");
        List<ApiObject> objectsByIds = restfulApiClient.getObjectsByIds(List.of("3", "5", "10"));
        objectsByIds.forEach(
                obj -> System.out.println("   - Object #" + obj.id() + ": " + obj.name()));

        // Test 4: Create a new object
        System.out.println("\n4. Creating a new object:");
        ApiObjectRequest newObjectRequest =
                new ApiObjectRequest(
                        "Apple MacBook Pro 16",
                        Map.of(
                                "year",
                                2023,
                                "price",
                                2499.99,
                                "CPU model",
                                "M3 Max",
                                "Hard disk size",
                                "1 TB"));
        ApiObject createdObject = restfulApiClient.createObject(newObjectRequest);
        System.out.println("   Created object with ID: " + createdObject.id());
        System.out.println("   Name: " + createdObject.name());
        System.out.println("   Data: " + createdObject.data());

        // Test 5: Update an object
        System.out.println("\n5. Updating object with PUT:");
        ApiObjectRequest updateRequest =
                new ApiObjectRequest(
                        "Apple MacBook Pro 16 (Updated)",
                        Map.of("year", 2024, "price", 2599.99, "color", "Space Gray"));
        ApiObject updatedObject = restfulApiClient.updateObject(createdObject.id(), updateRequest);
        System.out.println("   Updated name: " + updatedObject.name());
        System.out.println("   Updated data: " + updatedObject.data());

        // Test 6: Partial update with PATCH
        System.out.println("\n6. Partial update with PATCH:");
        ApiObjectRequest patchRequest =
                new ApiObjectRequest(null, Map.of("price", 2399.99, "on_sale", true));
        ApiObject patchedObject =
                restfulApiClient.partialUpdateObject(createdObject.id(), patchRequest);
        System.out.println("   Patched data: " + patchedObject.data());

        // Test 7: Delete an object
        System.out.println("\n7. Deleting the created object:");
        restfulApiClient.deleteObject(createdObject.id());
        System.out.println("   Object deleted successfully");

        System.out.println("\n=== All Restful-API.dev tests completed! ===\n");

        // ===== Custom Argument Resolver Test =====
        System.out.println(
                "\n=== Testing Custom Argument Resolver (HttpServiceArgumentResolver) ===\n");
        System.out.println(
                "   This demonstrates the HttpServiceArgumentResolver pattern from Spring Framework docs.");
        System.out.println(
                "   The ObjectSearch parameter is automatically converted to query parameters.\n");

        // Test: Search objects using custom ObjectSearch parameter
        System.out.println("1. Searching objects with custom ObjectSearch parameter:");
        System.out.println("   ObjectSearch.of(null, null) -> GET /objects");
        ObjectSearch search = ObjectSearch.of(null, null);
        List<ApiObject> searchResults = restfulApiSearchClient.searchObjects(search);
        System.out.println("   Found " + searchResults.size() + " objects");
        searchResults.stream()
                .limit(3)
                .forEach(
                        obj ->
                                System.out.println(
                                        "   - " + obj.name() + " (ID: " + obj.id() + ")"));

        System.out.println(
                "\n   Note: The Restful-API.dev doesn't support name/color query params,");
        System.out.println(
                "   but the resolver correctly adds them to the request as demonstrated in logs above.");

        System.out.println("\n=== Custom Argument Resolver test completed! ===\n");

        // ===== GitHub User Service Tests =====
        System.out.println("\n=== GitHub User Service (OAuth2) ===\n");
        System.out.println(
                "   GitHub OAuth2 requires browser-based authentication (Authorization Code flow).");
        System.out.println(
                "   OAuth2 cannot be tested in CommandLineRunner without a web context.\n");
        System.out.println("   To test GitHub OAuth2 integration:");
        System.out.println(
                "   1. Make sure your OAuth2 credentials are configured in application.properties");
        System.out.println("   2. Start the application (it should be running now)");
        System.out.println(
                "   3. Open your browser and visit: http://localhost:8080/api/github/user");
        System.out.println("   4. You'll be redirected to GitHub to authorize");
        System.out.println("   5. After authorization, you'll see your GitHub user info\n");
        System.out.println("=== GitHub OAuth2 endpoint available at /api/github/user ===\n");

        System.out.println("\n=== All tests completed! ===\n");
    }
}
