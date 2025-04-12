package com.example.g_time.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Stage {
    private Long id;
    private Long goalId;
    private Long parentStageId;
    private List<Stage> subStages;
    private String name;
    private String description;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date dueDate;

    private int status;
    private Integer sequence;

    public Stage(Long stageId, int newSequence) {
        this.id = stageId;
        this.sequence = newSequence;
    }
}
