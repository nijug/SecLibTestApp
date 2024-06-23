package com.example.seclibtestapp.post.service;

import com.example.seclibtestapp.post.repository.PostRepository;
import com.seclib.user.service.DefaultUserService;
import com.seclib.userRoles.service.DefaultRoleService;
import org.springframework.stereotype.Service;
import com.example.seclibtestapp.post.model.Post;

import java.util.Optional;

@Service
public class PostService {
    private final PostRepository postRepository;
    private final DefaultRoleService roleService;

    public PostService(PostRepository postRepository, DefaultRoleService roleService) {
        this.postRepository = postRepository;
        this.roleService = roleService;
    }

    public Post createPost(String title, String content, String author) {
        Post post = new Post();
        post.setTitle(title);
        post.setContent(content);
        post.setAuthor(author);
        return postRepository.save(post);
    }

    public Iterable<Post> getAllPosts() {
        return postRepository.findAll();
    }

    public Optional<Post> getPostById(Long id) {
        return postRepository.findById(id);
    }

    public Post updatePost(Long id, String title, String content, String author) {
        Post post = getPostById(id).orElseThrow(() -> new IllegalArgumentException("Post not found"));
        System.out.println(post.getAuthor());
        System.out.println(author);
        if (!post.getAuthor().equals(author) && !roleService.userHasRole(author, "admin")) {
            throw new IllegalArgumentException("User not authorized to update this post");
        }
        post.setTitle(title);
        post.setContent(content);
        return postRepository.save(post);
    }

    public void deletePost(Long id, String author) {
        Post post = getPostById(id).orElseThrow(() -> new IllegalArgumentException("Post not found"));
        if (!post.getAuthor().equals(author) && !roleService.userHasRole(author, "admin")) {
            throw new IllegalArgumentException("User not authorized to delete this post");
        }
        postRepository.delete(post);
    }
}