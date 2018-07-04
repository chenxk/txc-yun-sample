package com.springMyBatis.system.dao;

import com.springMyBatis.system.model.User;

public interface UserDao {
    public void insert(User user);
    public void enableTXC();
}