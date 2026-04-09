package com.example.scheduler.scheduler;

import com.example.scheduler.entity.EmailJob;
import com.example.scheduler.entity.EmailStatus;
import com.example.scheduler.repository.EmailJobRepository;
import com.example.scheduler.service.EmailSenderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class EmailJobScheduler {

    @Autowired
    private EmailJobRepository emailJobRepository;

    @Autowired
    private EmailSenderService emailSenderService;

    @Scheduled(fixedRate = 5000)
    public void processJobs(){
        List<EmailJob> jobs = emailJobRepository.findByStatusAndScheduledTimeBefore(
                EmailStatus.PENDING,
                LocalDateTime.now()
        );

        for(EmailJob job: jobs){
            try{
                emailSenderService.sendEmail(
                        job.getEmail(),
                        job.getSubject(),
                        job.getBody()
                );

                job.setStatus(EmailStatus.SENT);
            }catch(Exception e){
                job.setRetryCount(job.getRetryCount() + 1);

                if(job.getRetryCount() >= 3){
                    job.setStatus(EmailStatus.FAILED);
                }else{
                    job.setStatus(EmailStatus.PENDING);
                }
            }
            emailJobRepository.save(job);
        }
    }

}
