package com.example.g_time.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserSetting {
    private Long id;

    private User user;

    private Boolean reminderEnabled = true; // 默认启用提醒

    private LocalTime notificationTime;

    private String timezone = "UTC"; // 默认时区

    private String language = "zh-CN"; // 默认语言
}
