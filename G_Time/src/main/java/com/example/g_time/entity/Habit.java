package com.example.g_time.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.PreUpdate;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Habit {
    private Long id;

    private Long userId;

    private String name;

    private String description;

    private Byte frequency; // 1:每日, 2:每周, 3:每月

    private String category;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;

    private Integer streak = 0;

    private Integer maxStreak = 0;

    private boolean status = true;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;

    @PreUpdate
    private void setLastUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
