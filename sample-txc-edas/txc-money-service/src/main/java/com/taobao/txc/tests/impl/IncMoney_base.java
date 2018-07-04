package com.taobao.txc.tests.impl;

import com.taobao.txc.common.TxcContext;
import com.taobao.txc.tests.IIncMoneyService;
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
 * Created by simiao.zw on 2016/7/15.
 */
public class IncMoney_base implements IIncMoneyService, ApplicationContextAware {
    protected DataSource dataSource;
    protected Connection connection;
    protected ApplicationContext applicationContext;
    protected IUpdateLevelService updateLevelService;

    @Override
    public String hello() {
        return TxcContext.getCurrentXid();
    }

    @Override
    @Transactional
    public void resetMoney(int money) throws SQLException {
        if (money < 0) {
            return;
        }
        Statement statement = connection.createStatement();
        StringBuilder sb = new StringBuilder();
        if (getMoney() == -1) {
            sb.append("insert into user_money_inc(money) values(").append(money).append(")");
            statement.execute(sb.toString());
        } else {
            sb.append("update user_money_inc set money=").append(money);
            statement.execute(sb.toString());
        }
        //adjust level
        updateLevelService.updateLevel(money);
    }

    @Override
    @Transactional
    public int incMoney(int money) throws SQLException {
        if (getMoney() == -1) {
            return -1;
        }
        Statement statement = connection.createStatement();
        StringBuilder sb = new StringBuilder();
        sb.append("update user_money_inc set money=money+").append(money);
        statement.execute(sb.toString());
        statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery("select * from user_money_inc");
        if (resultSet.next()) {
            money = resultSet.getInt("money");
            //adjust level
            updateLevelService.updateLevel(money);
        }
        return money;
    }

    @Override
    public int getMoney() throws SQLException {
        Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery("select * from user_money_inc");
        int money = -1;
        if (resultSet.next()) {
            money = resultSet.getInt("money");
        }
        return money;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
