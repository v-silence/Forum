package com.example.forum.service;

import com.example.forum.model.Attachment;
import com.example.forum.model.ForumPost;
import com.example.forum.model.ForumTopic;
import com.example.forum.model.Role;
import com.example.forum.model.UserAccount;
import com.example.forum.repository.ForumPostRepository;
import com.example.forum.repository.ForumTopicRepository;
import com.example.forum.repository.UserRepository;
import com.example.forum.web.dto.AttachmentResponse;
import com.example.forum.web.dto.ForumPostResponse;
import com.example.forum.web.dto.ForumTopicResponse;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class ForumService {

    private final ForumTopicRepository forumTopicRepository;
    private final ForumPostRepository forumPostRepository;
    private final UserRepository userRepository;
    private final StorageService storageService;

    public ForumService(ForumTopicRepository forumTopicRepository,
                        ForumPostRepository forumPostRepository,
                        UserRepository userRepository,
                        StorageService storageService) {
        this.forumTopicRepository = forumTopicRepository;
        this.forumPostRepository = forumPostRepository;
        this.userRepository = userRepository;
        this.storageService = storageService;
    }

    @Transactional(readOnly = true)
    public List<ForumTopicResponse> listTopics() {
        return forumTopicRepository.findAll().stream().map(topic -> new ForumTopicResponse(
                topic.getId(),
                topic.getTitle(),
                topic.getContent(),
                topic.getCreatedAt(),
                author(topic.getAuthor()),
                topic.getAttachments().stream().map(this::attachment).toList(),
                List.of()
        )).toList();
    }

    @Transactional(readOnly = true)
    public ForumTopicResponse getTopic(Long id) {
        ForumTopic topic = forumTopicRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Topic not found"));
        return new ForumTopicResponse(
                topic.getId(),
                topic.getTitle(),
                topic.getContent(),
                topic.getCreatedAt(),
                author(topic.getAuthor()),
                topic.getAttachments().stream().map(this::attachment).toList(),
                topic.getPosts().stream().map(this::post).toList()
        );
    }

    @Transactional
    public ForumTopicResponse createTopic(String title, String content, MultipartFile[] files, String username) {
        UserAccount author = getUser(username);
        ForumTopic topic = new ForumTopic();
        topic.setTitle(title);
        topic.setContent(content);
        topic.setAuthor(author);
        forumTopicRepository.save(topic);
        attachFiles(topic, files, author);
        return getTopic(topic.getId());
    }

    @Transactional
    public ForumPostResponse addPost(Long topicId, String content, String username) {
        ForumTopic topic = forumTopicRepository.findById(topicId)
                .orElseThrow(() -> new IllegalArgumentException("Topic not found"));
        ForumPost post = new ForumPost();
        post.setTopic(topic);
        post.setAuthor(getUser(username));
        post.setContent(content);
        forumPostRepository.save(post);
        return post(post);
    }

    @Transactional
    public void deleteTopic(Long topicId, String username) {
        UserAccount user = getUser(username);
        ensureModerator(user);
        ForumTopic topic = forumTopicRepository.findById(topicId)
                .orElseThrow(() -> new IllegalArgumentException("Topic not found"));
        List<Attachment> attachments = List.copyOf(topic.getAttachments());
        forumTopicRepository.delete(topic);
        attachments.forEach(storageService::delete);
    }

    private void attachFiles(ForumTopic topic, MultipartFile[] files, UserAccount author) {
        if (files == null) {
            return;
        }
        for (MultipartFile file : files) {
            if (file == null || file.isEmpty()) {
                continue;
            }
            Attachment attachment = storageService.storeTopicFile(file, author);
            attachment.setTopic(topic);
            topic.getAttachments().add(attachment);
        }
    }

    private UserAccount getUser(String username) {
        return userRepository.findByUsername(username).orElseThrow(() -> new IllegalArgumentException("User not found"));
    }

    private void ensureModerator(UserAccount user) {
        if (user.getRoles().stream().noneMatch(role -> role == Role.ROLE_ADMIN || role == Role.ROLE_MODERATOR)) {
            throw new IllegalArgumentException("Only moderators and admins can delete topics");
        }
    }

    private ForumPostResponse post(ForumPost post) {
        return new ForumPostResponse(post.getId(), post.getContent(), post.getCreatedAt(), author(post.getAuthor()));
    }

    private AttachmentResponse attachment(Attachment attachment) {
        return new AttachmentResponse(
                attachment.getId(),
                attachment.getOriginalFilename(),
                attachment.getContentType(),
                attachment.getSize(),
                attachment.getCreatedAt(),
                "/api/files/" + attachment.getId()
        );
    }

    private ForumPostResponse.AuthorResponse author(UserAccount user) {
        return new ForumPostResponse.AuthorResponse(user.getId(), user.getUsername());
    }
}
