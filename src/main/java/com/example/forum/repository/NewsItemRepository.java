package com.example.forum.repository;

import com.example.forum.model.NewsItem;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NewsItemRepository extends JpaRepository<NewsItem, Long> {
    @Override
    @EntityGraph(attributePaths = {"author"})
    List<NewsItem> findAll();

    @Override
    @EntityGraph(attributePaths = {"author", "comments", "comments.author", "comments.parent", "comments.replies", "comments.replies.author"})
    Optional<NewsItem> findById(Long id);
}
