package net.tislib.blog.examples.rabbitmqwebflux;

import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import org.springframework.boot.autoconfigure.amqp.RabbitProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Mono;
import reactor.rabbitmq.RabbitFlux;
import reactor.rabbitmq.Receiver;
import reactor.rabbitmq.ReceiverOptions;
import reactor.rabbitmq.Sender;
import reactor.rabbitmq.SenderOptions;

import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicReference;

@Configuration
public class RabbitMqConfiguration {

    // the mono for connection, it is cached to re-use the connection across sender and receiver instances
    // this should work properly in most cases
    @Bean
    Mono<Connection> rabbitMqConnection(RabbitProperties rabbitProperties) {
        ConnectionFactory connectionFactory = new ConnectionFactory();
        connectionFactory.setHost(rabbitProperties.getHost());
        connectionFactory.setPort(rabbitProperties.getPort());
        connectionFactory.useNio();

        connectionFactory.setAutomaticRecoveryEnabled(false);
        connectionFactory.setTopologyRecoveryEnabled(false);

        connectionFactory.setUsername(rabbitProperties.getUsername());
        connectionFactory.setPassword(rabbitProperties.getPassword());

        Callable<Connection> connectionProvider = () -> connectionFactory.newConnection("reactor-rabbit");

        AtomicReference<Connection> cachedConnection = new AtomicReference<>();

        return Mono.fromCallable(() -> {
            if (cachedConnection.get() == null || !cachedConnection.get().isOpen()) {
                cachedConnection.set(connectionProvider.call());
            }

            return cachedConnection.get();
        });
    }

    @Bean
    Sender sender(Mono<Connection> connectionMono) {
        return RabbitFlux.createSender(new SenderOptions()
                .connectionMono(connectionMono));
    }

    @Bean
    Receiver receiver(Mono<Connection> connectionMono) {
        return RabbitFlux.createReceiver(new ReceiverOptions()
                .connectionMono(connectionMono));
    }

//    @PreDestroy
//    public void close(Mono<Connection> rabbitMqConnectionMono) throws Exception {
//        rabbitMqConnectionMono.block().close();
//    }

}
