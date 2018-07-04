package com.springMyBatis.system.test;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import javax.sql.DataSource;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.transaction.annotation.Transactional;

import com.springMyBatis.system.dao.UserDao;
import com.springMyBatis.system.model.User;

public class UserController {
    
	//用spring的tx来管理分布式事务txc的提交和回滚
	@Transactional
	public void insert(UserDao userDao){
		User user=new User();
		//在DRDS上开启txc事务，在UserDao.xml中包装了select last_txc_xid() 
		userDao.enableTXC();
        user.setId(1);
        userDao.insert(user);
        user.setId(2);
        userDao.insert(user);
        user.setId(3);
        userDao.insert(user);
        //抛出异常，插入回滚
        throw new RuntimeException("I make an error here!");
	}
    /**
     * @param args
     * @throws InterruptedException 
     * @throws SQLException
     * 本案例需要在DRDS上建表的语句 ： create table test1(id int, name varchar(30), primary key(id)) dbpartition by hash(id);
     */
    public static void main(String[] args) throws InterruptedException, SQLException {
        @SuppressWarnings("resource")
		ApplicationContext ctx = new ClassPathXmlApplicationContext("ApplicationContext.xml");
        UserDao userDao=(UserDao) ctx.getBean("userDao");
        //清空test1数据
        final DataSource dataSource = (DataSource) ctx.getBean("jdbcDataSource1");
		final Connection connection = dataSource.getConnection();
		Statement statement = connection.createStatement();
		statement.execute("truncate  test1");
		//测试DRDS上的TXC事务
        UserController userController=(UserController) ctx.getBean("UserController");
        userController.insert(userDao);
        Thread.sleep(5*1000);
        System.out.println("-----------ok---------");
        System.exit(0);
    }
}
