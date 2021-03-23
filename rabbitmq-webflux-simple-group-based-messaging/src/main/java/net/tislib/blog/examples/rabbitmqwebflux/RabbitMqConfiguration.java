package net.tislib.blog.examples.rabbitmqwebflux;

import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Queue;
import org.springframework.boot.autoconfigure.amqp.RabbitProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Mono;
import reactor.rabbitmq.RabbitFlux;
import reactor.rabbitmq.Receiver;
import reactor.rabbitmq.ReceiverOptions;
import reactor.rabbitmq.Sender;
import reactor.rabbitmq.SenderOptions;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

@Configuration
public class RabbitMqConfiguration {

    public static final String QUEUE = "reactor.rabbitmq.spring.boot";
    private static final Logger LOGGER = LoggerFactory.getLogger(RabbitMqConfiguration.class);

    private final AmqpAdmin amqpAdmin;

    public RabbitMqConfiguration(AmqpAdmin amqpAdmin) {
        this.amqpAdmin = amqpAdmin;
    }

    // the mono for connection, it is cached to re-use the connection across sender and receiver instances
    // this should work properly in most cases
    @Bean
    Mono<Connection> rabbitMqConnection(RabbitProperties rabbitProperties) {
        ConnectionFactory connectionFactory = new ConnectionFactory();
        connectionFactory.setHost(rabbitProperties.getHost());
        connectionFactory.setPort(rabbitProperties.getPort());
        connectionFactory.setUsername(rabbitProperties.getUsername());
        connectionFactory.setPassword(rabbitProperties.getPassword());
        return Mono.fromCallable(() -> connectionFactory.newConnection("reactor-rabbit")).cache();
    }

    @Bean
    Sender sender(Mono<Connection> connectionMono) {
        return RabbitFlux.createSender(new SenderOptions().connectionMono(connectionMono));
    }

    @Bean
    Receiver receiver(Mono<Connection> connectionMono) {
        return RabbitFlux.createReceiver(new ReceiverOptions().connectionMono(connectionMono));
    }

    @PostConstruct
    public void init() {
        amqpAdmin.declareQueue(new Queue(QUEUE, false, false, true));
    }

//    @PreDestroy
//    public void close(Mono<Connection> rabbitMqConnectionMono) throws Exception {
//        rabbitMqConnectionMono.block().close();
//    }

}
