package com.taobao.txc.springboot;


import com.taobao.txc.client.aop.annotation.TxcTransaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class AccDAO {
    @Autowired
    @Qualifier("primaryJdbcTemplate")
    private JdbcTemplate jdbcTemplate1;

    @Autowired
    @Qualifier("secondaryJdbcTemplate")
    private JdbcTemplate jdbcTemplate2;
    public void reset( int  money) {

        jdbcTemplate2.execute("truncate account");
        jdbcTemplate1.execute("truncate account");
        jdbcTemplate1.update("insert into account(money) values(?)", money);
        jdbcTemplate2.update("insert into account(money) values(0)");

    }
    @TxcTransaction
    public void transferAccount(int money) throws RuntimeException {
        jdbcTemplate2.update("update account set money = money + ?",money);
        int acc_money = getMoney1().intValue();
        if(money > acc_money)
        {
            throw new RuntimeException("The account balance is not enough");
        }
        jdbcTemplate1.update("update account set money = money - ?",money);
    }

    public Integer getMoney1()
    {
        return  jdbcTemplate1.queryForObject("select money from account",Integer.class);
    }
    public Integer getMoney2()
    {
        return  jdbcTemplate2.queryForObject("select money from account",Integer.class);
    }




}
