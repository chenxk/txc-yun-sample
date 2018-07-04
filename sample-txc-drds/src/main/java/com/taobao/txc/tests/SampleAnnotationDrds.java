package com.taobao.txc.tests;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Random;

import com.taobao.txc.client.aop.annotation.TxcTransaction;
import com.taobao.txc.common.TxcContext;
import org.apache.commons.lang.StringUtils;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class SampleAnnotationDrds {
    public static Random random = new Random();

    @TxcTransaction(timeout = 60000 * 3)
    public void insertTest(DataSource dataSource) throws SQLException {
        String xid = TxcContext.getCurrentXid();

        if (StringUtils.isEmpty(xid))
            throw new RuntimeException("xid is null");

        Connection connection = null;
        Statement statement = null;
        try {
            connection = dataSource.getConnection();
            statement = connection.createStatement();

            connection.setAutoCommit(false);
            statement.execute("set TXC_XID='" + xid + "'");
            statement.execute("insert into account(cardNum, name, balance) values(1, 'aaa', 100)");
            statement.execute("insert into account(cardNum, name, balance) values(2, 'bbb', 200)");
            statement.execute("insert into account(cardNum, name, balance) values(3, 'ccc', 300)");
            connection.commit();

            if (random.nextInt(2) == 1)
                throw new RuntimeException("aaaa");

            connection.setAutoCommit(true);
        } finally {
            connection.setAutoCommit(true);
            if (statement != null)
                statement.close();
            if (connection != null)
                connection.close();
        }

    }

    public static void main(String[] args) throws SQLException, InterruptedException {
        String configFile = "txc-annotation-drds-context.xml";
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(configFile);
        final SampleAnnotationDrds clienttest = (SampleAnnotationDrds) context.getBean("clientTest");
        final DataSource dataSource = (DataSource) context.getBean("txcDataSource"); //drds ds

        Connection connection = dataSource.getConnection();
        Statement statement = connection.createStatement();
        statement.execute("truncate account");

        clienttest.insertTest(dataSource);

        Thread.sleep(30*1000);

        context.close();
        System.exit(0);
    }
}
