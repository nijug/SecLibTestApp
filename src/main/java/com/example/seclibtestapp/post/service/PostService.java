package com.example.seclibtestapp.post.service;

import com.example.seclibtestapp.post.model.Post;
import com.example.seclibtestapp.post.repository.PostRepository;
import com.seclib.userRoles.service.DefaultRoleService;
import com.seclib.validator.TextSanitizer;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class PostService {

    private final PostRepository postRepository;
    private final DefaultRoleService roleService;
    private final TextSanitizer textSanitizer;
    private final MarkdownService markdownService;

    public PostService(PostRepository postRepository, DefaultRoleService roleService, TextSanitizer textSanitizer, MarkdownService markdownService) {
        this.postRepository = postRepository;
        this.roleService = roleService;
        this.textSanitizer = textSanitizer;
        this.markdownService = markdownService;
    }

    public Post createPost(String title, String content, String author) {
        Post newPost = new Post();
        newPost.setTitle(textSanitizer.sanitize(title));
        newPost.setContent(textSanitizer.sanitize(content));
        newPost.setAuthor(author);

        return postRepository.save(newPost);
    }

    public Iterable<Post> getAllPosts() {
        return postRepository.findAll();
    }

    public Optional<Post> getPostById(Long id) {
        Optional<Post> dupa= postRepository.findById(id);
        System.out.println(dupa.get().getContent());
        return dupa;
    }

    public Post updatePost(Long id, String title, String content, String author) {
        Post postToUpdate = getPostById(id).orElseThrow(() -> new IllegalArgumentException("Post not found"));
        System.out.println(postToUpdate.getAuthor());
        System.out.println(author);
        if (!postToUpdate.getAuthor().equals(author) && !roleService.userHasRole(author, "admin")) {
            throw new IllegalArgumentException("User not authorized to update this post");
        }
        postToUpdate.setTitle(textSanitizer.sanitize(title));
        postToUpdate.setContent(textSanitizer.sanitize(content));
        return postRepository.save(postToUpdate);
    }

    public void deletePost(Long id, String author) {
        Post postToDelete = getPostById(id).orElseThrow(() -> new IllegalArgumentException("Post not found"));
        if (!postToDelete.getAuthor().equals(author) && !roleService.userHasRole(author, "admin")) {
            throw new IllegalArgumentException("User not authorized to delete this post");
        }
        postRepository.delete(postToDelete);
    }
}