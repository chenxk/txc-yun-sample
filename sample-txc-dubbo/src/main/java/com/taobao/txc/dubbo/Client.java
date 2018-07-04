package com.taobao.txc.dubbo;

import com.alibaba.dubbo.rpc.RpcContext;
import com.taobao.txc.client.aop.annotation.TxcTransaction;
import com.taobao.txc.common.TxcContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.sql.Timestamp;
import java.util.Date;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;

public class Client {
    @TxcTransaction(timeout = 60000 * 3)
    public void Bussiness(OrderService orderService, StockService stockService, String userId) {
        String xid = TxcContext.getCurrentXid();
        //通过RpcContext将xid传到服务端
        RpcContext.getContext().setAttachment("xid", xid);
        int productId = new Random().nextInt(1000);
        int productNumber = new Random().nextInt(5)+1;
        OrderDO orderDO = new OrderDO(userId, productId, productNumber, new Timestamp(new Date().getTime()));
        orderService.createOrder(orderDO);
        if (new Random().nextInt(100) < 1) {
            throw new RuntimeException("error");
        }

        RpcContext.getContext().setAttachment("xid",xid);
        stockService.updateStock(orderDO);
        if (new Random().nextInt(100) < 3) {
            throw new RuntimeException("error");
        }
        System.out.println("product id："+ productId +" number：" + productNumber);
    }

    public static void main(String args[]) throws Exception {
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(new String[]{"dubbo-client.xml"});
        final OrderService orderService = (OrderService)context.getBean("OrderService");
        final StockService stockService = (StockService)context.getBean("StockService");
        final Client client = (Client)context.getBean("client");

        int previousAmount = stockService.getSum().intValue();
        final String userId = UUID.randomUUID().toString();
        int threadNum = 2;
        final CountDownLatch countDownLatch = new CountDownLatch(threadNum);
        for (int tnum = 0;tnum < threadNum;tnum++) {
            Thread thread = new Thread() {
                @Override
                public void run() {
                    for (int i = 0;i < 100;i++) {
                        try {
                            client.Bussiness(orderService, stockService, userId);
                        } catch (Exception e) {
                            System.out.println("Transaction is rollbacked.");
                            e.printStackTrace();
                        }
                    }
                    countDownLatch.countDown();
                }
            };
            thread.start();
        }
        countDownLatch.await();

        int productNumber = orderService.getSum(userId).intValue();
        int currentAmount = stockService.getSum().intValue();
        if (previousAmount == (productNumber + currentAmount)) {
            System.out.println("The result is right.");
        } else {
            System.out.println("The result is wrong.");
        }
    }
}
