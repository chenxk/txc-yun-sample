package com.taobao.txc.tests;

import com.taobao.txc.client.aop.annotation.TxcTransaction;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.CountDownLatch;

/**
 * Created by simiao.zw on 2017/8/15.
 */
public class MtResourceReserveClient {
    @TxcTransaction(timeout = 60000)
    public void runBussiness(int id, int money, MtServiceRollin mtServiceRollin, MtServiceRollout mtServiceRollout) {
        mtServiceRollout.rollout(null, 0, id, money);
        mtServiceRollin.rollin(null, 0, id, money);
    }

    public static void main(String[] args) {
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("mt-client-context.xml");
        int threadCount = 10;
        final int loopTimes = 100;
        final CountDownLatch countDownLatch = new CountDownLatch(threadCount);
        final MtResourceReserveClient clienttest = (MtResourceReserveClient) context.getBean("clientTest");
        final MtServiceRollin mtServiceRollin = (MtServiceRollin) context.getBean("MtServiceBean1");
        final MtServiceRollout mtServiceRollout = (MtServiceRollout) context.getBean("MtServiceBean2");
        final JdbcTemplate jdbcTemplate1 = (JdbcTemplate) context.getBean("jdbcTemplate1");
        final JdbcTemplate jdbcTemplate2 = (JdbcTemplate) context.getBean("jdbcTemplate2");

        System.out.println("init data --------------------------");
        jdbcTemplate1.execute("truncate  account");
        jdbcTemplate2.execute("truncate  account");

        StringBuilder builder = new StringBuilder("insert into account(id, money,reserve_money) values");
        for (int i = 0; i < threadCount * loopTimes; i++)
            builder.append("(").append(i).append(",1000,0),");
        String sql = builder.substring(0, builder.length() -1);
        jdbcTemplate1.update(sql);
        jdbcTemplate2.update(sql);

        System.out.println("mt mode txc begin ------------------");
        for (int i = 0; i < threadCount; ++i) {
            final int threadId = i;
            Thread thread = new Thread() {
                public void run() {
                    System.out.println(Calendar.getInstance().getTime());
                    int id = threadId * loopTimes;
                    for (int i = 0; i < loopTimes; i++) {
                        int money = new Random().nextInt(50)+1;
                        System.out.println(Thread.currentThread().getName()+"("+i+")"+"    accountId:"+(id +i)+
                            "    money:"+money);
                        try {
                            clienttest.runBussiness(id + i, money, mtServiceRollin, mtServiceRollout);
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

        System.out.println("mt mode txc finish -----------------");
        System.out.println("wait for 3 minute ------------------");
        try {
            Thread.sleep(3 * 60 * 1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("checking result --------------------");
        List<Map<String, Object>> m1 = jdbcTemplate1.queryForList("select id, money from account where money > 1000");
        List<Map<String, Object>> m2 = jdbcTemplate2.queryForList("select id, money from account where money < 1000");
        System.out.println("money list on datasource1: " + m1);
        System.out.println("money list on datasource2: " + m2);

        if (m1.size() != m2.size()) {
            System.out.println("the result is wrong. please check.");
        } else {
            for (int i = 0; i < m1.size(); i++) {
                if (((Long) (m1.get(i).get("money"))).intValue() + ((Long) (m2.get(i).get("money"))).intValue() != 2000) {
                    System.out.println("the result is wrong. please check.");
                    System.exit(1);
                }
            }
            System.out.println("the result is good, size:" + m1.size());
        }

        System.out.println("\nyou can check data on mysql with the SQL query: 'select id,money from account;' \n");

        context.close();
        System.exit(0);
    }
}
