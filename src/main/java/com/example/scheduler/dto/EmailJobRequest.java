package com.example.scheduler.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class EmailJobRequest {

    @Email(message = "Invalid email format")
    @NotBlank(message = "Email is required")
    private String email;

    @NotBlank(message = "Subject can not be blank")
    private String subject;

    @NotBlank(message = "Body is required")
    private String body;

    @NotNull(message = "Scheduled time is required")
    private LocalDateTime scheduledTime;
}
