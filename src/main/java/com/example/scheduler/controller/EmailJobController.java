package com.example.scheduler.controller;

import com.example.scheduler.dto.EmailJobRequest;
import com.example.scheduler.entity.EmailJob;
import com.example.scheduler.service.EmailJobService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/emails")
public class EmailJobController {

    @Autowired
    private EmailJobService emailJobService;

    @PostMapping
    public void createJob(@Valid @RequestBody EmailJobRequest request){
        emailJobService.createJob(request);
    }

    @GetMapping
    public List<EmailJob> getAllJobs(){
        return emailJobService.getAllJobs();
    }

    @GetMapping("/{id}")
    public EmailJob getJobById(@PathVariable Long id){
        return emailJobService.getJobById(id);
    }

    @PostMapping("/{id}/retry")
    public EmailJob retryJob(@PathVariable Long id) {
        return emailJobService.retryJob(id);
    }

}
