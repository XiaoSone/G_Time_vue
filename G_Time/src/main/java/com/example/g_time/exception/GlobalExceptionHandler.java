package com.example.g_time.exception;

import cn.dev33.satoken.util.SaResult;
import com.example.g_time.dto.ResultData;
import com.example.g_time.dto.ReturnCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.sql.SQLIntegrityConstraintViolationException;

@Slf4j
@RestControllerAdvice(basePackages = "com.example.g_time.controller") // 指定扫描的 controller 包
public class GlobalExceptionHandler {

    /**
     * 处理 UnifiedException 异常
     */
    @ExceptionHandler(UnifiedException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public SaResult handleUnifiedException(UnifiedException e) {
        log.error("统一异常信息: code={}, ex={}", e.getCode(), e.getMessage(), e);
        return SaResult.error(e.getMessage());
    }

    /**
     * 处理 SQL 完整性约束异常
     */
    //@ExceptionHandler(DataIntegrityViolationException.class)
    //@ResponseStatus(HttpStatus.BAD_REQUEST)
    //public SaResult handleDataIntegrityViolationException(DataIntegrityViolationException e) {
    //    if (e.getCause() instanceof SQLIntegrityConstraintViolationException) {
    //        log.error("数据库完整性约束异常: ex={}", e.getMessage(), e);
    //        return SaResult.error("操作失败: 数据完整性错误");
    //    }
    //    log.error("数据库操作异常: ex={}", e.getMessage(), e);
    //    return SaResult.error("操作失败: 数据库操作异常");
    //}

    /**
     * 处理空指针异常
     */
    //@ExceptionHandler(NullPointerException.class)
    //@ResponseStatus(HttpStatus.BAD_REQUEST)
    //public ResultData<String> handleNullPointerException(NullPointerException e) {
    //    log.error("空指针异常: ex={}", e.getMessage(), e);
    //    return ResultData.fail(ReturnCode.BAD_REQUEST, "请求参数错误，请检查输入");
    //}

    /**
     * 处理自定义业务异常
     */
    //@ExceptionHandler(BusinessException.class)
    //@ResponseStatus(HttpStatus.BAD_REQUEST)
    //public ResultData<String> handleBusinessException(BusinessException e) {
    //    log.error("业务异常: ex={}", e.getMessage(), e);
    //    return ResultData.fail(e.getCode(), e.getMessage());
    //}

    /**
     * 处理全局未捕获的异常
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public SaResult handleException(Exception e) {
        log.error("全局异常信息: ex={}", e.getMessage(), e);
        return SaResult.error("系统异常，请稍后重试");
    }

    /**
     * 处理请求参数错误异常
     */
    //@ExceptionHandler(IllegalArgumentException.class)
    //@ResponseStatus(HttpStatus.BAD_REQUEST)
    //public ResultData<String> handleIllegalArgumentException(IllegalArgumentException e) {
    //    log.error("请求参数错误: ex={}", e.getMessage(), e);
    //    return ResultData.fail(ReturnCode.BAD_REQUEST, e.getMessage());
    //}

    /**
     * 简化错误信息
     */
    //private String simplifyErrorMessage(String message) {
    //    if (message == null) {
    //        return "未知错误";
    //    }
    //    // 简化 SQL 异常信息
    //    if (message.contains("SQLIntegrityConstraintViolationException")) {
    //        // 提取关键信息
    //        return message.split("\n")[0].replace("java.sql.SQLIntegrityConstraintViolationException: ", "");
    //    }
    //    // 简化其他异常信息
    //    return message.split("\n")[0];
    //}
}
