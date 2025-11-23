package com.example.demo.client.jph;

import java.util.List;

import com.example.demo.model.Comment;
import com.example.demo.model.Post;
import com.example.demo.model.User;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.DeleteExchange;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.PostExchange;
import org.springframework.web.service.annotation.PutExchange;

public interface JsonPlaceholderClient {

    @GetExchange("/posts")
    List<Post> getAllPosts();

    @GetExchange(url = "/posts/{id}", version = "2.0.0")
    Post getPostById(@PathVariable Long id);

    @GetExchange("/posts/{postId}/comments")
    List<Comment> getCommentsByPostId(@PathVariable Long postId);

    @PostExchange("/posts")
    Post createPost(@RequestBody Post post);

    @PutExchange("/posts/{id}")
    Post updatePost(@PathVariable Long id, @RequestBody Post post);

    @DeleteExchange("/posts/{id}")
    void deletePost(@PathVariable Long id);

    @GetExchange("/users")
    List<User> getAllUsers();

    @GetExchange("/users/{id}")
    User getUserById(@PathVariable Long id);

    @GetExchange("/users/{userId}/posts")
    List<Post> getPostsByUserId(@PathVariable Long userId);
}
