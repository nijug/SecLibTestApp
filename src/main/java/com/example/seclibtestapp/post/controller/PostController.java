package com.example.seclibtestapp.post.controller;

import com.example.seclibtestapp.post.model.Post;
import com.example.seclibtestapp.post.service.PostService;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpSession;
import com.seclib.userRoles.permissions.RequiredPermissions;



@RestController
@RequestMapping("/api/posts")
public class PostController {
    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    @RequiredPermissions("ReadPost")
    @GetMapping
    public Iterable<Post> getAllPosts(HttpSession session) {
        System.out.println("Getting all posts");
        return postService.getAllPosts();
    }

    @RequiredPermissions("WritePost")
    @PostMapping
    public Post createPost(@RequestBody Post post, HttpSession session) {
        System.out.println("Creating post");
        return postService.createPost(post.getTitle(), post.getContent(), post.getAuthor());
    }

    @RequiredPermissions("WritePost")
    @PutMapping("/{id}")
    public Post updatePost(@PathVariable Long id, @RequestBody Post post, HttpSession session) {
        System.out.println("Updating post");
        return postService.updatePost(id, post.getTitle(), post.getContent(), post.getAuthor());
    }

    @RequiredPermissions("WritePost")
    @DeleteMapping("/{id}")
    public void deletePost(@PathVariable Long id, @RequestParam String author, HttpSession session) {
        System.out.println("Deleting post");
        postService.deletePost(id, author);
    }
}