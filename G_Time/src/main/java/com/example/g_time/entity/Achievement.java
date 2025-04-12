package com.example.g_time.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Achievement {
    private Long id;

    private User user;

    private String title;

    private String description;

    private LocalDateTime earnedAt = LocalDateTime.now();
}
