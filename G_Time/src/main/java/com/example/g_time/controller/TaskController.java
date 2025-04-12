package com.example.g_time.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.util.SaResult;
import com.example.g_time.dto.TaskDTO;
import com.example.g_time.entity.Task;
import com.example.g_time.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/task")
public class TaskController {
    @Autowired
    private TaskService taskService;

    // 创建任务
    @SaCheckLogin
    @PostMapping("/create")
    public SaResult createTask(@RequestBody TaskDTO taskDTO) {
        Long userId = StpUtil.getLoginIdAsLong();
        taskService.createTask(userId, taskDTO);
        return SaResult.ok("任务创建成功");
    }

    // 获取任务列表
    @SaCheckLogin
    @PostMapping("/list")
    public SaResult getTasks() {
        Long userId = StpUtil.getLoginIdAsLong();
        List<Task> tasks = taskService.getTasksByUserId(userId);
        //System.out.println(tasks);
        return SaResult.ok().setData(tasks);
    }

    //获取任务
    @SaCheckLogin
    @PostMapping("/get/{taskId}")
    public Task getTaskByUserIdAndTaskId(@PathVariable Long taskId) {
        Long userId = StpUtil.getLoginIdAsLong();
        return taskService.getTaskByUserIdAndTaskId(userId, taskId);
    }

    // 更新任务
    @SaCheckLogin
    @PostMapping("/update/{taskId}")
    public SaResult updateTask(@PathVariable Long taskId, @RequestBody TaskDTO taskDTO) {
        Long userId = StpUtil.getLoginIdAsLong();
        System.out.println(taskDTO);
        taskService.updateTask(taskId, userId, taskDTO);
        Task task = taskService.getTaskByUserIdAndTaskId(userId, taskId);
        if (task != null) {
            return SaResult.ok("任务更新成功").setData(task);
        }
        return SaResult.error("任务不存在或无权修改");
    }

    // 删除任务
    @SaCheckLogin
    @PostMapping("/delete/{taskId}")
    public SaResult deleteTask(@PathVariable Long taskId) {
        Long userId = StpUtil.getLoginIdAsLong();
        taskService.deleteTask(taskId, userId);
        return SaResult.ok("任务删除成功");
    }

    @SaCheckLogin
    @PostMapping("/nowTask")
    public SaResult getTaskListNow(@RequestBody Map<String, String> request) {
        try {


            Long userId = StpUtil.getLoginIdAsLong();
            List<Task> tasks = taskService.getTasksByUserId(userId);
            LocalDateTime date = LocalDateTime.parse(request.get("date"));
            List<Task> nowTasks = tasks.stream()
                    .filter(task -> {
                        LocalDateTime startTime = task.getStartTime();
                        LocalDateTime endTime = task.getEndTime();
                        return (startTime.isBefore(date) || startTime.equals(date)) && endTime.isAfter(date)
                                && task.getStatus() == 0;
                    })
                    .collect(Collectors.toList());
            return SaResult.ok("当前日期任务获取成功").setData(nowTasks);
        } catch (Exception e) {
            //throw new RuntimeException(e);
            return SaResult.error("当前日期任务获取失败" + e.getMessage());
        }
    }
}
