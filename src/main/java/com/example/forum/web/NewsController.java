package com.example.forum.web;

import com.example.forum.service.NewsService;
import com.example.forum.web.dto.CommentCreateRequest;
import com.example.forum.web.dto.NewsCommentResponse;
import com.example.forum.web.dto.NewsCreateRequest;
import com.example.forum.web.dto.NewsItemResponse;
import jakarta.validation.Valid;
import java.security.Principal;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/news")
public class NewsController {

    private final NewsService newsService;

    public NewsController(NewsService newsService) {
        this.newsService = newsService;
    }

    @GetMapping
    public List<NewsItemResponse> listNews() {
        return newsService.listNews();
    }

    @GetMapping("/{id}")
    public NewsItemResponse getNews(@PathVariable Long id) {
        return newsService.getNews(id);
    }

    @PostMapping
    public NewsItemResponse createNews(@Valid @RequestBody NewsCreateRequest request, Principal principal) {
        return newsService.createNews(request, principal.getName());
    }

    @PostMapping("/{id}/comments")
    public NewsCommentResponse addComment(@PathVariable Long id,
                                          @Valid @RequestBody CommentCreateRequest request,
                                          Principal principal) {
        return newsService.addComment(id, request.parentCommentId(), request.content(), principal.getName());
    }

    @DeleteMapping("/{id}")
    public void deleteNews(@PathVariable Long id, Principal principal) {
        newsService.deleteNews(id, principal.getName());
    }
}
