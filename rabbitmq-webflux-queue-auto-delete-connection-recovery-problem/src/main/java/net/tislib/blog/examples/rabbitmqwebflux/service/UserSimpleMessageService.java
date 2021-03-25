package net.tislib.blog.examples.rabbitmqwebflux.service;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface UserSimpleMessageService {
    Mono<Void> sendMessage(long userId, String content);

    Flux<String> receive(long userId);
}
