package com.taobao.txc.tests;

import java.sql.SQLException;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import com.taobao.txc.client.aop.annotation.TxcTransaction;
import com.taobao.txc.common.TxcContext;

/**
 * txc+edas的client代码
 */
public class Client implements ApplicationContextAware{
    private IIncMoneyService incMoneyService;
    private IDecMoneyService decMoneyService;
    private IUpdateLevelService updateLevelService;
    ApplicationContext applicationContext;

    public void init() {
        incMoneyService = (IIncMoneyService) applicationContext.getBean("incMoneyService_rds");
        decMoneyService = (IDecMoneyService) applicationContext.getBean("decMoneyService_rds");
        updateLevelService = (IUpdateLevelService) applicationContext.getBean("updateLevelService_rds");
        System.out.println("incMoneyService: " + incMoneyService);
        System.out.println("decMoneyService: " + decMoneyService);
        System.out.println("updateLevelService: " + updateLevelService);
    }

    public void init(ApplicationContext applicationContext) {
        incMoneyService = (IIncMoneyService) applicationContext.getBean("incMoneyService_rds");
        decMoneyService = (IDecMoneyService) applicationContext.getBean("decMoneyService_rds");
        updateLevelService = (IUpdateLevelService) applicationContext.getBean("updateLevelService_rds");
        System.out.println("incMoneyService: " + incMoneyService);
        System.out.println("decMoneyService: " + decMoneyService);
        System.out.println("updateLevelService: " + updateLevelService);
    }

    @TxcTransaction(appName = "myapp3")
    public void pay1() throws SQLException {
        System.out.println("XID: " + TxcContext.getCurrentXid());
        //add
        incMoneyService.incMoney(500);
        //dec
        decMoneyService.decMoney(500);
    }

    public void showOK() throws SQLException {
        System.out.println("OK:");
        System.out.println("inc_money: " + incMoneyService.getMoney());
        System.out.println("dec_money: " + decMoneyService.getMoney());
        System.out.println("level: " + updateLevelService.getLevel());
        System.out.println();
    }

    public void showError(Exception e) throws SQLException {
        System.out.println("ERROR:");
        System.out.println("inc_money: " + incMoneyService.getMoney());
        System.out.println("dec_money: " + decMoneyService.getMoney());
        System.out.println("level: " + updateLevelService.getLevel());
        System.out.println(e.getMessage());
        System.out.println();
    }

    @TxcTransaction(appName = "myapp3", timeout = 300000)
    public void pay2() throws SQLException {
        System.out.println("XID: " + TxcContext.getCurrentXid());
        //add
        incMoneyService.incMoney(1500);
        //dec
        decMoneyService.decMoney(1500);
    }

    public void reset() throws SQLException {
        System.out.println("reset...");
        incMoneyService.resetMoney(0);
        decMoneyService.resetMoney(1000);
        updateLevelService.resetLevel(0);
        System.out.println("INIT:");
        System.out.println("inc_money: " + incMoneyService.getMoney());
        System.out.println("dec_money: " + decMoneyService.getMoney());
        System.out.println("level: " + updateLevelService.getLevel());
        System.out.println();
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
