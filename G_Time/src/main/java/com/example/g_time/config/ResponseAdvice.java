package com.example.g_time.config;

import cn.dev33.satoken.util.SaResult;
import com.example.g_time.dto.ResultData;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

@RestControllerAdvice(basePackages = "com.example.g_time.controller") // 扫描 controller 包
public class ResponseAdvice implements ResponseBodyAdvice<Object> {
    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public boolean supports(MethodParameter methodParameter, Class<? extends HttpMessageConverter<?>> aClass) {
        return true; // 支持所有 Controller 方法的返回值
    }

    @SneakyThrows
    @Override
    public Object beforeBodyWrite(Object o, MethodParameter methodParameter, MediaType mediaType, Class<? extends HttpMessageConverter<?>> aClass, ServerHttpRequest serverHttpRequest, ServerHttpResponse serverHttpResponse) {
        if (o instanceof String) {
            // 如果返回值是 String 类型，手动将其转换为 JSON 格式
            return objectMapper.writeValueAsString(ResultData.success(o));
        }
        if (o instanceof ResultData || o instanceof SaResult) {
            // 如果返回值已经是 ResultData 或 SaResult 类型，直接返回
            return o;
        }
        // 其他类型，统一封装为 ResultData
        return ResultData.success(o);
    }
}
