package net.tislib.blog.examples.rabbitmqwebflux.service;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;

public interface UserMessageService {
    Mono<Void> sendMessage(long groupId, String content);

    Flux<String> receive(long userId, Duration timeout, Integer maxMessageCount);
}
