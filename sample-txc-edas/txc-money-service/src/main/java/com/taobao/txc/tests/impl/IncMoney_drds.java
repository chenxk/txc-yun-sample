package com.taobao.txc.tests.impl;

import com.taobao.txc.tests.IUpdateLevelService;
import javax.sql.DataSource;
import java.sql.SQLException;

/**
 * Created by simiao.zw on 2016/8/3.
 */
public class IncMoney_drds extends IncMoney_base{
    public void init() throws SQLException {
        dataSource = (DataSource) applicationContext.getBean("DataSource_drds");
        updateLevelService = (IUpdateLevelService) applicationContext.getBean("UpdateLevelService_drds");
        connection = dataSource.getConnection();
    }
}
