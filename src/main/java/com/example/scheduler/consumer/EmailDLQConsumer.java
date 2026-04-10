package com.example.scheduler.consumer;

import com.example.scheduler.dto.EmailMessage;
import com.example.scheduler.entity.EmailJob;
import com.example.scheduler.entity.EmailStatus;
import com.example.scheduler.repository.EmailJobRepository;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class EmailDLQConsumer {

    @Autowired
    private EmailJobRepository repository;

    @RabbitListener(queues = "email_dlq")
    public void handleFailedMessage(EmailMessage message) {

        System.out.println("Message moved to DLQ for: " + message.getEmail());

        EmailJob job = new EmailJob();
        job.setEmail(message.getEmail());
        job.setSubject(message.getSubject());
        job.setBody(message.getBody());
        job.setScheduledTime(message.getScheduledTime());

        job.setStatus(EmailStatus.FAILED);

        repository.save(job);
    }
}