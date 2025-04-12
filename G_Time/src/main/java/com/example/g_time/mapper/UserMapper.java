package com.example.g_time.mapper;

import com.example.g_time.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;


@Mapper
public interface UserMapper {
    User selectUserByPhone(@Param("phone") String account);
    User selectUserByEmail(@Param("email") String account);
    void save(@Param("user")User user);
}
