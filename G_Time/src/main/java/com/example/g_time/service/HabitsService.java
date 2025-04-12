package com.example.g_time.service;

import com.example.g_time.dto.HabitsDTO;
import com.example.g_time.entity.Habit;
import com.example.g_time.entity.HabitLog;
import com.example.g_time.mapper.HabitsMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Service
public class HabitsService {
    @Autowired
    private HabitsMapper habitsMapper;

    //创建习惯
    public void createHabit(Long userId, HabitsDTO habitsDTO) {
        if (userId == null) {
            throw new RuntimeException("用户ID不能为空");
        }
        if (habitsDTO == null) {
            throw new RuntimeException("习惯数据不能为空");
        }
        try {
            habitsDTO.setCreatedAt(LocalDateTime.now());
            habitsMapper.createHabit(userId, habitsDTO);
        } catch (Exception e) {
            throw new RuntimeException("创建习惯失败: " + e.getMessage(), e);
        }

    }

    //查询用户全部习惯
    public List<Habit> selectHabitsByUserId(Long userId) {
        if (userId == null) {
            throw new RuntimeException("用户ID不能为空");
        }
        List<Habit> habits = habitsMapper.selectHabitsByUserId(userId);
        return habits != null ? habits : Collections.emptyList();
    }

    //查询单个习惯
    public Habit selectHabitById(Long habitId, Long userId) {
        if (habitId == null || userId == null) {
            throw new RuntimeException("习惯ID或用户ID不能为空");
        }
        Habit habit = habitsMapper.selectHabitById(habitId, userId);
        if (habit == null) {
            throw new RuntimeException("习惯ID “" + habitId + "” 不存在");
        }
        return habit;
    }

    //更新习惯
    public void updateHabit(Long userId, Long habitId, HabitsDTO habitsDTO) {
        if (userId == null || habitId == null || habitsDTO == null) {
            throw new RuntimeException("用户ID、习惯ID或习惯数据不能为空");
        }
        Habit habit = habitsMapper.selectHabitById(habitId, userId);
        if (habit == null) {
            throw new RuntimeException("未找到习惯ID “" + habitId + "” 对应的习惯");
        }
        try {
            habitsDTO.setCreatedAt(habit.getCreatedAt());
            habitsMapper.updateHabit(userId, habitId, habitsDTO);
        } catch (Exception e) {
            throw new RuntimeException("更新习惯失败" + e.getMessage(), e);
        }

    }

    //删除习惯
    public void deleteHabit(Long habitId, Long userId) {
        if (habitId == null || userId == null) {
            throw new RuntimeException("习惯ID或用户ID不能为空");
        }
        Habit habit = habitsMapper.selectHabitById(habitId, userId);
        if (habit == null) {
            throw new RuntimeException("习惯ID “" + habitId + "” 不存在");
        }
        try {
            habitsMapper.deleteHabit(habitId, userId);
        } catch (Exception e) {
            throw new RuntimeException("删除习惯失败: " + e.getMessage(), e);
        }
    }

    //完成习惯
    public void checkHabit(Long habitId, Long userId) {
        if (habitId == null || userId == null) {
            throw new RuntimeException("习惯ID或用户ID不能为空");
        }
        Habit habit = habitsMapper.selectHabitById(habitId, userId);
        if (habit == null) {
            throw new RuntimeException("未找到习惯ID “" + habitId + "” 对应的习惯");
        }

        // 获取今天的日期
        LocalDate today = LocalDate.now();

        // 检查今天是否已经打过卡
        HabitLog existingLog = habitsMapper.getHabitLogByDate(habitId, today);
        if (existingLog != null && existingLog.getIsCompleted()) {
            throw new RuntimeException("今天已经完成打卡");
        }

        // 插入今天的打卡记录
        habitsMapper.createHabitLog(habitId, today, true);

        // 获取昨天的日期
        LocalDate yesterday = today.minusDays(1);

        // 检查昨天的打卡记录是否存在
        HabitLog lastLog = habitsMapper.getHabitLogByDate(habitId, yesterday);
        int newStreak = (lastLog != null && lastLog.getIsCompleted()) ? habit.getStreak() + 1 : 1;

        // 更新 streak 和 max_streak
        int newMaxStreak = Math.max(newStreak, habit.getMaxStreak());
        habitsMapper.updateStreak(habitId, newStreak, newMaxStreak);
    }

    //    查询一习惯日志
    public List<HabitLog> selectHabitLog(LocalDate date) {
        return habitsMapper.selectHabitLog(date);
    }//    查询段习惯日志
    public List<HabitLog> selectHabitLogs(LocalDate startDate, LocalDate endDate) {
        return habitsMapper.selectHabitLogs(startDate, endDate);
    }

    //修改一习惯日志
    public HabitLog upHabitLog(LocalDate date, Long habitId, boolean isCompleted, Long userId) {
        habitsMapper.upHabitLog(date, habitId, isCompleted);
        if (!isCompleted) {
            int streak = habitsMapper.selectHabitById(habitId, userId).getStreak();
            int maxStreak = habitsMapper.selectHabitById(habitId, userId).getMaxStreak();
            habitsMapper.updateStreak(habitId, streak - 1, maxStreak);
        }
        return habitsMapper.selectHabitLogbyHabitId(date, habitId);
    }

}
