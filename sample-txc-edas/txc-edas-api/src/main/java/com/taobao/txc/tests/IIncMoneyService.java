package com.taobao.txc.tests;

import java.sql.SQLException;

/**
 * Created by simiao.zw on 2016/7/15.
 */
public interface IIncMoneyService {
    String hello();
    void resetMoney(int money) throws SQLException;
    int incMoney(int money) throws SQLException;
    int getMoney() throws SQLException;
}
