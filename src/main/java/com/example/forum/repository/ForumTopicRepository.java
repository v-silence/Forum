package com.example.forum.repository;

import com.example.forum.model.ForumTopic;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ForumTopicRepository extends JpaRepository<ForumTopic, Long> {
    @Override
    @EntityGraph(attributePaths = {"author", "attachments"})
    java.util.List<ForumTopic> findAll();

    @Override
    @EntityGraph(attributePaths = {"author", "attachments", "posts", "posts.author"})
    java.util.Optional<ForumTopic> findById(Long id);
}
