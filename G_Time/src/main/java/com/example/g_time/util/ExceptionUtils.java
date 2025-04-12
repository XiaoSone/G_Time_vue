package com.example.g_time.util;

import org.springframework.dao.DataIntegrityViolationException;
import java.sql.SQLIntegrityConstraintViolationException;

public class ExceptionUtils {

    /**
     * 根据异常类型生成对应的错误信息
     */
    public static String getSimplifiedErrorMessage(Throwable e) {
        if (e instanceof DataIntegrityViolationException) {
            // 处理 SQL 完整性约束异常
            if (e.getCause() instanceof SQLIntegrityConstraintViolationException) {
                return extractSqlExceptionMessage(e.getCause().getMessage());
            }
            return "数据完整性错误";
        } else if (e instanceof NullPointerException) {
            // 处理空指针异常
            return "空指针异常";
        }else if (e instanceof IllegalArgumentException){
            // 处理非法参数异常
            return "非法参数: " + e.getMessage();
        }else {
            // 处理其他异常
            return e.getMessage();
        }
    }

    /**
     * 提取 SQL 异常中的关键信息
     */
    private static String extractSqlExceptionMessage(String message) {
        if (message == null) {
            return "数据库操作异常";
        }
        // 提取关键信息
        return message.split("\n")[0].replace("java.sql.SQLIntegrityConstraintViolationException: ", "");
    }
}
