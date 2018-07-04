package com.taobao.txc.tests;

import com.taobao.txc.client.aop.annotation.TxcTransaction;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.Calendar;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

/**
 * Created by simiao.zw on 2017/8/15.
 * Modify by qingfeng on 2017/10/29
 */
public class MtResourceCompensateClient {
    @TxcTransaction(timeout = 60000)
    public void runBussiness(int id, MtServiceRollin MtServiceRollin, MtServiceRollout MtServiceRollout) {
        int pid = 2000;//商品编号
        int pnum = simulation(1,20);//商品数量
        System.out.println("####pnum = "+pnum);
        if(pnum > 19)pnum = 1500;//模拟大额订单,5%的概率
        MtServiceRollout.rollout(null, 0, 1000, pnum);
        MtServiceRollin.rollin(null, 0, id, 1000,1000,pnum);
    }

    public static void main(String[] args) {
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("mt-client-context.xml");
        int threadCount = 5;
        final CountDownLatch countDownLatch = new CountDownLatch(threadCount);
        final MtResourceCompensateClient clienttest = (MtResourceCompensateClient) context.getBean("clientTest");
        final MtServiceRollin mtServiceRollin = (MtServiceRollin) context.getBean("MtServiceBean1");
        final MtServiceRollout mtServiceRollout = (MtServiceRollout) context.getBean("MtServiceBean2");
        final JdbcTemplate jdbcTemplate1 = (JdbcTemplate) context.getBean("jdbcTemplate1");
        final JdbcTemplate jdbcTemplate2 = (JdbcTemplate) context.getBean("jdbcTemplate2");

        System.out.println("init data --------------------------");

        jdbcTemplate1.execute("truncate  orders");
        jdbcTemplate2.execute("truncate  stock");

        jdbcTemplate2.update("insert into stock values(1000,'book',50,1000,'Thinking in java')");
        System.out.println("mt mode txc begin ------------------");
        for (int i = 0; i < threadCount; ++i) {
            final int threadId = i;
            Thread thread = new Thread() {
                public void run() {
                    System.out.println(Calendar.getInstance().getTime());
                    int id = threadId * 100;
                    for (int i = 0; i < 20; i++) {
                        try {
                            clienttest.runBussiness(id + i, mtServiceRollin, mtServiceRollout);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    countDownLatch.countDown();
                    System.out.println(Calendar.getInstance().getTime());
                }
            };
            thread.start();
        }

        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Map map = jdbcTemplate1.queryForMap("select sum(pnum) as total from orders");
        //int total = (int) map.get("total");
        Object ob = map.get("total");
        int pnum=Integer.parseInt(ob.toString());
        System.out.println("******total:"+pnum);
        Map map2 = jdbcTemplate2.queryForMap("select number  from stock");
        int number =(int) map2.get("number");
        if((pnum+number)==1000)
        {
            System.out.println("the result is good");
        }
        else
        {
            System.out.println("the result is wrong. please check.");
        }
        context.close();
        System.exit(0);
    }

    public static int simulation(int min,int max)
    {
        return  new java.util.Random().nextInt(max) % (max - min + 1) + min;
    }
}
