package com.taobao.txc.springboot;

import com.taobao.txc.client.aop.TxcTransactionScaner;
import com.taobao.txc.datasource.cobar.TxcDataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;


@Configuration
public class AccConfig {
    @Bean(name = "primaryDataSource")
    @Qualifier("primaryDataSource")
    @ConfigurationProperties(prefix="spring.datasource.primary")
    public com.taobao.txc.datasource.cobar.TxcDataSource primaryDataSource()
    {
        return new TxcDataSource();
    }


    @Bean(name = "secondaryDataSource")
    @Qualifier("secondaryDataSource")
    @Primary
    @ConfigurationProperties(prefix="spring.datasource.secondary")
    public com.taobao.txc.datasource.cobar.TxcDataSource secondaryDataSource()
    {
        return new TxcDataSource();
    }

    @Bean(name = "primaryJdbcTemplate")
    public JdbcTemplate primaryJdbcTemplate(
            @Qualifier("primaryDataSource") javax.sql.DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

    @Bean(name = "secondaryJdbcTemplate")
    public JdbcTemplate secondaryJdbcTemplate(
            @Qualifier("secondaryDataSource") javax.sql.DataSource dataSource)
    {
        return new JdbcTemplate(dataSource);
    }


    /*
    ////////////////弹内scanner声明//////////////////////////
    //定义声明式事务，要想让事务annotation感知的话，要在这里定义一下
    @Bean(name = "txcScanner")
    public TxcTransactionScaner txcTransactionScaner()
    {
        //app:业务自定义名，由业务方指定，用于区分不同业务
        //txc-group-name txc分组名，请联系TXC项目组索取
        //[0:None (only be client) 1:Default Mode 2:Manual Mode 3:Default Mode & Manual Mode 5:Default Mode & Service Mode 6:Manual Mode & Service Mode 7:Default Mode & Manual Mode &Service Mode]
        return  new TxcTransactionScaner("app","txc-group-name",1);
    }
   */

    ////////////////公有云scanner声明//////////////////////////
    //定义声明式事务，要想让事务annotation感知的话，要在这里定义一下
    @Bean(name = "txcScanner")
    @ConfigurationProperties(prefix="aluser")
    public TxcTransactionScaner txcTransactionScaner()
    {
         //xxxx填写txc的逻辑组名
         return  new TxcTransactionScaner("xxxxx");
    }

}
