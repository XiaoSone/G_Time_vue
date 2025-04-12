package com.example.g_time.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SystemLog {
    private Long id;

    private String logType;

    private String message;

    private LocalDateTime createdAt = LocalDateTime.now();
}
