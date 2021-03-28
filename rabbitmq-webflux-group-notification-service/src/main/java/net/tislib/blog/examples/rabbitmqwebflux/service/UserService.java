package net.tislib.blog.examples.rabbitmqwebflux.service;

import reactor.core.publisher.Flux;

public interface UserService {
    Flux<Long> locateGroups(long userId);
}
