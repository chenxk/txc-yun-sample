package com.taobao.txc.tests;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Calendar;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 *A和B两个用户的数据分别位于在一个DRDS实例的两个不同的分库中，用50个进程并发进行A转账给B，每个进程转账10次，每次转账金额在1到10之间随机生成，
 *转账过程中模拟了3%概率的网络异常，使用TXC事务保证了A和B钱的总数始终不变。
 */
public class SampleClientDrds {
	static Random random = new Random();
	
	/*使用了TXC的DRDS数据库转账方法，主要步骤如下
	 * set autocommit=false
	 * select last_txc_xid()
	 * insert/update/delete等业务sql
	 * commit 或者 rollback
	 */
	public void transferAccount(Statement statement1, int tansferMoney) throws SQLException {
		//关闭单机事务
        statement1.execute("set autocommit=false");
        //初始化TXC事务，并对A扣款
        statement1.execute("select last_txc_xid()");
        //查询A的余额是否小于转账金额，即不足以扣款，如果是则抛出A余额不足的异常
        ResultSet resultSet = statement1.executeQuery("select balance from account where cardNum='1' for update");
		int moneyA = 0;
		if (resultSet.next()) {
			moneyA = resultSet.getInt("balance");
		}
        if (moneyA < tansferMoney) {
        	throw new RuntimeException("not enough money in account of A");
		}
        //对A出账
        StringBuilder sb1 = new StringBuilder();
        sb1.append("update account set balance=balance-").append(tansferMoney).append(" where cardNum = 1");
        statement1.execute(sb1.toString());
        //模拟转账过程中3%的网络异常
        if(random.nextInt(100) < 3){
        	throw new RuntimeException("the network is down");
        }
        //对B入账
        StringBuilder sb2 = new StringBuilder();
        sb2.append("update account set balance=balance+").append(tansferMoney).append(" where cardNum = 2");
        statement1.execute(sb2.toString());
		statement1.execute("commit");
    }
    
	public static void main(String[] args) throws SQLException {
		String configFile = "txc-client-context.xml";
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(configFile);	
		int threadCount = 50;
		final AtomicInteger tranSuccessTimes = new AtomicInteger(0);
        final AtomicInteger tranFailTimes = new AtomicInteger(0);
		final SampleClientDrds clientTest = new SampleClientDrds();
		final DataSource dataSource1 = (DataSource) context.getBean("DataSource");
		final Connection connection1 = dataSource1.getConnection();
		Statement statement1 = connection1.createStatement();
		statement1.execute("truncate  account");
		//A和B的账户插入在DRDS的两个分库上，初始余额各10000元
		statement1.execute("trace insert into account(cardNum, name, balance) values(1,'a',10000)");
		ResultSet resultSet = statement1.executeQuery("show trace");
        String dbName1 = "";
		while (resultSet.next()) {
			dbName1 = resultSet.getString("GROUP_NAME");
		}
        System.out.println("A用户的卡记录在"+dbName1+"分库上");        
        statement1.execute("trace insert into account(cardNum, name, balance) values(2,'b',10000)");
		resultSet = statement1.executeQuery("show trace");
		String dbName2 = "";
		while (resultSet.next()) {
			dbName2 = resultSet.getString("GROUP_NAME");
		}
        System.out.println("B用户的卡记录在"+dbName2+"分库上\n");
		// 并发50个线程，每个线程转账10次，金额在1到10之间随机
        final CountDownLatch countDownLatch = new CountDownLatch(threadCount);
        for (int i = 0; i < threadCount; ++i) {
        	final int threadId = i;
			Thread thread = new Thread() {
				public void run() {
					System.out.println(Calendar.getInstance().getTime()+"  :  thread "+threadId+" start!");
					try {
						//每个线程都建立一个connection，并开启事务转账
						Connection connectionTh = dataSource1.getConnection();
						Statement statementTh = connectionTh.createStatement();
						for(int i = 0; i < 10; ++i){
							int tranferMoney = random.nextInt(10) + 1;
							try {
								clientTest.transferAccount(statementTh, tranferMoney);
								System.out.println("thread "+threadId+" info:转账成功,金额为："+tranferMoney+"\n");
								tranSuccessTimes.getAndIncrement();
							} catch (Exception e) {
								statementTh.execute("rollback");
								e.printStackTrace();
								System.out.println("thread "+threadId+" info:转账失败且已经回滚!!,金额为："+tranferMoney+"\n");
								tranFailTimes.getAndIncrement();
							}
						}
						connectionTh.close();
					} catch (SQLException e1) {
						e1.printStackTrace();
					}
					countDownLatch.countDown();
					System.out.println(Calendar.getInstance().getTime()+"  :  thread "+threadId+" end!\n");
				}
			};
			thread.start();
		}
		try {
			countDownLatch.await();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		System.out.println("wait for several seconds  ------------------");
		try {
			Thread.sleep(1 * 10 * 1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		System.out.println("checking result --------------------");
		//打印运行结果
		System.out.println("A用户的卡记录在"+dbName1+"分库上");
		System.out.println("B用户的卡记录在"+dbName2+"分库上");
		System.out.println("成功转账"+tranSuccessTimes.get()+"次，失败转账"+tranFailTimes+"次");
		resultSet = statement1.executeQuery("select balance from account where cardNum='1'");
        int moneyA = 0;
        if (resultSet.next()) {
        	moneyA = resultSet.getInt("balance");
        }
        System.out.println("A原来来有10000元，现在为"+moneyA+"元");
        resultSet = statement1.executeQuery("select balance from account where cardNum='2'");
        int moneyB = 0;
        if (resultSet.next()) {
        	moneyB = resultSet.getInt("balance");
        }
        System.out.println("B原来来有10000元，现在为"+moneyB+"元");
        System.out.println("A和B的金额总和为"+(moneyA+moneyB)+"元");
        connection1.close();
        context.close();
		System.exit(0);
	}
}
