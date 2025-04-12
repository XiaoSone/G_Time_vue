package com.example.g_time;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class test {

    private LinkedHashMap<String, Integer> dataMap = new LinkedHashMap<>();

    public test(Map<String, Integer> initialData) {
        // 使用LinkedHashMap保持插入顺序
        this.dataMap = new LinkedHashMap<>(initialData);
    }

    /**
     * 移动元素到新位置并调整其他元素
     * @param key 要移动的元素key
     * @param newPosition 新的位置值(1-based)
     */
    public void moveElement(String key, int newPosition) {
        if (!dataMap.containsKey(key)) {
            throw new IllegalArgumentException("Key not found: " + key);
        }

        int oldPosition = dataMap.get(key);

        // 不需要移动的情况
        if (oldPosition == newPosition) {
            return;
        }

        // 备份旧值
        int oldValue = dataMap.get(key);

        if (newPosition < oldPosition) {
            // 向前移动的情况 (newPosition < oldPosition)
            dataMap.replaceAll((k, v) -> {
                if (k.equals(key)) return newPosition;
                if (v >= newPosition && v < oldPosition) return v + 1;
                return v;
            });
        } else {
            // 向后移动的情况 (newPosition > oldPosition)
            dataMap.replaceAll((k, v) -> {
                if (k.equals(key)) return newPosition;
                if (v > oldPosition && v <= newPosition) return v - 1;
                return v;
            });
        }

        // 更新移动元素的值
        dataMap.put(key, newPosition);

        // 验证数据一致性
        validatePositions();
    }

    private void validatePositions() {
        long uniqueCount = dataMap.values().stream().distinct().count();
        if (uniqueCount != dataMap.size()) {
            throw new IllegalStateException("Position values are not unique after move operation");
        }
    }

    public Map<String, Integer> getData() {
        return dataMap.entrySet().stream()
                .sorted(Map.Entry.comparingByValue())
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        LinkedHashMap::new
                ));
    }

    public static void main(String[] args) {
        // 初始数据 {A:1, B:2, C:3, D:4}
        test manager = new test(new LinkedHashMap<String,Integer>() {{
            put("A", 1);
            put("B", 2);
            put("C", 3);
            put("D", 4);
        }});

        System.out.println("初始状态: " + manager.getData());

        // 案例1: 移动A到4的位置
        manager.moveElement("A", 4);
        System.out.println("移动A到4后: " + manager.getData());
        // 预期: {B:1, C:2, D:3, A:4}

        // 案例2: 移动D到1的位置
        manager.moveElement("D", 1);
        System.out.println("移动D到1后: " + manager.getData());
        // 预期: {D:1, B:2, C:3, A:4}

        // 案例3: 移动C到2的位置(不改变)
        manager.moveElement("C", 2);
        System.out.println("移动C到2后: " + manager.getData());
        // 预期: {D:1, C:2, B:3, A:4}
    }
}

