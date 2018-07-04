package com.taobao.txc.tests;

import com.taobao.txc.client.TxcTransaction;
import com.taobao.txc.datasource.cobar.TxcDataSource;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Created by simiao.zw on 2017/2/22.
 */
public class SpringFreeClient {
    public static void main(String[] args) throws SQLException {
        String vgroup = "xxx";
        String ak = "xxx";
        String sk = "xxx";

        TxcDataSource datasource1 = new TxcDataSource();
        datasource1.setUrl("jdbc:mysql://xxx:3306/sample");
        datasource1.setUsername("xxx");
        datasource1.setPassword("xxx");
        datasource1.setDriverClassName("com.mysql.jdbc.Driver");

        TxcDataSource datasource2 = new TxcDataSource();
        datasource2.setUrl("jdbc:mysql://xxx:3306/sample");
        datasource2.setUsername("xxx");
        datasource2.setPassword("xxx");
        datasource2.setDriverClassName("com.mysql.jdbc.Driver");

        TxcTransaction txcTransaction = TxcTransaction.getInstance(vgroup, ak, sk);

        Statement user1 = datasource1.getConnection().createStatement();
        Statement user2 = datasource2.getConnection().createStatement();

        user1.executeUpdate("truncate user_money");
        user2.executeUpdate("truncate user_money");

        user1.executeUpdate("insert into user_money(userId, money) values(1, 1000)");
        user2.executeUpdate("insert into user_money(userId, money) values(2, 0)");

        int rollbackcount = 5;
        while (true) {
            try {
                txcTransaction.begin(30000);

                user1.executeUpdate("update user_money set money=money-100 where userId=1");
                user2.executeUpdate("update user_money set money=money+100 where userId=2");

                ResultSet resultSet = user1.executeQuery("select count(*) _records from user_money where money < 0 and userId=1");
                resultSet.next();
                int num = resultSet.getInt("_records");
                if (num > 0) {
                    System.out.println("A账户没钱了，回滚!");
                    txcTransaction.rollback();
                    if (rollbackcount-- < 0) {
                        System.out.println("等待结束...");
                        Thread.sleep(60000);
                        System.exit(-1);
                    }
                } else {
                    System.out.println("A账户有钱，继续!");
                    txcTransaction.commit();
                }
            } catch (Throwable e) {
                txcTransaction.rollback();
                e.printStackTrace();
                System.exit(-1);
            }
        }
    }
}
