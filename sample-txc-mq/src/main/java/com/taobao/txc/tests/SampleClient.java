package com.taobao.txc.tests;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Calendar;
import javax.sql.DataSource;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import com.taobao.txc.client.aop.annotation.TxcTransaction;
import com.taobao.txc.rm.mq.TxcMQProducer;
import com.aliyun.openservices.shade.com.alibaba.rocketmq.client.exception.MQClientException;
import com.aliyun.openservices.shade.com.alibaba.rocketmq.client.producer.SendResult;
import com.aliyun.openservices.shade.com.alibaba.rocketmq.common.message.Message;

/**
 *本sample的逻辑主要是A转账给B了10次，部分成功，部分失败，其中A和B分别在两个数据库中，使用TXC事务保证了A和B的钱总数始终不变。
 *每转账成功一次，mq的provider会发送一个massage。失败的转账message不发送。
 */
public class SampleClient {
	static TxcMQProducer txcMQProducer;
	final static String configFile = "txc-mq-client-context.xml";
	final static ClassPathXmlApplicationContext context=new ClassPathXmlApplicationContext(configFile);

	/**
	 * TxcTransaction声明过的方法就是一个分布式事务啦，是不是很简单 o(∩_∩)o <br>
	 * 在两个库上各写一次，不用分布式事务的话，第一个成功，第二个失败会挂的噢 <br>
	 */
	@TxcTransaction(appName = "myapp3")
	public void update(Connection connection1, Connection connection2, int money) throws SQLException, MQClientException {		
		//1号库减钱
		Statement statement1 = connection1.createStatement();
		StringBuilder sb1 = new StringBuilder();
		sb1.append("update user_money_a set money=money-").append(money);
		statement1.execute(sb1.toString());
		statement1 = connection1.createStatement();
		//2号库加钱
		Statement statement2 = connection2.createStatement();
		StringBuilder sb2 = new StringBuilder();
		sb2.append("update user_money_b set money=money+").append(money);
		statement2.execute(sb2.toString());
		//查a和b的余额
		ResultSet resultSet1 = statement1.executeQuery("select * from user_money_a");	
		int decMoney = 0;
		if (resultSet1.next()) {
			decMoney = resultSet1.getInt("money");
		}
		ResultSet resultSet2 = statement2.executeQuery("select * from user_money_b");	
		int incMoney = 0;
		if (resultSet2.next()) {
			incMoney = resultSet2.getInt("money");
		}
		//发送一个消息通知已经转账
		Message msg = new Message(/*topic*/"txc_mq_test",
                /*tag*/"onePay",
                String.format("onePay message:success transfer 20 from a to b. Now a has "+decMoney+". b has "+incMoney+"\n").getBytes());
        /* 事务完成消息可见 */
        SendResult sendResult = txcMQProducer.send(null, 0, msg);
        //console打印消息内容
  		System.out.println(String.format("send msgId:%s status:%s broker:%s qid:%d, topic:%s",
                  sendResult.getMsgId(), sendResult.getSendStatus(),
                  sendResult.getMessageQueue().getBrokerName(),
                  sendResult.getMessageQueue().getQueueId(),
                  sendResult.getMessageQueue().getTopic()));
  		//判断1号库剩余钱是否小于0，小于0则回滚。
		System.out.println("A的中间状态为："+decMoney+"元");
		if (decMoney < 0) {
			throw new RuntimeException("not enough money. 回滚到事务前状态");
		}
	}

	public static void main(String[] args) throws SQLException, MQClientException {	
		int count = 10;
		final SampleClient clienttest = (SampleClient) context.getBean("clientTest");
		final DataSource dataSource1 = (DataSource) context.getBean("txcDataSource1");
		final DataSource dataSource2 = (DataSource) context.getBean("txcDataSource2");
		final Connection connection1 = dataSource1.getConnection();
		final Connection connection2 = dataSource2.getConnection();
		Statement statement1 = connection1.createStatement();
		Statement statement2 = connection2.createStatement();
		statement1.execute("truncate  user_money_a");
		statement2.execute("truncate  user_money_b");
		//每个账户初始值为100元
		statement1.execute("insert into user_money_a(money) values(100)");
		statement2.execute("insert into user_money_b(money) values(100)");
		//启动mq
		txcMQProducer = (TxcMQProducer) context.getBean("txc_mq_producer");
        txcMQProducer.start();
        System.out.println("Producer started!");
        
		// 10次扣款，每次20元
		for (int i = 0; i < count; ++i) {
			System.out.println("");
			System.out.println(Calendar.getInstance().getTime());
            //完成一次转账
			try {
				clienttest.update(connection1, connection2, 20);
			} 
			catch (Exception e) {
				System.out.println(e.getMessage());
			}
			System.out.println("本次转账结束，A和B的余额为：");
			//每操作完一次转账，查看A和B的余额
			statement1 = connection1.createStatement();
	        ResultSet resultSet1 = statement1.executeQuery("select * from user_money_a");
	        int Dec_Money = 0;
	        if (resultSet1.next()) {
	        	Dec_Money = resultSet1.getInt("money");
	        }
	        System.out.println("A账户现在余额为"+Dec_Money+"元");
	        statement2 = connection2.createStatement();
	        ResultSet resultSet2 = statement2.executeQuery("select * from user_money_b");
	        int Inc_Money = 0;
	        if (resultSet2.next()) {
	        	Inc_Money = resultSet2.getInt("money");
	        }
	        System.out.println("B账户现在余额为"+Inc_Money+"元");
	        System.out.println("A和B的金额总和为"+(Inc_Money+Dec_Money)+"元");
		}
		System.out.println("wait for several seconds  ------------------");
		try {
			Thread.sleep(1 * 10 * 1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		System.out.println("checking result --------------------");
		statement1 = connection1.createStatement();
        ResultSet resultSet1 = statement1.executeQuery("select * from user_money_a");
        int decMoney = 0;
        if (resultSet1.next()) {
        	decMoney = resultSet1.getInt("money");
        }
        System.out.println("A原来来有100元，现在为"+decMoney+"元");
        statement2 = connection2.createStatement();
        ResultSet resultSet2 = statement2.executeQuery("select * from user_money_b");
        int incMoney = 0;
        if (resultSet2.next()) {
        	incMoney = resultSet2.getInt("money");
        }
        System.out.println("B原来来有100元，现在为"+incMoney+"元");
        System.out.println("A和B的金额总和为"+(incMoney+decMoney)+"元");
        context.close();
		System.exit(0);
	}
}
