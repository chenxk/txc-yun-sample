package com.taobao.txc.tests.impl;

import com.taobao.txc.tests.IUpdateLevelService;
import javax.sql.DataSource;
import java.sql.SQLException;

/**
 * Created by simiao.zw on 2016/8/3.
 */
public class DecMoney_rds extends DecMoney_base{
    public void init() throws SQLException {
        dataSource = (DataSource) applicationContext.getBean("DataSource_rds");
        updateLevelService = (IUpdateLevelService) applicationContext.getBean("UpdateLevelService_rds");
        connection = dataSource.getConnection();
    }
}
