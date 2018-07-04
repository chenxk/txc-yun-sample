package com.taobao.txc.tests;

import com.aliyun.openservices.shade.com.alibaba.rocketmq.client.exception.MQClientException;
import com.taobao.hsf.standalone.HSFEasyStarter;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import java.sql.SQLException;

/**
 * txc+edas+mq的console版main函数
 */
public class TestMain {
    public static void main(String[] args) throws SQLException, MQClientException {
        System.out.println("container start...");
        HSFEasyStarter.startWithPathAndIdentifier(System.getenv("HOME") + "/edas-agent/temp", "edas.sar.V2.8.2");
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("txc-2-client-rds.xml");
        System.out.println("new Client...");
        Client client = (Client) context.getBean("bussiness");
        System.out.println("client.run...");
        client.reset();
        System.out.println("Pay1---");
        try {
            client.pay1();
            client.showOK();
        } catch (Exception e) {
            client.showError(e);
        }
        System.out.println("Pay2---");
        try {
            client.pay2();
            client.showOK();
        } catch (Exception e) {
            client.showError(e);
        }
        context.close();
        System.exit(0);
    }
}
