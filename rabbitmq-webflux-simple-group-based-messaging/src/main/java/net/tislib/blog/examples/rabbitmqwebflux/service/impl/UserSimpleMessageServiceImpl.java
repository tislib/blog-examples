package net.tislib.blog.examples.rabbitmqwebflux.service.impl;

import net.tislib.blog.examples.rabbitmqwebflux.service.UserSimpleMessageService;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.rabbitmq.BindingSpecification;
import reactor.rabbitmq.ExchangeSpecification;
import reactor.rabbitmq.OutboundMessage;
import reactor.rabbitmq.QueueSpecification;
import reactor.rabbitmq.Receiver;
import reactor.rabbitmq.Sender;

import java.time.Duration;
import java.util.logging.Level;

@Service
public class UserSimpleMessageServiceImpl implements UserSimpleMessageService {

    private final Sender sender;
    private final Receiver receiver;

    private final String topicName = "user-simple-message";

    public UserSimpleMessageServiceImpl(Sender sender, Receiver receiver) {
        this.sender = sender;
        this.receiver = receiver;
    }

    @Override
    public Mono<Void> sendMessage(long userId, String content) {
        String routingKey = topicName + "-" + userId;

        OutboundMessage message = new OutboundMessage(topicName, routingKey, content.getBytes());

        return sender.declareExchange(ExchangeSpecification.exchange()
                .name(topicName)
                .durable(true)
                .type("topic"))
                .flatMap(item -> sender.send(Mono.fromSupplier(() -> message)));
    }

    @Override
    public Flux<String> receive(long userId, Duration timeout, Integer maxMessageCount) {
        final String routingKey = topicName + "-" + userId;

        Flux<String> result = sender
                .declareQueue(QueueSpecification.queue())
                .log("declare-queue", Level.FINER)
                .flatMap(declareOk ->
                        sender.bindQueue(BindingSpecification.binding()
                                .queue(declareOk.getQueue())
                                .exchange(topicName)
                                .routingKey(routingKey)).map(bindOk -> declareOk.getQueue()))
                .log("bind-queue", Level.FINER)
                .flatMapMany(receiver::consumeAutoAck)
                .map(item -> new String(item.getBody()));

        if (timeout != null) {
            result = result.timeout(timeout);
        }

        if (maxMessageCount != null) {
            result = result.limitRequest(maxMessageCount);
        }

        return result;
    }
}
