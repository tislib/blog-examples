package net.tislib.blog.examples.rabbitmqwebflux.service.impl;

import com.rabbitmq.client.Delivery;
import net.tislib.blog.examples.rabbitmqwebflux.service.UserMessageService;
import net.tislib.blog.examples.rabbitmqwebflux.service.UserService;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.rabbitmq.BindingSpecification;
import reactor.rabbitmq.ConsumeOptions;
import reactor.rabbitmq.ExchangeSpecification;
import reactor.rabbitmq.OutboundMessage;
import reactor.rabbitmq.QueueSpecification;
import reactor.rabbitmq.Receiver;
import reactor.rabbitmq.Sender;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

@Service
public class UserMessageServiceImpl implements UserMessageService {

    private final Sender sender;
    private final Receiver receiver;
    private final UserService userService;

    private final String topicName = "user-group-message";
    private final Map<String, Sinks.Many<Delivery>> groupConsumerMap = new ConcurrentHashMap<>();

    public UserMessageServiceImpl(Sender sender, Receiver receiver, UserService userService) {
        this.sender = sender;
        this.receiver = receiver;
        this.userService = userService;
    }

    @Override
    public Mono<Void> sendMessage(long groupId, String content) {
        String routingKey = topicName + "-" + groupId;

        OutboundMessage message = new OutboundMessage(topicName, routingKey, content.getBytes());

        return sender.declareExchange(ExchangeSpecification.exchange()
                .name(topicName)
                .durable(true)
                .type("topic"))
                .flatMap(item -> sender.send(Mono.fromSupplier(() -> message)));
    }

    @Override
    @SuppressWarnings("ALL")
    public Flux<String> receive(long userId, Duration timeout, Integer maxMessageCount) {
        Flux<Long> groupIds = userService.locateGroups(userId);

        Flux<String> result = groupIds.map(item -> getGroupReceiver(item))
                .flatMap(item -> item)
                .log("receiver-log", Level.FINER)
                .map(item -> new String(item.getBody()));

        if (timeout != null) {
            result = result.timeout(timeout);
        }

        if (maxMessageCount != null) {
            result = result.limitRequest(maxMessageCount);
        }

        return result;
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
        Sinks.Many<Delivery> sink = Sinks.many().multicast().onBackpressureBuffer();

        Mono<String> declareQueue = sender
                .declareQueue(QueueSpecification.queue())
                .log("declare-queue", Level.FINER)
                .flatMap(declareOk ->
                        sender.bindQueue(BindingSpecification.binding()
                                .queue(declareOk.getQueue())
                                .exchange(topicName)
                                .routingKey(routingKey)).map(bindOk -> declareOk.getQueue()))
                .log("bind-queue", Level.FINER);

        declareQueue.flatMapMany(queueName -> receiver.consumeAutoAck(queueName, new ConsumeOptions()))
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
