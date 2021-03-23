package net.tislib.blog.examples.rabbitmqwebflux.controller;

import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.rabbitmq.OutboundMessage;
import reactor.rabbitmq.Receiver;
import reactor.rabbitmq.Sender;

import java.util.HashMap;

@RestController
@RequestMapping("/simple/users/{userId}/messages")
public class UserSimpleMessagesController {

    private final Sender sender;
    private final Receiver receiver;
    private final AmqpAdmin amqpAdmin;

    private String topicName = "user-simple-message";

    public UserSimpleMessagesController(Sender sender, Receiver receiver, AmqpAdmin amqpAdmin) {
        this.sender = sender;
        this.receiver = receiver;
        this.amqpAdmin = amqpAdmin;
    }

    @PostMapping
    public Mono<Void> sendMessage(@PathVariable long userId, @RequestBody String content) {
        String routingKey = topicName + "-" + userId;
        OutboundMessage message = new OutboundMessage(topicName, routingKey, content.getBytes());

        return sender.send(Mono.fromSupplier(() -> message));
    }

    @GetMapping(produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> receive(@PathVariable long userId) {
        String queueName = topicName + "-" + userId + "-" + System.nanoTime();

        amqpAdmin.declareQueue(new Queue(queueName));

        amqpAdmin.declareBinding(new Binding(queueName,
                Binding.DestinationType.QUEUE,
                topicName,
                topicName + "-" + userId,
                new HashMap<>()));

        return receiver.consumeAutoAck(queueName)
                .log()
                .map(item -> {
                    return new String(item.getBody());
                });
    }

}
