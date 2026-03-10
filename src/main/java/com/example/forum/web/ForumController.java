package com.example.forum.web;

import com.example.forum.service.ForumService;
import com.example.forum.web.dto.ForumPostResponse;
import com.example.forum.web.dto.ForumTopicResponse;
import jakarta.validation.constraints.NotBlank;
import java.security.Principal;
import java.util.List;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/forum")
@Validated
public class ForumController {

    private final ForumService forumService;

    public ForumController(ForumService forumService) {
        this.forumService = forumService;
    }

    @GetMapping("/topics")
    public List<ForumTopicResponse> listTopics() {
        return forumService.listTopics();
    }

    @GetMapping("/topics/{id}")
    public ForumTopicResponse getTopic(@PathVariable Long id) {
        return forumService.getTopic(id);
    }

    @PostMapping(value = "/topics", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ForumTopicResponse createTopic(@RequestParam @NotBlank String title,
                                          @RequestParam @NotBlank String content,
                                          @RequestParam(required = false) MultipartFile[] files,
                                          Principal principal) {
        return forumService.createTopic(title, content, files, principal.getName());
    }

    @PostMapping("/topics/{id}/posts")
    public ForumPostResponse createPost(@PathVariable Long id,
                                        @RequestParam @NotBlank String content,
                                        Principal principal) {
        return forumService.addPost(id, content, principal.getName());
    }

    @DeleteMapping("/topics/{id}")
    public void deleteTopic(@PathVariable Long id, Principal principal) {
        forumService.deleteTopic(id, principal.getName());
    }
}
