package com.example.g_time.mapper;

import com.example.g_time.dto.HabitsDTO;
import com.example.g_time.entity.Habit;
import com.example.g_time.entity.HabitLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface HabitsMapper {
    void createHabit(@Param("userId") Long userId, @Param("habitsDTO") HabitsDTO habitsDTO);
    Habit selectHabitById(@Param("habitId")Long habitId, @Param("userId") Long userId);

    List<Habit> selectHabitsByUserId(@Param("userId")Long userId);

    void updateHabit(@Param("userId") Long userId, @Param("habitId") Long habitId, @Param("habitsDTO") HabitsDTO habitsDTO);

    void deleteHabit(@Param("habitId") Long habitId, @Param("userId") Long userId);

    void checkHabit(@Param("habitId") Long habitId, @Param("userId") Long userId, @Param("newStreak") int newStreak, @Param("newMaxStreak") int newMaxStreak);

    // 根据日期查询打卡记录
    HabitLog getHabitLogByDate(@Param("habitId") Long habitId, @Param("loggedAt") LocalDate loggedAt);

    // 插入打卡记录
    void createHabitLog(@Param("habitId")Long habitId, @Param("loggedAt")LocalDate today, @Param("isCompleted")boolean isCompleted);

    // 更新连续打卡天数和最大连续天数
    void updateStreak(@Param("habitId") Long habitId, @Param("newStreak") int newStreak, @Param("newMaxStreak") int newMaxStreak);

    List<HabitLog> selectHabitLog(@Param("date") LocalDate date);

    List<HabitLog> selectHabitLogs(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);


    void upHabitLog(@Param("date")LocalDate date, @Param("habitId")Long habitId, @Param("isCompleted")Boolean isCompleted);

    HabitLog selectHabitLogbyHabitId(@Param("date")LocalDate date, @Param("habitId")Long habitId);
}
