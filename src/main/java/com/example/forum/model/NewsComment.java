package com.example.forum.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.PrePersist;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
public class NewsComment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private NewsItem newsItem;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private UserAccount author;

    @ManyToOne(fetch = FetchType.LAZY)
    private NewsComment parent;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("createdAt ASC")
    private List<NewsComment> replies = new ArrayList<>();

    @Column(nullable = false, length = 3000)
    private String content;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    void onCreate() {
        createdAt = Instant.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public NewsItem getNewsItem() { return newsItem; }
    public void setNewsItem(NewsItem newsItem) { this.newsItem = newsItem; }
    public UserAccount getAuthor() { return author; }
    public void setAuthor(UserAccount author) { this.author = author; }
    public NewsComment getParent() { return parent; }
    public void setParent(NewsComment parent) { this.parent = parent; }
    public List<NewsComment> getReplies() { return replies; }
    public void setReplies(List<NewsComment> replies) { this.replies = replies; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
