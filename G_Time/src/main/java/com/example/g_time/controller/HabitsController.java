package com.example.g_time.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.util.SaResult;
import com.example.g_time.dto.HabitsDTO;
import com.example.g_time.entity.Habit;
import com.example.g_time.service.HabitsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/habits")
public class HabitsController {
    @Autowired
    private HabitsService habitsService;

    @SaCheckLogin
    @PostMapping("/create")
    public SaResult createHabit(@RequestBody HabitsDTO habitsDTO) {
        try {
            Long userId = StpUtil.getLoginIdAsLong();
            habitsService.createHabit(userId, habitsDTO);
            return SaResult.data(habitsDTO).setMsg("创建习惯成功");
        } catch (RuntimeException e) {
            return SaResult.error("创建习惯失败: " + e.getMessage());
        }
    }

    //查询单习惯
    @SaCheckLogin
    @PostMapping("/{habitId}")
    public SaResult getHabit(@PathVariable Long habitId) {
        try {
            Long userId = StpUtil.getLoginIdAsLong();
            Habit habit = habitsService.selectHabitById(habitId, userId);
            return SaResult.data(habit).setMsg("查询单条习惯成功");
        } catch (RuntimeException e) {
            return SaResult.error("查询习惯失败: " + e.getMessage());
        }
    }

    //查询所有习惯
    @SaCheckLogin
    @PostMapping("/allHabits")
    public SaResult selectHabitsByUserId() {
        try {
            Long userId = StpUtil.getLoginIdAsLong();
            List<Habit> habitList = habitsService.selectHabitsByUserId(userId);
            return SaResult.data(habitList).setMsg("查询所有习惯成功");
        } catch (RuntimeException e) {
            return SaResult.error("查询习惯失败: " + e.getMessage());
        }

    }

    //更新习惯
    @SaCheckLogin
    @PostMapping("/update/{habitId}")
    public SaResult updateHabit(@PathVariable Long habitId, @RequestBody HabitsDTO habitsDTO) {
        try {
            Long userId = StpUtil.getLoginIdAsLong();
            habitsService.updateHabit(userId, habitId, habitsDTO);
            Habit habit = habitsService.selectHabitById(habitId, userId);
            return SaResult.data(habit).setMsg("习惯更新成功");
        } catch (RuntimeException e) {
            return SaResult.error("更新习惯失败: " + e.getMessage());
        }

    }

    //删除习惯
    @SaCheckLogin
    @PostMapping("/delete/{habitId}")
    public SaResult deleteHabit(@PathVariable Long habitId) {
        try {
            Long userId = StpUtil.getLoginIdAsLong();
            habitsService.deleteHabit(habitId, userId);
            return SaResult.ok("习惯删除成功");
        } catch (RuntimeException e) {
            return SaResult.error("删除习惯失败: " + e.getMessage());
        }

    }

    @SaCheckLogin
    @PostMapping("/check/{habitId}")
    public SaResult checkHabit(@PathVariable Long habitId) {
        try {
            Long userId = StpUtil.getLoginIdAsLong();
            habitsService.checkHabit(habitId, userId);
            int streak = habitsService.selectHabitById(habitId, userId).getStreak();
            return SaResult.data("当前打卡天数：" + streak + "天").setMsg("习惯完成");
        } catch (RuntimeException e) {
            return SaResult.error("习惯打卡失败: " + e.getMessage());
        }

    }

    //    查询习惯日志
    @SaCheckLogin
    @PostMapping("/logs")
    public SaResult selectHabitLog(@RequestBody Map<String, String> request) {
        try {
            LocalDate date = LocalDate.parse(request.get("date")); // 从请求体中获取日期
            return SaResult.data(habitsService.selectHabitLog(date)).setMsg("查询成功");
        } catch (RuntimeException e) {
            return SaResult.error("查询习惯日志失败: " + e.getMessage());
        }
    }

    //    查询段习惯日志
    @SaCheckLogin
    @PostMapping("/logs/range")
    public SaResult selectHabitLogs(@RequestBody Map<String, String> request) {
        try {
            LocalDate startDate = LocalDate.parse(request.get("startDate"));
            LocalDate endDate = LocalDate.parse(request.get("endDate"));
            return SaResult.data(habitsService.selectHabitLogs(startDate, endDate)).setMsg("查询成功");
        } catch (RuntimeException e) {
            return SaResult.error("查询习惯日志失败: " + e.getMessage());
        }
    }

    //    修改习惯日志
    @SaCheckLogin
    @PostMapping("/logs/updata")
    public SaResult upHabitLog(@RequestBody Map<String, String> request) {
        try {
            Long userId = StpUtil.getLoginIdAsLong();
            LocalDate date = LocalDate.parse(request.get("date")); // 从请求体中获取日期
            Long habitId = Long.parseLong(request.get("habitId"));
            boolean isCompleted = Boolean.parseBoolean(request.get("isCompleted"));
            return SaResult.data(habitsService.upHabitLog(date, habitId, isCompleted, userId)).setMsg("更新成功");
        } catch (RuntimeException e) {
            return SaResult.error("更新习惯日志失败: " + e.getMessage());
        }
    }
}
