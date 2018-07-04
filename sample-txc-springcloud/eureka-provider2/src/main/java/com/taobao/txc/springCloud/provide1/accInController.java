package com.taobao.txc.springCloud.provide1;


import com.taobao.txc.common.TxcContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

@RestController
@Repository
public class accInController {

    @Autowired
    @Qualifier("secondaryJdbcTemplate")
    private JdbcTemplate jdbcTemplate1;



    @RequestMapping(value="/add",method= RequestMethod.GET)
    public String accIn(HttpServletRequest request) throws IOException {
        String xid = request.getHeader("xid");
        System.out.println("xid="+request.getHeader("xid"));
        String str_money = request.getParameter("money");
        System.out.println("money:"+str_money);
        TxcContext.bind(xid,null);
        long startTime=System.currentTimeMillis();   //获取开始时间
        jdbcTemplate1.update("update account set money = money + ? where id = 1",str_money);
        long endTime=System.currentTimeMillis(); //获取结束时间
        System.out.println("运行时间： "+(endTime-startTime)+"ms");


        TxcContext.unbind();
        return "1" ;
    }

    @RequestMapping(value="/getmoney",method= RequestMethod.GET)
    public int getSum(HttpServletRequest request)
    {
        Integer money = jdbcTemplate1.queryForObject("select money from account",Integer.class);
        return  money.intValue();
    }

    @RequestMapping(value="/resetacc",method= RequestMethod.GET)
    public int resetacc(HttpServletRequest request)
    {
        jdbcTemplate1.update("truncate table account");
        jdbcTemplate1.update("insert into account(id,money) VALUES (1,0)");
        return  1;
    }

}
