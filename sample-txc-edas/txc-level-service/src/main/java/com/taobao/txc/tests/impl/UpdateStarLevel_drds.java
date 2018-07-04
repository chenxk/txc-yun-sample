package com.taobao.txc.tests.impl;

import javax.sql.DataSource;
import java.sql.SQLException;

/**
 * Created by simiao.zw on 2016/8/3.
 */
public class UpdateStarLevel_drds extends UpdateStarLvel_base {
    public void init() throws SQLException {
        dataSource = (DataSource) applicationContext.getBean("DataSource_drds");
        connection = dataSource.getConnection();
    }
}
