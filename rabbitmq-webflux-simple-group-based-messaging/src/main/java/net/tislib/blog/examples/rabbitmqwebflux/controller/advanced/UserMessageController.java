package net.tislib.blog.examples.rabbitmqwebflux.controller.advanced;

import net.tislib.blog.examples.rabbitmqwebflux.service.UserSimpleMessageService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * group to user approach
 */
@RestController
@RequestMapping("/users/{userId}/messages")
public class UserMessageController {

    private final UserSimpleMessageService userSimpleMessageService;

    public UserMessageController(UserSimpleMessageService userSimpleMessageService) {
        this.userSimpleMessageService = userSimpleMessageService;
    }

    @PostMapping
    public Mono<Void> sendMessage(@PathVariable long userId, @RequestBody String content) {
        return userSimpleMessageService.sendMessage(userId, content);
    }

    @GetMapping(produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> receive(@PathVariable long userId) {
        return userSimpleMessageService.receive(userId);
    }

}
