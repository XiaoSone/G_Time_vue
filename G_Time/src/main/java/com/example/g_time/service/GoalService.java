package com.example.g_time.service;

import com.example.g_time.entity.Goal;
import com.example.g_time.entity.Stage;
import com.example.g_time.mapper.GoalMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class GoalService {
    @Autowired
    private GoalMapper goalMapper;

    // 创建目标
    public void createGoal(Long userId, Goal goal) {
        goal.setUserId(userId);
        goal.setCreatedAt(LocalDateTime.now());
        goal.setUpdatedAt(LocalDateTime.now());
        goal.setStatus(0); // 默认状态为进行中
        goalMapper.createGoal(goal);
    }

    // 添加阶段/子阶段
    @Transactional
    public Stage addStage(Long goalId, Stage stage, Long userId) {
        // 验证目标存在且属于用户
        Goal goal = goalMapper.selectGoalById(goalId, userId);
        if (goal == null) {
            throw new RuntimeException("目标不存在或无权操作");
        }

        // 如果指定了父阶段，验证其存在且属于同一目标
        if (stage.getParentStageId() != null) {
            Stage parentStage = goalMapper.selectStageById(stage.getParentStageId());
            if (parentStage == null || !parentStage.getGoalId().equals(goalId)) {
                throw new RuntimeException("父阶段不存在或不属于此目标");
            }
        }

        // 设置序列号
        Integer maxSequence = goalMapper.getMaxSequenceByGoalId(goalId);
        stage.setSequence(maxSequence != null ? maxSequence + 1 : 1);

        stage.setGoalId(goalId);
        stage.setStatus(0); // 默认未完成
        goalMapper.addStage(stage);

        return stage;
    }

    // 获取目标及其完整的阶段树
    public Goal getGoalWithStagesTree(Long goalId, Long userId) {
        Goal goal = goalMapper.selectGoalById(goalId, userId);
        if (goal == null) {
            return null;
        }

        List<Stage> stages = goalMapper.selectStagesByGoalId(goalId);
        goal.setStages(buildStageTree(stages));
        goal.setProgress(calculateGoalProgress(goal));

        return goal;
    }

    // 获取用户所有目标及其阶段树
    public List<Goal> getGoalsWithStagesTreeByUserId(Long userId) {
        List<Goal> goals = goalMapper.selectGoalsByUserId(userId);
        if (goals == null || goals.isEmpty()) {
            return goals;
        }

        // 查询所有目标的阶段
        List<Stage> allStages = new ArrayList<>();
        for (Goal goal : goals) {
            allStages.addAll(goalMapper.selectStagesByGoalId(goal.getId()));
        }

        // 按目标分组构建阶段树
        Map<Long, List<Stage>> goalStagesMap = allStages.stream()
                .collect(Collectors.groupingBy(Stage::getGoalId));

        for (Goal goal : goals) {
            List<Stage> stages = goalStagesMap.getOrDefault(goal.getId(), new ArrayList<>());
            goal.setStages(buildStageTree(stages));
            goal.setProgress(calculateGoalProgress(goal));
        }

        return goals;
    }

    // 构建阶段树
    private List<Stage> buildStageTree(List<Stage> stages) {
        if (stages == null || stages.isEmpty()) {
            return new ArrayList<>();
        }

        // 按父阶段ID分组
        Map<Long, List<Stage>> parentStageMap = stages.stream()
                .filter(stage -> stage.getParentStageId() != null)
                .collect(Collectors.groupingBy(Stage::getParentStageId));

        // 设置每个阶段的子阶段
        for (Stage stage : stages) {
            if (parentStageMap.containsKey(stage.getId())) {
                stage.setSubStages(parentStageMap.get(stage.getId()));
            } else {
                stage.setSubStages(new ArrayList<>());
            }
        }

        // 返回根阶段(没有父阶段的)
        return stages.stream()
                .filter(stage -> stage.getParentStageId() == null)
                .sorted(Comparator.comparingInt(Stage::getSequence))
                .collect(Collectors.toList());
    }

    // 计算目标进度
    private Double calculateGoalProgress(Goal goal) {
        if (goal.getStages() == null || goal.getStages().isEmpty()) {
            return goal.getStatus() == 1 ? 100.0 : 0.0;
        }

        // 递归计算所有阶段的完成情况
        int[] counts = countStages(goal.getStages());
        if (counts[1] == 0) {
            return 0.0;
        }

        return (double) Math.round(counts[0] * 10000.0 / counts[1]) / 100;
    }

    // 递归统计阶段完成情况 [已完成数, 总数]
    private int[] countStages(List<Stage> stages) {
        int completed = 0;
        int total = 0;

        for (Stage stage : stages) {
            total++;
            if (stage.getStatus() == 1) {
                completed++;
            }

            // 递归统计子阶段
            if (stage.getSubStages() != null && !stage.getSubStages().isEmpty()) {
                int[] subCounts = countStages(stage.getSubStages());
                completed += subCounts[0];
                total += subCounts[1];
            }
        }

        return new int[]{completed, total};
    }

    // 更新目标
    @Transactional
    public void updateGoal(Long userId, Long goalId, Goal goal) {
        Goal existingGoal = goalMapper.selectGoalById(goalId, userId);
        if (existingGoal == null) {
            throw new RuntimeException("目标不存在或无权操作");
        }

        goal.setId(goalId);
        goal.setUserId(userId);
        goal.setUpdatedAt(LocalDateTime.now());
        goalMapper.updateGoal(userId, goalId, goal);
    }

    // 删除目标(及其所有阶段)
    @Transactional
    public void deleteGoal(Long goalId, Long userId) {
        Goal goal = goalMapper.selectGoalById(goalId, userId);
        if (goal == null) {
            throw new RuntimeException("目标不存在或无权操作");
        }

        goalMapper.deleteGoal(goalId, userId);
    }

    // 更新阶段状态
    @Transactional
    public void updateStageStatusWithChildren(Long goalId, Long stageId, int status, Long userId) {
        if (status != 0 && status != 1) {
            throw new RuntimeException("状态值不合法");
        }

        // 验证目标存在且属于用户
        Goal goal = goalMapper.selectGoalById(goalId, userId);
        if (goal == null) {
            throw new RuntimeException("目标不存在或无权操作");
        }

        // 获取阶段及其所有子阶段
        Stage stage = goalMapper.selectStageById(stageId);
        if (stage == null || !stage.getGoalId().equals(goalId)) {
            throw new RuntimeException("阶段不存在或不属于此目标");
        }

        // 递归更新阶段及其子阶段状态
        updateStageAndChildrenStatus(goalId, stageId, status);

        // 检查并更新父阶段状态（如果有）
        updateParentStageStatusIfNeeded(goalId, stage.getParentStageId());

        // 重新计算目标进度
        Goal updatedGoal = getGoalWithStagesTree(goalId, userId);
        goalMapper.updateGoal(updatedGoal.getUserId(), updatedGoal.getId(), updatedGoal);
    }

    // 更新阶段
    @Transactional
    public void updateStageWithChildren(Long goalId, Long stageId, Stage stage, Long userId) {
        // 验证目标存在且属于用户
        Goal goal = goalMapper.selectGoalById(goalId, userId);
        if (goal == null) {
            throw new RuntimeException("目标不存在或无权操作");
        }

        // 验证阶段存在且属于目标
        Stage existingStage = goalMapper.selectStageById(stageId);


        boolean isOnlyUpdatingBasicAttributes =
                stage.getParentStageId() == null && stage.getGoalId() == null && stage.getSequence() == null;


        System.out.println("isOnlyUpdatingBasicAttributes："+ isOnlyUpdatingBasicAttributes);
        if (isOnlyUpdatingBasicAttributes) {
            // 仅更新基本字段
            existingStage.setName(stage.getName());
            existingStage.setDescription(stage.getDescription());
            existingStage.setDueDate(stage.getDueDate());
            existingStage.setStatus(stage.getStatus());
            goalMapper.updateStage(existingStage);
            return;
        }

        if (existingStage == null || !existingStage.getGoalId().equals(goalId)) {
            throw new RuntimeException("阶段不存在或不属于此目标");
        }

        // 如果要更新父阶段ID，需要验证新的父阶段存在且属于同一目标
        if (stage.getParentStageId() != null &&
                !stage.getParentStageId().equals(existingStage.getParentStageId())) {
            Stage newParentStage = goalMapper.selectStageById(stage.getParentStageId());
            if (newParentStage == null || !newParentStage.getGoalId().equals(goalId)) {
                throw new RuntimeException("新的父阶段不存在或不属于此目标");
            }

            // 检查是否会形成循环引用
            if (isCircularReference(stageId, stage.getParentStageId(), goalId)) {
                throw new RuntimeException("不能将阶段设置为其自身的子阶段");
            }
        }

        // 判断是否是层级变更（改变parentStageId）
        boolean isChangingLevel = stage.getParentStageId() != null &&
                !stage.getParentStageId().equals(existingStage.getParentStageId()) ||
                (stage.getParentStageId() == null && existingStage.getParentStageId() != null);



        // 获取目标层级的所有同级阶段（移动后的层级）
        Long targetParentId = stage.getParentStageId() != null ? stage.getParentStageId() : null;
        List<Stage> targetSiblings = goalMapper.selectStagesByGoalAndParent(goalId, targetParentId);



        // 如果是同级移动且没有改变序列号，无需调整序列
        boolean isSameLevelNoSequenceChange =
                (existingStage.getParentStageId() == null && targetParentId == null ||
                        existingStage.getParentStageId() != null && existingStage.getParentStageId().equals(targetParentId)) &&
                        stage.getSequence() == null;



        if (!isSameLevelNoSequenceChange) {
            // 1. 调整原位置的序列（如果阶段移动走了，需要填补空缺）
            if (isChangingLevel) {
                // 获取原来层级的同级阶段（不包括当前阶段）
                List<Stage> originalSiblings = goalMapper.selectStagesByGoalAndParent(
                                goalId, existingStage.getParentStageId()).stream()
                        .filter(s -> !s.getId().equals(stageId))
                        .collect(Collectors.toList());
                // 调整原来层级的序列
                adjustSequencesAfterRemoval(originalSiblings);
            }

            // 获取目标层级最大的sequence值
            int maxSequence = targetSiblings.stream()
                    .filter(s -> !s.getId().equals(stageId))  // 排除当前阶段（如果存在）
                    .mapToInt(Stage::getSequence)
                    .max()
                    .orElse(0);


            if (stage.getSequence() == null || stage.getSequence() > maxSequence || stage.getSequence() < 1) {
                stage.setSequence(maxSequence + 1);
            }


            if (!targetSiblings.isEmpty()) {
                Map<Long, Integer> sequenceMap = targetSiblings.stream().collect(Collectors.toMap(
                        Stage::getId,
                        Stage::getSequence,
                        (a, b) -> b,
                        LinkedHashMap::new
                ));
                //如果在sequenceMap中找不到stage.getStageId
                if (!sequenceMap.containsKey(stage.getId())) {
                    sequenceMap.put(stage.getId(), stage.getSequence());
                }


                int oldPosition = sequenceMap.get(stage.getId());


                int newPosition = stage.getSequence();


                Map<Long, Integer> newMap = new LinkedHashMap<>();
                if (oldPosition == newPosition) {
                    newMap = new LinkedHashMap<>(sequenceMap);
                }

                if (newPosition < oldPosition) {
                    // 向前移动
                    Map<Long, Integer> finalNewMap = newMap;

                    sequenceMap.forEach((id, seq) -> {
                        if (id == stageId) {
                            finalNewMap.put(id, newPosition);
                        } else if (seq >= newPosition && seq < oldPosition) {
                            finalNewMap.put(id, seq + 1);
                        } else {
                            finalNewMap.put(id, seq);
                        }
                    });
                    newMap = finalNewMap;
                } else {
                    // 向后移动
                    Map<Long, Integer> finalNewMap1 = newMap;
                    sequenceMap.forEach((id, seq) -> {
                        if (id == stageId) {
                            finalNewMap1.put(id, newPosition);
                        } else if (seq > oldPosition && seq <= newPosition) {
                            finalNewMap1.put(id, seq - 1);
                        } else {
                            finalNewMap1.put(id, seq);
                        }
                    });
                    newMap = finalNewMap1;
                }
                newMap.entrySet().stream()
                        .sorted(Map.Entry.comparingByValue())
                        .collect(Collectors.toMap(
                                Map.Entry::getKey,
                                Map.Entry::getValue,
                                (a, b) -> b,
                                LinkedHashMap::new
                        ));

                if (!newMap.isEmpty()) {
                    goalMapper.batchUpdateStageSequences(newMap);
                }
            }
        }

        //更新阶段（及子树）状态
        if (stage.getStatus() != existingStage.getStatus()) {
            updateStageAndChildrenStatus(stage.getGoalId(), stage.getId(), stage.getStatus());
        }
        // 更新阶段基本信息（包括可能的parentStageId和sequence变更）
        stage.setId(stageId);
        System.out.println("更新阶段基本信息" + stage);
        goalMapper.updateStage(stage);

        // 重新计算目标进度
        Goal updatedGoal = getGoalWithStagesTree(goalId, userId);
        goalMapper.updateGoal(updatedGoal.getUserId(), updatedGoal.getId(), updatedGoal);
    }


    // 当一个阶段从层级中移除时，调整剩余阶段的序列
    private void adjustSequencesAfterRemoval(List<Stage> siblings) {
        // 按sequence排序
        siblings.sort(Comparator.comparingInt(Stage::getSequence));

        Map<Long, Integer> sequenceUpdates = new HashMap<>();
        for (int i = 0; i < siblings.size(); i++) {
            Stage s = siblings.get(i);
            int expectedSequence = i + 1;
            if (s.getSequence() != expectedSequence) {
                sequenceUpdates.put(s.getId(), expectedSequence);
            }
        }
        System.out.println("原阶层的更新：" + sequenceUpdates);
        if (!sequenceUpdates.isEmpty()) {
            goalMapper.batchUpdateStageSequences(sequenceUpdates);
        }
    }


    // 检查是否会形成循环引用
    private boolean isCircularReference(Long stageId, Long newParentId, Long goalId) {
        if (stageId.equals(newParentId)) return true;

        // 加载目标下所有阶段（避免递归查询）
        Map<Long, Stage> allStages = goalMapper.selectStagesByGoalId(goalId).stream()
                .collect(Collectors.toMap(Stage::getId, s -> s));

        Long current = newParentId;
        while (current != null) {
            if (current.equals(stageId)) return true;
            current = allStages.get(current) != null ?
                    allStages.get(current).getParentStageId() : null;
        }
        return false;
    }


    // 递归更新阶段及子阶段状态
    private void updateStageAndChildrenStatus(Long goalId, Long stageId, int status) {
        // 更新当前阶段
        goalMapper.updateStageStatus(goalId, stageId, status);

        // 获取并更新所有子阶段
        List<Stage> children = goalMapper.selectSubStagesByParentId(stageId);
        for (Stage child : children) {
            updateStageAndChildrenStatus(goalId, child.getId(), status);
        }
    }

    // 检查并更新父阶段状态
    private void updateParentStageStatusIfNeeded(Long goalId, Long parentId) {
        if (parentId == null) {
            parentId = null;
        }
        // 获取父阶段的所有子阶段
        List<Stage> children = goalMapper.selectSubStagesByParentId(parentId);
        System.out.println(children);
        // 检查是否所有子阶段都已完成
        boolean allChildrenCompleted = children.stream()
                .allMatch(child -> child.getStatus() == 1);

        if (allChildrenCompleted) {
            if (parentId == null) {
                // 更新目标状态为完成
                goalMapper.updateGoalStatus(goalId, 1);
            } else {
                // 更新父阶段状态为完成
                goalMapper.updateStageStatus(goalId, parentId, 1);

                // 递归检查父阶段的父阶段...
                Stage parentStage = goalMapper.selectStageById(parentId);
                updateParentStageStatusIfNeeded(goalId, parentStage.getParentStageId());
            }

        } else {
            // 如果有未完成的子阶段，确保父阶段状态为未完成
            goalMapper.updateGoalStatus(goalId, 0);
            goalMapper.updateStageStatus(goalId, parentId, 0);
        }
    }

    // 获取目标的完整阶段树
    public List<Stage> getStagesTreeByGoalId(Long goalId, Long userId) {
        Goal goal = goalMapper.selectGoalById(goalId, userId);
        if (goal == null) {
            throw new RuntimeException("目标不存在或无权操作");
        }

        List<Stage> stages = goalMapper.selectStagesByGoalId(goalId);
        return buildStageTree(stages);
    }

    // 删除阶段(含子阶段)
    @Transactional
    public void deleteStagesWithChildren(Long goalId, List<Long> stageIds, Long userId) {
        // 验证目标存在且属于用户
        Goal goal = goalMapper.selectGoalById(goalId, userId);
        if (goal == null) {
            throw new RuntimeException("目标不存在或无权操作");
        }

        // 获取所有要删除的阶段ID(包含子阶段)
        Set<Long> allStageIds = new HashSet<>(stageIds);

        // 递归获取所有子阶段ID
        Queue<Long> queue = new LinkedList<>(stageIds);
        while (!queue.isEmpty()) {
            Long parentId = queue.poll();
            List<Stage> children = goalMapper.selectSubStagesByParentId(parentId);
            children.forEach(child -> {
                if (allStageIds.add(child.getId())) {
                    queue.offer(child.getId());
                }
            });
        }

        // 删除阶段
        if (!allStageIds.isEmpty()) {
            goalMapper.deleteStages(goalId, new ArrayList<>(allStageIds));

            // 重新计算目标进度
            Goal updatedGoal = getGoalWithStagesTree(goalId, userId);
            goalMapper.updateGoal(updatedGoal.getUserId(), updatedGoal.getId(), updatedGoal);
        }
    }
}
