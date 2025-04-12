package com.example.g_time.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserPoint {
    private Long id;

    private User user;

    private Integer points;

    private String reason;

    private LocalDateTime createdAt = LocalDateTime.now();
}
