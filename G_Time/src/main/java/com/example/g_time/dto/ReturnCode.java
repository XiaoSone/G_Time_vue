package com.example.g_time.dto;

/**
 * 统一返回状态码枚举类
 */
public enum ReturnCode {
    SUCCESS(200, "操作成功"),              // 操作成功
    BAD_REQUEST(400, "请求参数错误"),       // 请求参数错误
    UNAUTHORIZED(401, "未授权访问"),        // 未授权访问
    FORBIDDEN(403, "禁止访问"),             // 禁止访问
    NOT_FOUND(404, "资源未找到"),           // 资源未找到
    INTERNAL_ERROR(500, "系统异常，请稍后重试"), // 系统异常
    DUPLICATE_KEY(501, "数据重复"),         // 数据重复
    INTEGRITY_ERROR(502, "数据完整性错误");  // 数据完整性错误

    private final int code;    // 状态码
    private final String msg;  // 描述信息

    ReturnCode(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public int getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }

    /**
     * 根据状态码获取对应的 ReturnCode 枚举
     */
    public static ReturnCode fromCode(int code) {
        for (ReturnCode returnCode : ReturnCode.values()) {
            if (returnCode.getCode() == code) {
                return returnCode;
            }
        }
        return INTERNAL_ERROR; // 默认返回系统异常
    }
}
