//package com.example.scheduler.config;
//
//import org.springframework.boot.CommandLineRunner;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.amqp.rabbit.core.RabbitTemplate;
//
//@Configuration
//public class TestRunner {
//
//    @Bean
//    public CommandLineRunner testRabbit(RabbitTemplate rabbitTemplate) {
//        return args -> {
//            System.out.println("Sending test message...");
//
//            rabbitTemplate.convertAndSend(
//                    "email_exchange",
//                    "email_routing",
//                    "hello from spring"
//            );
//        };
//    }
//}