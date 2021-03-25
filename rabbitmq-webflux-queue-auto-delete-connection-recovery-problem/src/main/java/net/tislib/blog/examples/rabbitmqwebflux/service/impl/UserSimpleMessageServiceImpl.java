package net.tislib.blog.examples.rabbitmqwebflux.service.impl;

import net.tislib.blog.examples.rabbitmqwebflux.service.UserSimpleMessageService;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.rabbitmq.OutboundMessage;
import reactor.rabbitmq.Receiver;
import reactor.rabbitmq.Sender;

import java.util.HashMap;

@Service
public class UserSimpleMessageServiceImpl implements UserSimpleMessageService {

    private final Sender sender;
    private final Receiver receiver;
    private final AmqpAdmin amqpAdmin;

    private final String topicName = "user-simple-message";

    public UserSimpleMessageServiceImpl(Sender sender, Receiver receiver, AmqpAdmin amqpAdmin) {
        this.sender = sender;
        this.receiver = receiver;
        this.amqpAdmin = amqpAdmin;
    }

    @Override
    public Mono<Void> sendMessage(long userId, String content) {
        String routingKey = topicName + "-" + userId;
        OutboundMessage message = new OutboundMessage(topicName, routingKey, content.getBytes());

        return sender.send(Mono.fromSupplier(() -> message));
    }

    @Override
    public Flux<String> receive(long userId) {
        final String routeKey = topicName + "-" + userId;
        String queueName = routeKey + "-" + System.nanoTime();

        amqpAdmin.declareQueue(new Queue(queueName, true, false, true));
        amqpAdmin.declareExchange(new TopicExchange(topicName));

        Binding binding = new Binding(queueName,
                Binding.DestinationType.QUEUE,
                topicName,
                routeKey,
                new HashMap<>());

        amqpAdmin.declareBinding(binding);

        return receiver.consumeAutoAck(queueName)
                .log()
                .map(item -> new String(item.getBody()));
    }
}
