package com.example.scheduler.consumer;

import com.example.scheduler.dto.EmailMessage;
import com.example.scheduler.entity.EmailJob;
import com.example.scheduler.entity.EmailStatus;
import com.example.scheduler.repository.EmailJobRepository;
import com.example.scheduler.service.EmailSenderService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class EmailConsumer {

    @Autowired
    private EmailSenderService emailSenderService;

    @Autowired
    private EmailJobRepository emailJobRepository;

    @RabbitListener(queues = "email_queue")
    public void consume(EmailMessage message){
        System.out.println("Received message for "+ message.getEmail());

        EmailJob job = new EmailJob();

        job.setEmail(message.getEmail());
        job.setSubject(message.getSubject());
        job.setBody(message.getBody());
        job.setScheduledTime(message.getScheduledTime());

        try{
            emailSenderService.sendEmail(
                    message.getEmail(),
                    message.getSubject(),
                    message.getBody()
            );
            job.setStatus(EmailStatus.SENT);
            emailJobRepository.save(job);
        } catch (Exception e) {
            throw new RuntimeException("Email sending failed");
        }

    }
}
