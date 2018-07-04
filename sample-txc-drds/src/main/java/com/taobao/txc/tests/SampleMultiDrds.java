package com.taobao.txc.tests;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Random;
import java.util.concurrent.CountDownLatch;

import org.springframework.context.support.ClassPathXmlApplicationContext;

public class SampleMultiDrds {

    static Random random = new Random();

    /*使用了TXC的DRDS数据库转账方法，主要步骤如下
     * set autocommit=false
     * select last_txc_xid()
     * insert/update/delete等业务sql
     * commit 或者 rollback
     */
    public void transferAccount(Connection connection1, Connection connection2, int delta) throws SQLException {
        //关闭单机事务
        try {
            Statement statement1 = connection1.createStatement();
            Statement statement2 = connection2.createStatement();

            connection1.setAutoCommit(false);
            connection2.setAutoCommit(false);

            //初始化TXC事务，并对A扣款
            ResultSet resultSet = statement1.executeQuery("select last_txc_xid()");
            String xid = null;
            while (resultSet.next()) {
                xid = resultSet.getString(1);
            }

            System.out.println("XID: " + xid);
            statement1.execute("update account set balance=balance-" + delta + " where cardNum=1");

            statement2.execute("set TXC_XID='" + xid + "'");
            statement2.execute("update account set balance=balance+" + delta + " where cardNum=2");

            if(random.nextInt(10) < 2){
                throw new RuntimeException("application exception haha!");
            }

            connection2.commit();

            if (random.nextInt(10) < 3) {
                throw new RuntimeException("application after B local commit!");
            }

            connection1.commit();
        } catch (RuntimeException e) {
            e.printStackTrace();
            try {
                connection1.rollback();
            } catch (SQLException e1) {
                e1.printStackTrace();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            try {
                connection1.rollback();
            } catch (SQLException e1) {
                e1.printStackTrace();
            }
        } finally {
            connection1.setAutoCommit(true);
            connection2.setAutoCommit(true);
        }
    }

    public static void main(String[] args) throws SQLException, InterruptedException {
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("txc-multi-drds-context.xml");

        final SampleMultiDrds clientTest = new SampleMultiDrds();
        final DataSource dataSource1 = (DataSource) context.getBean("DataSource1");
        final Connection connection1 = dataSource1.getConnection();
        final DataSource dataSource2 = (DataSource) context.getBean("DataSource2");
        final Connection connection2 = dataSource2.getConnection();

        //init
        Statement statement1 = connection1.createStatement();
        statement1.execute("truncate account");
        statement1.execute("insert into account(cardNum, name, balance) values(1, 'AAA', 1000)");

        Statement statement2 = connection2.createStatement();
        statement2.execute("truncate account");
        statement2.execute("insert into account(cardNum, name, balance) values(2, 'BBB', 0)");

        int threadCount = 10;
        final CountDownLatch countDownLatch = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            try {
                clientTest.transferAccount(connection1, connection2, 100);
            } catch (Exception e) {
                e.printStackTrace();
            }
            countDownLatch.countDown();
        }
        countDownLatch.await();

        System.out.println("wait for several seconds  ------------------");
        try {
            Thread.sleep(1 * 10 * 1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        ResultSet resultSet1 = statement1.executeQuery("select balance from account where cardNum=1");
        resultSet1.next();
        int balance1 = resultSet1.getInt(1);

        ResultSet resultSet2 = statement2.executeQuery("select balance from account where cardNum=2");
        resultSet2.next();
        int balance2 = resultSet2.getInt(1);

        if (balance1 + balance2 != 1000) {
            System.out.println("check money failed! A:" + balance1 + " B:" + balance2);
        } else {
            System.out.println("check money OK 1000 A:" + balance1 + " B:" + balance2);
        }

        context.close();
        System.exit(0);
    }
}
