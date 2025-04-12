package com.example.g_time.mapper;

import com.example.g_time.entity.Goal;
import com.example.g_time.entity.Stage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface GoalMapper {
    // 目标操作
    void createGoal(@Param("goal") Goal goal);

    Goal selectGoalById(@Param("id") Long id, @Param("userId") Long userId);

    List<Goal> selectGoalsByUserId(@Param("userId") Long userId);

    void updateGoal(@Param("userId") Long userId, @Param("goalId") Long goalId, @Param("goal") Goal goal);

    void deleteGoal(@Param("goalId") Long goalId, @Param("userId") Long userId);

    // 阶段操作
    void addStage(@Param("stage") Stage stage);

    void updateStageStatus(@Param("goalId") Long goalId, @Param("stageId") Long stageId, @Param("status") int status);

    List<Stage> selectStagesByGoalId(@Param("goalId") Long goalId);

    Stage selectStageById(@Param("stageId") Long stageId);

    List<Stage> selectSubStagesByParentId(@Param("parentStageId") Long parentStageId);

    Integer getMaxSequenceByGoalId(@Param("goalId") Long goalId);

    void deleteStages(@Param("goalId") Long goalId, @Param("stageIds") List<Long> stageIds);

    List<Stage> selectStagesByIds(@Param("goalId") Long goalId, @Param("stageIds") List<Long> stageIds);

    //void updateStageSequence(@Param("stageId") Long stageId, @Param("sequence") Integer sequence);

    void updateStage(@Param("stage") Stage stage);

    List<Stage> selectRootStagesByGoalId(@Param("goalId") Long goalId);

    List<Stage> selectStagesByGoalAndParent(@Param("goalId") Long goalId, @Param("parentStageId") Long parentStageId);

    void batchUpdateStageSequences(@Param("sequenceUpdates") Map<Long, Integer> sequenceUpdates);

    void batchUpdateStageStatus(@Param("statusUpdates") Map<Long, Integer> statusUpdates);

    void updateGoalStatus(@Param("goalId") Long goalId, @Param("status")int i);
}
