package com.example.g_time.dto;

import lombok.Data;

/**
 * 统一返回结果封装类
 */
@Data
public class ResultData<T> {
    private int code;        // 状态码
    private String msg;      // 描述信息
    private T data;          // 返回的数据
    private long timestamp;  // 时间戳

    public ResultData() {
        this.timestamp = System.currentTimeMillis();
    }

    /**
     * 成功方法：默认返回状态码 200
     */
    public static <T> ResultData<T> success() {
        return success(null);
    }

    /**
     * 成功方法：返回状态码 200 和成功提示信息，并携带数据
     */
    public static <T> ResultData<T> success(T data) {
        ResultData<T> result = new ResultData<>();
        result.setCode(ReturnCode.SUCCESS.getCode());
        result.setMsg(ReturnCode.SUCCESS.getMsg());
        result.setData(data);
        return result;
    }

    /**
     * 失败方法：返回自定义状态码和错误信息
     */
    public static <T> ResultData<T> fail(int code, String msg) {
        ResultData<T> result = new ResultData<>();
        result.setCode(code);
        result.setMsg(msg);
        return result;
    }

    /**
     * 失败方法：基于 ReturnCode 返回
     */
    public static <T> ResultData<T> fail(ReturnCode returnCode) {
        return fail(returnCode.getCode(), returnCode.getMsg());
    }

    /**
     * 失败方法：基于 ReturnCode 返回，并支持自定义提示信息
     */
    public static <T> ResultData<T> fail(ReturnCode returnCode, String customMsg) {
        return fail(returnCode.getCode(), customMsg);
    }
}
