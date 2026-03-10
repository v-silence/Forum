package com.example.forum.service;

import com.example.forum.model.NewsComment;
import com.example.forum.model.NewsItem;
import com.example.forum.model.Role;
import com.example.forum.model.UserAccount;
import com.example.forum.repository.NewsCommentRepository;
import com.example.forum.repository.NewsItemRepository;
import com.example.forum.repository.UserRepository;
import com.example.forum.web.dto.NewsCommentResponse;
import com.example.forum.web.dto.NewsCreateRequest;
import com.example.forum.web.dto.NewsItemResponse;
import com.example.forum.web.dto.ForumPostResponse;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NewsService {

    private final NewsItemRepository newsItemRepository;
    private final NewsCommentRepository newsCommentRepository;
    private final UserRepository userRepository;

    public NewsService(NewsItemRepository newsItemRepository,
                       NewsCommentRepository newsCommentRepository,
                       UserRepository userRepository) {
        this.newsItemRepository = newsItemRepository;
        this.newsCommentRepository = newsCommentRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<NewsItemResponse> listNews() {
        return newsItemRepository.findAll().stream()
                .sorted(Comparator.comparing(NewsItem::getCreatedAt).reversed())
                .map(item -> new NewsItemResponse(item.getId(), item.getTitle(), item.getContent(), item.getCreatedAt(), author(item.getAuthor()), List.of()))
                .toList();
    }

    @Transactional(readOnly = true)
    public NewsItemResponse getNews(Long id) {
        NewsItem item = newsItemRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("News item not found"));
        List<NewsCommentResponse> roots = item.getComments().stream()
                .filter(comment -> comment.getParent() == null)
                .map(this::mapComment)
                .toList();
        return new NewsItemResponse(item.getId(), item.getTitle(), item.getContent(), item.getCreatedAt(), author(item.getAuthor()), roots);
    }

    @Transactional
    public NewsItemResponse createNews(NewsCreateRequest request, String username) {
        UserAccount author = getUser(username);
        if (author.getRoles().stream().noneMatch(role -> role == Role.ROLE_ADMIN || role == Role.ROLE_MODERATOR)) {
            throw new IllegalArgumentException("Only moderators and admins can create news");
        }
        NewsItem item = new NewsItem();
        item.setTitle(request.title());
        item.setContent(request.content());
        item.setAuthor(author);
        newsItemRepository.save(item);
        return getNews(item.getId());
    }

    @Transactional
    public NewsCommentResponse addComment(Long newsId, Long parentCommentId, String content, String username) {
        NewsItem item = newsItemRepository.findById(newsId)
                .orElseThrow(() -> new IllegalArgumentException("News item not found"));
        NewsComment comment = new NewsComment();
        comment.setNewsItem(item);
        comment.setAuthor(getUser(username));
        comment.setContent(content);
        if (parentCommentId != null) {
            NewsComment parent = newsCommentRepository.findById(parentCommentId)
                    .orElseThrow(() -> new IllegalArgumentException("Parent comment not found"));
            if (!parent.getNewsItem().getId().equals(newsId)) {
                throw new IllegalArgumentException("Parent comment belongs to another news item");
            }
            comment.setParent(parent);
        }
        newsCommentRepository.save(comment);
        return mapComment(comment);
    }

    @Transactional
    public void deleteNews(Long newsId, String username) {
        UserAccount user = getUser(username);
        ensureModerator(user);
        NewsItem item = newsItemRepository.findById(newsId)
                .orElseThrow(() -> new IllegalArgumentException("News item not found"));
        newsItemRepository.delete(item);
    }

    private UserAccount getUser(String username) {
        return userRepository.findByUsername(username).orElseThrow(() -> new IllegalArgumentException("User not found"));
    }

    private void ensureModerator(UserAccount user) {
        if (user.getRoles().stream().noneMatch(role -> role == Role.ROLE_ADMIN || role == Role.ROLE_MODERATOR)) {
            throw new IllegalArgumentException("Only moderators and admins can manage news");
        }
    }

    private NewsCommentResponse mapComment(NewsComment comment) {
        return new NewsCommentResponse(
                comment.getId(),
                comment.getContent(),
                comment.getCreatedAt(),
                author(comment.getAuthor()),
                comment.getParent() == null ? null : comment.getParent().getId(),
                comment.getReplies().stream().map(this::mapComment).toList()
        );
    }

    private ForumPostResponse.AuthorResponse author(UserAccount user) {
        return new ForumPostResponse.AuthorResponse(user.getId(), user.getUsername());
    }
}
