package com.taobao.txc.dubbo;

import com.alibaba.dubbo.rpc.RpcContext;
import com.taobao.txc.common.TxcContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.Random;

public class StockServiceImpl implements StockService {

    private static JdbcTemplate jdbcTemplate = null;

    public int updateStock(OrderDO orderDO) {
        System.out.println("updateStock is called.");
        //获取全局事务ID，并绑定到上下文
        String xid = RpcContext.getContext().getAttachment("xid");
        TxcContext.bind(xid,null);
        int ret = jdbcTemplate.update("update stock set amount = amount - ? where product_id = ?",
                new Object[]{orderDO.getNumber(), orderDO.getProductId()});

        Integer amount = jdbcTemplate.queryForObject("select amount from stock where product_id = ?",
                new Object[]{orderDO.getProductId()}, java.lang.Integer.class);
        if (amount < 0) {
            ret = -1;
            throw new RuntimeException("product："+orderDO.getProductId()+" is not enough.");
        }
        TxcContext.unbind();
        System.out.println("updateStock success.");
        return ret;
    }
    //获取总库存
    public Integer getSum() {

        System.out.println("getSum is called.");
        Integer sum = jdbcTemplate.queryForObject("select IF(ISNULL(SUM(amount)), 0, SUM(amount)) from stock", java.lang.Integer.class);
        System.out.println("sum:" + sum);
        return sum;
    }

    public static void main(String []args) throws Exception{
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(new String[]{"dubbo-stock-service.xml"});
        jdbcTemplate = (JdbcTemplate)context.getBean("jdbcTemplate");
        jdbcTemplate.update("truncate stock ");
        for (int i = 0;i < 1000;i++) {
            jdbcTemplate.update("insert into stock values(?, ?, ?)", new Object[]{i, new Random().nextInt(100), 100000});
        }
        System.out.println("StockServie is running.");
        System.in.read();
    }
}
