package com.example.g_time.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.PreUpdate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SystemConfig {
    private Long id;

    private String configKey;

    private String configValue;

    private LocalDateTime updatedAt = LocalDateTime.now();

    @PreUpdate
    private void setLastUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
