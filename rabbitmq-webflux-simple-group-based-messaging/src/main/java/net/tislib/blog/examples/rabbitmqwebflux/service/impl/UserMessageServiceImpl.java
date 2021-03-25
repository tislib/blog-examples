package net.tislib.blog.examples.rabbitmqwebflux.service.impl;

import com.rabbitmq.client.Delivery;
import net.tislib.blog.examples.rabbitmqwebflux.service.UserMessageService;
import net.tislib.blog.examples.rabbitmqwebflux.service.UserService;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.rabbitmq.ConsumeOptions;
import reactor.rabbitmq.OutboundMessage;
import reactor.rabbitmq.Receiver;
import reactor.rabbitmq.Sender;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

@Service
public class UserMessageServiceImpl implements UserMessageService {

    private final Sender sender;
    private final Receiver receiver;
    private final AmqpAdmin amqpAdmin;
    private final UserService userService;

    private final String topicName = "user-group-message";
    private final Map<String, Sinks.Many<Delivery>> groupConsumerMap = new ConcurrentHashMap<>();

    public UserMessageServiceImpl(Sender sender, Receiver receiver, AmqpAdmin amqpAdmin, UserService userService) {
        this.sender = sender;
        this.receiver = receiver;
        this.amqpAdmin = amqpAdmin;
        this.userService = userService;
    }

    @Override
    public Mono<Void> sendMessage(long groupId, String content) {
        String routingKey = topicName + "-" + groupId;

        OutboundMessage message = new OutboundMessage(topicName, routingKey, content.getBytes());

        return sender.send(Mono.fromSupplier(() -> message));
    }

    @Override
    @SuppressWarnings("ALL")
    public Flux<String> receive(long userId) {
        Flux<Long> groupIds = userService.locateGroups(userId);

        return groupIds.map(item -> getGroupReceiver(item))
                .flatMap(item -> item)
                .log("receiver-log", Level.FINER)
                .map(item -> new String(item.getBody()));
    }

    private Flux<Delivery> getGroupReceiver(Long groupId) {
        String routingKey = topicName + "-" + groupId;

        if (!groupConsumerMap.containsKey(routingKey)) {
            registerGroupReceiver(routingKey);
        }

        return groupConsumerMap.get(routingKey)
                .asFlux()
                .log("flux-log", Level.FINER);
    }

    private synchronized void registerGroupReceiver(String routingKey) {
        String queueName = routingKey + "-" + System.nanoTime();

        amqpAdmin.declareExchange(new TopicExchange(topicName));

        amqpAdmin.declareQueue(new Queue(queueName, true, false, true));

        Binding binding = new Binding(queueName,
                Binding.DestinationType.QUEUE,
                topicName,
                routingKey,
                new HashMap<>());

        amqpAdmin.declareBinding(binding);

        Sinks.Many<Delivery> sink = Sinks.many().multicast().onBackpressureBuffer();

        receiver.consumeAutoAck(queueName, new ConsumeOptions())
                .log("broker-log", Level.FINER)
                .doOnNext(sink::tryEmitNext)
                .doOnComplete(sink::tryEmitComplete)
                .doOnError(sink::tryEmitError)
                .subscribe();

        groupConsumerMap.put(routingKey, sink);

        sink.asFlux().doOnTerminate(() -> {
            groupConsumerMap.remove(routingKey, sink);
        }).subscribe();
    }
}
