package com.taobao.txc.tests;

import java.util.Properties;

import com.aliyun.openservices.ons.api.Action;
import com.aliyun.openservices.ons.api.ConsumeContext;
import com.aliyun.openservices.ons.api.Consumer;
import com.aliyun.openservices.ons.api.Message;
import com.aliyun.openservices.ons.api.MessageListener;
import com.aliyun.openservices.ons.api.ONSFactory;
import com.aliyun.openservices.ons.api.PropertyKeyConst;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Created by simiao.zw on 2016/8/10.
 */
public class SMSListener {
    private static Log log = LogFactory.getLog(SMSListener.class);
    
    public SMSListener() {
        // xxx 请用私有账户下的资源进行替换
        Properties properties = new Properties();
        properties.put(PropertyKeyConst.ConsumerId, "CID_xxx");
        properties.put(PropertyKeyConst.AccessKey, "xxx");
        properties.put(PropertyKeyConst.SecretKey, "xxx");
        Consumer consumer = ONSFactory.createConsumer(properties);
        consumer.subscribe("xxx", "*", new MessageListener() {
            @Override
            public Action consume(Message message, ConsumeContext context) {
                log.info(String.format("SMSListener got message:%s", message));
                System.out.println(String.format("SMSListener got message:%s", message));
                System.out.println(new String(message.getBody()));
                return Action.CommitMessage;
            }
        });
        consumer.start();
        System.out.println("Consumer started!");
        log.info("Consumer started!");
    }
    
    public static void main(String[] args) {
        new SMSListener();
        System.out.println("wait listener got message...");
    }
}
