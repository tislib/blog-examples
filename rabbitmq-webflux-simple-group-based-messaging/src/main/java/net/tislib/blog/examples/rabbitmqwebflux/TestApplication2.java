//package net.tislib.blog.examples.rabbitmqwebflux;
//
//import net.tislib.blog.examples.rabbitmqwebflux.service.UserSimpleMessageService;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.CommandLineRunner;
//import org.springframework.boot.SpringApplication;
//import org.springframework.boot.autoconfigure.SpringBootApplication;
//import reactor.core.publisher.EmitterProcessor;
//import reactor.core.publisher.Sinks;
//
//import java.util.concurrent.CompletableFuture;
//
//@SpringBootApplication
//public class TestApplication2 implements CommandLineRunner {
//
//    @Autowired
//    UserSimpleMessageService userSimpleMessageService;
//
//    public static void main(String[] args) {
//        SpringApplication.run(TestApplication2.class, args);
//    }
//
//    @Override
//    public void run(String... args) {
//        CompletableFuture.runAsync(() -> {
//            while (true) {
//                userSimpleMessageService.sendMessage(1L, "hello world").subscribe();
//
//                try {
//                    Thread.sleep(1000);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//            }
//        });
//
//        Sinks.many().multicast().onBackpressureBuffer();
//
//        userSimpleMessageService.receive(1L)
////                .subscribeWith(EmitterProcessor.create())
//                .subscribe(msg -> {
//                    System.out.println("Message received: " + msg);
//                });
//    }
//}
