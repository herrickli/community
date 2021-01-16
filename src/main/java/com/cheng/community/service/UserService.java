package com.cheng.community.service;

import com.cheng.community.dao.UserMapper;
import com.cheng.community.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @Autowired
    public UserMapper userMapper;

    public User findUserById(int id){
        return userMapper.selectById(id);
    }
}
