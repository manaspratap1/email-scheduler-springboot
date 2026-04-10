package com.example.scheduler.dto;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;

@Getter
@Setter
public class EmailMessage{

    private String email;

    private String subject;

    private String body;

    private LocalDateTime scheduledTime;
}
