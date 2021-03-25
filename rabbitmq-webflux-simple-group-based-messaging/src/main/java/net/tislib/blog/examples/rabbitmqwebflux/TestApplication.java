//package net.tislib.blog.examples.rabbitmqwebflux;
//
//import net.tislib.blog.examples.rabbitmqwebflux.service.UserMessageService;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.CommandLineRunner;
//import org.springframework.boot.SpringApplication;
//import org.springframework.boot.autoconfigure.SpringBootApplication;
//import reactor.core.publisher.EmitterProcessor;
//import reactor.rabbitmq.Receiver;
//import reactor.rabbitmq.Sender;
//
//import java.util.concurrent.CompletableFuture;
//
//@SpringBootApplication
//public class TestApplication implements CommandLineRunner {
//
//    @Autowired
//    private Receiver receiver;
//
//    @Autowired
//    private Sender sender;
//
//    @Autowired
//    UserMessageService userMessageService;
//
//    public static void main(String[] args) {
//        SpringApplication.run(TestApplication.class, args);
//    }
//
//    @Override
//    public void run(String... args) throws Exception {
//        CompletableFuture.runAsync(() -> {
//            while (true) {
//                userMessageService.sendMessage(1L, "hello world").subscribe();
//
//                try {
//                    Thread.sleep(1000);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//            }
//        });
//
//        receiveMessages();
//    }
//
//    private void receiveMessages() {
//        try {
//            Thread.sleep(1000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//        System.out.println("receiving events");
//
//        userMessageService.receive(1L)
//                .doOnTerminate(this::receiveMessages)
//                .subscribe(msg -> {
//                    System.out.println("Message received: " + msg);
//                });
//    }
//}
