package com.example.demo.runner;

import java.util.List;
import java.util.Map;

import com.example.demo.client.ara.RestfulApiClient;
import com.example.demo.client.jph.JsonPlaceholderClient;
import com.example.demo.model.ApiObject;
import com.example.demo.model.ApiObjectRequest;
import com.example.demo.model.Comment;
import com.example.demo.model.Post;
import com.example.demo.model.User;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class JsonPlaceholderTestRunner implements CommandLineRunner {

    private final JsonPlaceholderClient jsonPlaceholderClient;
    private final RestfulApiClient restfulApiClient;

    public JsonPlaceholderTestRunner(
            JsonPlaceholderClient jsonPlaceholderClient, RestfulApiClient restfulApiClient) {
        this.jsonPlaceholderClient = jsonPlaceholderClient;
        this.restfulApiClient = restfulApiClient;
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

        System.out.println("\n=== All tests completed successfully! ===\n");
    }
}
