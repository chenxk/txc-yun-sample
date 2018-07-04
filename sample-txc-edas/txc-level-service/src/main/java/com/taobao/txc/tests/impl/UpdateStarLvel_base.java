package com.taobao.txc.tests.impl;

import com.taobao.txc.common.TxcContext;
import com.taobao.txc.tests.IUpdateLevelService;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.transaction.annotation.Transactional;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Created by simiao.zw on 2016/8/3.
 */
public class UpdateStarLvel_base implements IUpdateLevelService, ApplicationContextAware {
    protected DataSource dataSource;
    protected Connection connection;
    protected ApplicationContext applicationContext;

    @Override
    public String hello() {
        return TxcContext.getCurrentXid();
    }

    @Override
    @Transactional
    public void resetLevel(int level) throws SQLException {
        if (level < 0) {
            return;
        }
        Statement statement = connection.createStatement();
        StringBuilder sb = new StringBuilder();
        if (getLevel() == -1) {
            sb.append("insert into user_level(level) values(").append(level).append(")");
            statement.execute(sb.toString());
        } else {
            sb.append("update user_level set level=").append(level);
            statement.execute(sb.toString());
        }
    }

    @Override
    @Transactional
    public int updateLevel(int money) throws SQLException {
        if (getLevel() == -1) {
            return -1;
        }
        int level = 0;
        Statement statement = connection.createStatement();
        StringBuilder sb = new StringBuilder();
        /* 0~999, 1000~4999, 5000~9999 */
        if (money > 0 && money < 1000) {
            level = 1;
        } else if (money >= 1000 && money < 5000) {
            level = 2;
        } else if (money >=5000) {
            level = 3;
        }
        sb.append("update user_level set level=").append(level);
        statement.execute(sb.toString());
        return level;
    }

    @Override
    public int getLevel() throws SQLException {
        Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery("select * from user_level");
        int level = -1;
        if (resultSet.next()) {
            level = resultSet.getInt("level");
        }
        return level;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
