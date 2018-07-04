package com.taobao.txc.tests;

import java.sql.SQLException;

/**
 * Created by simiao.zw on 2016/7/15.
 */
public interface IUpdateLevelService {
    String hello();
    void resetLevel(int level) throws SQLException;
    int updateLevel(int money) throws SQLException;
    int getLevel() throws SQLException;
}
