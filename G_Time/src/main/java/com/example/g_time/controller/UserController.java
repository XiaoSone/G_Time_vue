package com.example.g_time.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.SaTokenInfo;
import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.util.SaResult;
import com.example.g_time.dto.LoginDTO;
import com.example.g_time.entity.User;
import com.example.g_time.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;


@RestController
@RequestMapping("/user")
public class UserController {
    @Autowired
    private UserService userService;

    //登出
    @SaCheckLogin
    @PostMapping("/logout")
    public SaResult logout(@RequestHeader("satoken") String satoken) {
        System.out.println(satoken);
        StpUtil.logoutByTokenValue(satoken);
        //StpUtil.logout(10001, "PC");              // 强制指定账号指定端注销下线
        return SaResult.ok("登出成功!");
    }

    //注册
    @PostMapping("/register")
    public SaResult register(@RequestBody User user) {
        userService.registerUser(user);
        return SaResult.ok("注册成功!");
    }

    //登录
    @PostMapping("/login")
    public SaResult login(@RequestBody LoginDTO loginDTO) {
        User user = userService.isUser(loginDTO.getLoginName(), loginDTO.getLoginPaw());
        if (user != null) {
            // 检查用户是否已经登录
            if (StpUtil.isLogin(user.getId())) {
                // 如果已经登录，抛出异常
                throw new RuntimeException("已登录，请勿重复登录");
            } else {
                // 如果未登录，执行登录操作
                StpUtil.login(user.getId());
                Map<String, Object> data = new HashMap<>();
                data.put("user", user); // 添加 user 信息
                data.put("token", StpUtil.getTokenInfo().getTokenValue()); // 添加 token 信息

                return SaResult.data(data);
            }
        }
        // 如果用户不存在或密码错误，返回null
        return SaResult.error("用户不存在或密码错误，请检查后重新输入！");
    }
}
