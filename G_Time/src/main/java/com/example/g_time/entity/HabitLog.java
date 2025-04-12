package com.example.g_time.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HabitLog {
    private Long id;

    private Long habitId;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate loggedAt;

    private Boolean isCompleted;

}
