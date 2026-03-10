package com.example.forum.repository;

import com.example.forum.model.NewsComment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NewsCommentRepository extends JpaRepository<NewsComment, Long> {
}
