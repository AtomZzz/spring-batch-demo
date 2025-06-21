package com.atom.springbatchtest.batch.processor;

import com.atom.springbatchtest.batch.model.User;
import org.springframework.batch.item.ItemProcessor;

public class UserItemProcessor implements ItemProcessor<User, User> {
    @Override
    public User process(User user) {
        // 示例：将用户名转为大写
        user.setName(user.getName().toUpperCase());
        return user;
    }
} 