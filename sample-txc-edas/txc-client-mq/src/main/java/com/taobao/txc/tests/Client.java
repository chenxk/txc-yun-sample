package com.taobao.txc.tests;

import com.aliyun.openservices.shade.com.alibaba.rocketmq.client.exception.MQClientException;
import com.aliyun.openservices.shade.com.alibaba.rocketmq.client.producer.SendResult;
import com.aliyun.openservices.shade.com.alibaba.rocketmq.common.message.Message;
import com.taobao.txc.client.aop.annotation.TxcTransaction;
import com.taobao.txc.common.TxcContext;
import com.taobao.txc.rm.mq.TxcMQProducer;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import java.sql.SQLException;

/**
 * txc+edas+mq的client类实现
 */
public class Client implements ApplicationContextAware{
    private IIncMoneyService incMoneyService;
    private IDecMoneyService decMoneyService;
    private IUpdateLevelService updateLevelService;
    ApplicationContext applicationContext;
    TxcMQProducer txcMQProducer;
    
    private TxcMQProducer createSMS() throws MQClientException {
        TxcMQProducer txcMQProducer = (TxcMQProducer) applicationContext.getBean("txc_mq_producer");
        txcMQProducer.start();
        System.out.println("Producer started!");
        return txcMQProducer;
    }

    public void init() throws MQClientException {
        incMoneyService = (IIncMoneyService) applicationContext.getBean("incMoneyService_rds");
        decMoneyService = (IDecMoneyService) applicationContext.getBean("decMoneyService_rds");
        updateLevelService = (IUpdateLevelService) applicationContext.getBean("updateLevelService_rds");
        System.out.println("incMoneyService: " + incMoneyService);
        System.out.println("decMoneyService: " + decMoneyService);
        System.out.println("updateLevelService: " + updateLevelService);
        txcMQProducer = createSMS();
    }

    public void init(ApplicationContext applicationContext) throws MQClientException {
        incMoneyService = (IIncMoneyService) applicationContext.getBean("incMoneyService_rds");
        decMoneyService = (IDecMoneyService) applicationContext.getBean("decMoneyService_rds");
        updateLevelService = (IUpdateLevelService) applicationContext.getBean("updateLevelService_rds");
        System.out.println("incMoneyService: " + incMoneyService);
        System.out.println("decMoneyService: " + decMoneyService);
        System.out.println("updateLevelService: " + updateLevelService);
        txcMQProducer = createSMS();
    }

    @TxcTransaction(appName = "myapp3")
    public void pay1() throws SQLException, MQClientException {
        System.out.println("XID: " + TxcContext.getCurrentXid());
        //OK
        Message msg = new Message(/*topic*/"txc_mq_test",
                /*tag*/"pay1",
                String.format("pay1 message : success transfer 500").getBytes());
        /* 事务完成消息可见 */
        SendResult sendResult = txcMQProducer.send(null, 0, msg);
        System.out.println(String.format("pay1 send msgId:%s status:%s broker:%s qid:%d, topic:%s",
                sendResult.getMsgId(), sendResult.getSendStatus(),
                sendResult.getMessageQueue().getBrokerName(),
                sendResult.getMessageQueue().getQueueId(),
                sendResult.getMessageQueue().getTopic()));
        //add
        incMoneyService.incMoney(500);
        //dec
        decMoneyService.decMoney(500);
    }

    @TxcTransaction(appName = "myapp3", timeout = 300000)
    public void pay2() throws SQLException, MQClientException {
        System.out.println("XID: " + TxcContext.getCurrentXid());
        Message msg = new Message(/*topic*/"txc_mq_test",
                /*tag*/"pay2",
                String.format("pay2 message : success transfer 1500").getBytes());
        SendResult sendResult = txcMQProducer.send(null, 0, msg);
        System.out.println(String.format("pay2 send msgId:%s status:%s broker:%s qid:%d, topic:%s",
                sendResult.getMsgId(), sendResult.getSendStatus(),
                sendResult.getMessageQueue().getBrokerName(),
                sendResult.getMessageQueue().getQueueId(),
                sendResult.getMessageQueue().getTopic()));
        //add
        incMoneyService.incMoney(1500);
        //dec
        decMoneyService.decMoney(1500);
    }

    public void reset() throws SQLException {
        System.out.println("reset...");
        incMoneyService.resetMoney(0);
        decMoneyService.resetMoney(1000);
        updateLevelService.resetLevel(0);
        System.out.println("INIT:");
        System.out.println("inc_money: " + incMoneyService.getMoney());
        System.out.println("dec_money: " + decMoneyService.getMoney());
        System.out.println("level: " + updateLevelService.getLevel());
        System.out.println();
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    public void showOK() throws SQLException {
        System.out.println("OK:");
        System.out.println("inc_money: " + incMoneyService.getMoney());
        System.out.println("dec_money: " + decMoneyService.getMoney());
        System.out.println("level: " + updateLevelService.getLevel());
        System.out.println();
    }

    public void showError(Exception e) throws SQLException {
        System.out.println("ERROR:");
        System.out.println("inc_money: " + incMoneyService.getMoney());
        System.out.println("dec_money: " + decMoneyService.getMoney());
        System.out.println("level: " + updateLevelService.getLevel());
        System.out.println(e.getMessage());
        System.out.println();
    }
}
