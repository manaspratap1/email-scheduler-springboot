package com.example.scheduler.service;

import com.example.scheduler.dto.EmailJobRequest;
import com.example.scheduler.dto.EmailMessage;
import com.example.scheduler.entity.EmailJob;
import com.example.scheduler.entity.EmailStatus;
import com.example.scheduler.repository.EmailJobRepository;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class EmailJobService {

    @Autowired
    private EmailJobRepository emailJobRepository;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    public void createJob(EmailJobRequest request){
        EmailMessage message = new EmailMessage();

        message.setEmail(request.getEmail());
        message.setSubject(request.getSubject());
        message.setBody(request.getBody());

        if(request.getScheduledTime().isBefore(LocalDateTime.now())){
            message.setScheduledTime(LocalDateTime.now());
        }else{
            message.setScheduledTime(request.getScheduledTime());
        }

        rabbitTemplate.convertAndSend(
                "email_exchange",
                "email_routing",
                message
        );

        System.out.println("Message send ho gya bhai");
    }

//    public EmailJob createJob(EmailJobRequest request){
//        EmailJob job = new EmailJob();
//
//        job.setEmail(request.getEmail());
//        job.setSubject(request.getSubject());
//        job.setBody(request.getBody());
//
//        if(request.getScheduledTime().isBefore(LocalDateTime.now())){
//            job.setScheduledTime(LocalDateTime.now());
//        }else{
//            job.setScheduledTime(request.getScheduledTime());
//        }
//
//        job.setStatus(EmailStatus.PENDING);
//        job.setRetryCount(0);
//
//        return emailJobRepository.save(job);
//    }

    public List<EmailJob> getAllJobs() {
        return emailJobRepository.findAll();
    }

    public EmailJob getJobById(Long id) {
        return emailJobRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Job not found"));
    }

    public EmailJob retryJob(Long id) {

        EmailJob job = emailJobRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Job not found"));

        job.setStatus(EmailStatus.PENDING);
        job.setRetryCount(0);
        job.setScheduledTime(LocalDateTime.now());

        return emailJobRepository.save(job);
    }
}
