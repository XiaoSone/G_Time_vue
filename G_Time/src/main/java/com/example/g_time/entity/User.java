package com.example.g_time.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.PreUpdate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    private Long id;

    private String username;

    private String password;

    private String email;

    private String phone;

    private String intro;

    private String avatar;

    private Byte status = 1; // 默认值为 1（正常）

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;

    private byte isAdmin = 0;

    @PreUpdate
    private void setLastUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}

