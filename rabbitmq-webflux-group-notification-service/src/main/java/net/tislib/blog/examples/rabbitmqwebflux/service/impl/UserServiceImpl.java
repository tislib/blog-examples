package net.tislib.blog.examples.rabbitmqwebflux.service.impl;

import net.tislib.blog.examples.rabbitmqwebflux.service.UserService;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Service
public class UserServiceImpl implements UserService {
    @Override
    public Flux<Long> locateGroups(long userId) {
        if (userId == 1) {
            return Flux.just(1L, 2L, 3L);
        } else {
            return Flux.just(4L, 5L, 6L);
        }
    }
}
