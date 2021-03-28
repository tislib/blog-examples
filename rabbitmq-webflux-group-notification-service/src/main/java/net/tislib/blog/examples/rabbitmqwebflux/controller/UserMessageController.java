package net.tislib.blog.examples.rabbitmqwebflux.controller;

import net.tislib.blog.examples.rabbitmqwebflux.service.UserMessageService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.time.Duration;

/**
 * group to user approach
 */
@RestController
@RequestMapping("/users/{userId}/messages")
public class UserMessageController {

    private final UserMessageService userMessageService;

    public UserMessageController(UserMessageService userMessageService) {
        this.userMessageService = userMessageService;
    }

    @GetMapping(produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> receive(@PathVariable long userId,
                                @RequestParam(required = false) Integer timeout,
                                @RequestParam(required = false) Integer maxMessageCount) {
        return userMessageService.receive(userId, timeout == null ? null : Duration.ofMillis(timeout), maxMessageCount);
    }

}
