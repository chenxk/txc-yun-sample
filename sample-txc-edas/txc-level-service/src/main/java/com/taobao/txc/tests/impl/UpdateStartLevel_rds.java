package com.taobao.txc.tests.impl;

import javax.sql.DataSource;
import java.sql.SQLException;

/**
 * Created by simiao.zw on 2016/8/3.
 */
public class UpdateStartLevel_rds extends UpdateStarLvel_base {
    public void init() throws SQLException {
        dataSource = (DataSource) applicationContext.getBean("DataSource_rds");
        connection = dataSource.getConnection();
    }
}
