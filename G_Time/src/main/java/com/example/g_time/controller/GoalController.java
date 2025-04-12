package com.example.g_time.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.util.SaResult;
import com.example.g_time.entity.Goal;
import com.example.g_time.entity.Stage;
import com.example.g_time.exception.UnifiedException;
import com.example.g_time.service.GoalService;
import com.example.g_time.util.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/goals")
public class GoalController {
    @Autowired
    private GoalService goalService;

    // 创建目标
    @SaCheckLogin
    @PostMapping("/create")
    public SaResult createGoal(@RequestBody Goal goal) {
        try {
            Long userId = StpUtil.getLoginIdAsLong();
            goalService.createGoal(userId, goal);
            return SaResult.data(goal).setMsg("创建目标成功");
        } catch (RuntimeException e) {
            String errorMessage = ExceptionUtils.getSimplifiedErrorMessage(e);
            throw new UnifiedException(500, "创建目标失败: " + errorMessage, e);
        }
    }

    // 添加阶段/子阶段
    @SaCheckLogin
    @PostMapping("/{goalId}/stages/create")
    public SaResult addStage(@PathVariable Long goalId, @RequestBody Stage stage) {
        try {
            Long userId = StpUtil.getLoginIdAsLong();
            Stage createdStage = goalService.addStage(goalId, stage, userId);
            return SaResult.data(createdStage).setMsg("添加阶段成功");
        } catch (RuntimeException e) {
            String errorMessage = ExceptionUtils.getSimplifiedErrorMessage(e);
            throw new UnifiedException(500, "添加阶段失败: " + errorMessage, e);
        }
    }

    // 查询目标及其完整阶段树
    @SaCheckLogin
    @PostMapping("/{goalId}")
    public SaResult getGoalById(@PathVariable Long goalId) {
        try {
            Long userId = StpUtil.getLoginIdAsLong();
            Goal goal = goalService.getGoalWithStagesTree(goalId, userId);
            if (goal != null) {
                return SaResult.data(goal).setMsg("查询目标成功");
            } else {
                throw new RuntimeException("目标不存在");
            }
        } catch (RuntimeException e) {
            return SaResult.error("查询目标失败: " + e.getMessage());
        }
    }

    // 查询用户所有目标(带阶段树)
    @SaCheckLogin
    @PostMapping("/list")
    public SaResult getGoalsByUserId() {
        try {
            Long userId = StpUtil.getLoginIdAsLong();
            List<Goal> goals = goalService.getGoalsWithStagesTreeByUserId(userId);
            if (goals != null) {
                return SaResult.data(goals).setMsg("查询目标列表成功");
            } else {
                throw new RuntimeException("暂无目标");
            }
        } catch (RuntimeException e) {
            return SaResult.error("查询目标列表失败: " + e.getMessage());
        }
    }

    // 更新目标
    @SaCheckLogin
    @PostMapping("/{goalId}/update")
    public SaResult updateGoal(@PathVariable Long goalId, @RequestBody Goal goal) {
        try {
            Long userId = StpUtil.getLoginIdAsLong();
            goalService.updateGoal(userId, goalId, goal);
            return SaResult.data(goal).setMsg("更新目标成功");
        } catch (RuntimeException e) {
            return SaResult.error("更新目标失败: " + e.getMessage());
        }
    }

    // 删除目标
    @SaCheckLogin
    @PostMapping("/{goalId}/delete")
    public SaResult deleteGoal(@PathVariable Long goalId) {
        try {
            Long userId = StpUtil.getLoginIdAsLong();
            goalService.deleteGoal(goalId, userId);
            return SaResult.data(goalService.getGoalsWithStagesTreeByUserId(userId)).setMsg("删除目标成功");
        } catch (RuntimeException e) {
            return SaResult.error("删除目标失败: " + e.getMessage());
        }
    }

    // 更新阶段状态(包含子阶段)
    @SaCheckLogin
    @PostMapping("{goalId}/stages/{stageId}/status")
    public SaResult updateStageStatus(@PathVariable("goalId") Long goalId,
                                      @PathVariable("stageId") Long stageId,
                                      @RequestBody Map<String, Boolean> status) {
        try {
            Long userId = StpUtil.getLoginIdAsLong();
            goalService.updateStageStatusWithChildren(goalId, stageId, status.get("status") ? 1 : 0, userId);
            return SaResult.ok().setMsg("更新阶段状态成功");
        } catch (RuntimeException e) {
            return SaResult.error("更新阶段状态失败: " + e.getMessage());
        }
    }


    // 更新阶段
    @SaCheckLogin
    @PostMapping("{goalId}/stages/{stageId}/update")
    public SaResult updateStage(@PathVariable("goalId") Long goalId,
                                      @PathVariable("stageId") Long stageId,
                                      @RequestBody Stage stage) {
        try {
            Long userId = StpUtil.getLoginIdAsLong();
            goalService.updateStageWithChildren(goalId, stageId, stage, userId);
            return SaResult.ok().setMsg("更新阶段状态成功");
        } catch (RuntimeException e) {
            return SaResult.error("更新阶段状态失败: " + e.getMessage());
        }
    }

    // 获取目标的完整阶段树
    @SaCheckLogin
    @PostMapping("/{goalId}/stages/tree")
    public SaResult getStagesTreeByGoalId(@PathVariable Long goalId) {
        try {
            Long userId = StpUtil.getLoginIdAsLong();
            List<Stage> stages = goalService.getStagesTreeByGoalId(goalId, userId);
            return SaResult.data(stages).setMsg("查询阶段树成功");
        } catch (RuntimeException e) {
            return SaResult.error("查询阶段树失败: " + e.getMessage());
        }
    }

    // 删除阶段(含子阶段)
    @SaCheckLogin
    @PostMapping("/{goalId}/stages/delete")
    public SaResult deleteStagesWithChildren(@PathVariable Long goalId, @RequestBody List<Long> stageIds) {
        try {
            Long userId = StpUtil.getLoginIdAsLong();
            goalService.deleteStagesWithChildren(goalId, stageIds, userId);
            return SaResult.ok().setMsg("删除阶段成功");
        } catch (RuntimeException e) {
            return SaResult.error("删除阶段失败: " + e.getMessage());
        }
    }
}
