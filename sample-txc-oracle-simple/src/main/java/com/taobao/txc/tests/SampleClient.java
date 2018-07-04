package com.taobao.txc.tests;

import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicLong;

import com.taobao.txc.client.aop.annotation.TxcTransaction;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;

public class SampleClient {

    /**
     * TxcTransaction声明过的方法就是一个分布式事务啦，是不是很简单 o(∩_∩)o <br>
     * 在两个库上各写一次，不用分布式事务的话，第一个成功，第二个失败会挂的噢 <br>
     */
    @TxcTransaction(timeout = 60000 * 3)
    public void runBussiness(AtomicLong id, JdbcTemplate jdbcTemplate1, JdbcTemplate jdbcTemplate2) {
        long nextId = id.getAndIncrement();
        jdbcTemplate1.update("insert into ids2 (id) values (?)", new Object[] { nextId });
        jdbcTemplate2.update("insert into ids2 (id) values (?)", new Object[] { nextId });
    }

    public static void main(String[] args) {
        String configFile = "txc-cobar-context-oracle.xml";
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(configFile);
        int threadCount = 10;
        final CountDownLatch countDownLatch = new CountDownLatch(threadCount);
        final SampleClient clienttest = (SampleClient) context.getBean("clientTest");
        final JdbcTemplate jdbcTemplate1 = (JdbcTemplate) context.getBean("jdbcTemplate1");
        final JdbcTemplate jdbcTemplate2 = (JdbcTemplate) context.getBean("jdbcTemplate2");
        jdbcTemplate1.execute("delete from ids2");
        jdbcTemplate2.execute("delete from ids2");

        // 50个线程，各跑100个分布式事务，看看能不能搞死他~~~~~
        for (int i = 0; i < threadCount; ++i) {
            final int threadId = i;
            Thread thread = new Thread() {
                public void run() {
                    AtomicLong id = new AtomicLong(threadId * 1000 + 10000);
                    System.out.println(Calendar.getInstance().getTime());
                    for (int i = 0; i < 5; i++) {
                        try {
                            clienttest.runBussiness(id, jdbcTemplate1, jdbcTemplate2);
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

        System.out.println("at mode txc finish -----------------");

        System.out.println("wait for 1 minute  ------------------");
        try {
            Thread.sleep(1 * 60 * 1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("checking result --------------------");

        List<Map<String, Object>> ids1 = jdbcTemplate1.queryForList("select id from ids2");
        List<Map<String, Object>> ids2 = jdbcTemplate2.queryForList("select id from ids2");
        System.out.println("id list on datasource1 :" + ids1.size());
        System.out.println("id list on datasource2 :" + ids2.size());

        if (ids1.size() == ids2.size() && ids2.containsAll(ids1) && ids1.containsAll(ids2)) {
            System.out.println("the result is good.");
        } else {
            System.out.println("the result is wrong. please check.");
        }

        context.close();
        System.exit(0);
    }
}
