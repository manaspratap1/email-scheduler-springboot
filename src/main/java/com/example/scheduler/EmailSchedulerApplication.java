package com.example.scheduler;

import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableRabbit
@SpringBootApplication
public class EmailSchedulerApplication {

    public static void main(String[] args) {
        SpringApplication.run(EmailSchedulerApplication.class, args);
    }

}
