package com.example.g_time.service;

import com.example.g_time.dto.TaskDTO;
import com.example.g_time.entity.Task;
import com.example.g_time.mapper.TaskMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TaskService {
    @Autowired
    private TaskMapper taskMapper;

    public void createTask(Long userId, TaskDTO taskDTO) {
        taskMapper.creatTask(userId,taskDTO);
    }
    public List<Task> getTasksByUserId(Long userId) {
        return taskMapper.getTasksByUserId(userId);
    }
    public void updateTask(Long taskId, Long userId, TaskDTO taskDTO) {
        taskMapper.updateTask(taskId, userId, taskDTO);
    }

    public Task getTaskByUserIdAndTaskId(Long userId, Long taskId) {
        return taskMapper.getTaskByUserIdAndTaskId(userId, taskId);
    }

    public void deleteTask(Long taskId, Long userId) {
        taskMapper.deleteTask(taskId, userId);
    }
}
