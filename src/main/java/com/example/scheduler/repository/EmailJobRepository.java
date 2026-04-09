package com.example.scheduler.repository;

import com.example.scheduler.entity.EmailJob;
import com.example.scheduler.entity.EmailStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface EmailJobRepository extends JpaRepository<EmailJob, Long> {

    List<EmailJob> findByStatusAndScheduledTimeBefore(EmailStatus status, LocalDateTime time);
}
