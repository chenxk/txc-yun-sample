package com.taobao.txc.tests.impl;

import com.taobao.txc.common.TxcContext;
import com.taobao.txc.tests.IDecMoneyService;
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
public class DecMoney_base implements IDecMoneyService, ApplicationContextAware {
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
            sb.append("insert into user_money_dec(money) values(").append(money).append(")");
            statement.execute(sb.toString());
        } else {
            sb.append("update user_money_dec set money=").append(money);
            statement.execute(sb.toString());
        }
    }

    @Override
    @Transactional
    public int decMoney(int money) throws SQLException {
        if (getMoney() == -1) {
            return -1;
        }
        Statement statement = connection.createStatement();
        StringBuilder sb = new StringBuilder();
        sb.append("update user_money_dec set money=money-").append(money);
        statement.execute(sb.toString());
        statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery("select * from user_money_dec");
        if (resultSet.next()) {
            money = resultSet.getInt("money");
        }
        int dec_money = money;
        statement = connection.createStatement();
        resultSet = statement.executeQuery("select * from user_money_inc");
        int inc_money = 0;
        if (resultSet.next()) {
            inc_money = resultSet.getInt("money");
        }
        statement = connection.createStatement();
        resultSet = statement.executeQuery("select * from user_level");
        int level = 0;
        if (resultSet.next()) {
            level = resultSet.getInt("level");
        }
        System.out.println("[XXX] DecMoney before return:");
        System.out.println("[XXX] inc_money: " + inc_money);
        System.out.println("[XXX] dec_money: " + dec_money);
        System.out.println("[XXX] level: " + level);
        //last will throw exception
        if (money < 0) {
            throw new RuntimeException("not enough money");
        }
        return money;
    }

    @Override
    public int getMoney() throws SQLException {
        Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery("select * from user_money_dec");
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
