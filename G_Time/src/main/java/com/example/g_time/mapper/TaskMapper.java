package com.example.g_time.mapper;

import com.example.g_time.dto.TaskDTO;
import com.example.g_time.entity.Task;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface TaskMapper {
    void creatTask(@Param("userId") Long userId, @Param("taskDTO") TaskDTO taskDTO);
    List<Task> getTasksByUserId(@Param("userId") Long userId);
    void updateTask(@Param("taskId") Long taskId, @Param("userId") Long userId, @Param("taskDTO") TaskDTO taskDTO);

    Task getTaskByUserIdAndTaskId(@Param("userId") Long userId, @Param("taskId") Long taskId);

    void deleteTask(@Param("taskId")Long taskId,@Param("userId") Long userId);
}
