package com.taobao.txc.dubbo;

import com.alibaba.dubbo.rpc.RpcContext;
import com.taobao.txc.common.TxcContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;

public class OrderServiceImpl implements OrderService {

    private static JdbcTemplate jdbcTemplate = null;

    public int createOrder(OrderDO orderDO) {
        System.out.println("createOrder is called.");
        String xid = RpcContext.getContext().getAttachment("xid");
        TxcContext.bind(xid,null);
        String sql = "insert into orders(user_id,product_id,number,gmt_create) values(?, ?, ?, ?)";
        int ret = jdbcTemplate.update(sql, new Object[]{orderDO.getUserId(), orderDO.getProductId(), orderDO.getNumber(), orderDO.getGmtCreate()});
        TxcContext.unbind();
        System.out.println("createOrder success.");
        return ret;
    }

    public Integer getSum(String userId) {
        System.out.println("getSum is called.");
        Integer sum = jdbcTemplate.queryForObject("select IF(ISNULL(SUM(number)), 0, SUM(number)) from orders where user_id = ?",
                new Object[]{userId}, java.lang.Integer.class);
        System.out.println("sum:" + sum);
        return sum;
    }

    public static void main(String []args) throws Exception {
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(new String[]{"dubbo-order-service.xml"});
        jdbcTemplate = (JdbcTemplate)context.getBean("jdbcTemplate");
        System.out.println("OrderService is running.");
        System.in.read();
    }
}
