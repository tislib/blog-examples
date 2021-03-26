package net.tislib.blog.examples.rabbitmqwebflux.controller.advanced;

import net.tislib.blog.examples.rabbitmqwebflux.service.UserMessageService;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

/**
 * group to user approach
 */
@RestController
@RequestMapping("/groups/{groupId}/messages")
public class GroupMessageController {

    private final UserMessageService userMessageService;

    public GroupMessageController(UserMessageService userMessageService) {
        this.userMessageService = userMessageService;
    }

    @PostMapping
    public Mono<Void> sendMessage(@PathVariable long groupId, @RequestBody String content) {
        return userMessageService.sendMessage(groupId, content);
    }

}
