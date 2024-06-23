package com.example.seclibtestapp.post.repository;

import com.example.seclibtestapp.post.model.Post;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostRepository extends JpaRepository<Post, Long> {
}