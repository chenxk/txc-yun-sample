package com.taobao.txc.tests;

import com.taobao.txc.client.aop.annotation.TxcTransaction;
import com.taobao.txc.common.TxcContext;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.web.servlet.ModelAndView;

import java.sql.SQLException;

/**
 * Created by simiao.zw on 2016/8/3.
 */
public abstract class BussinessBase implements ApplicationContextAware {
    protected IIncMoneyService incMoneyService;
    protected IDecMoneyService decMoneyService;
    protected IUpdateLevelService updateLevelService;
    protected ApplicationContext applicationContext;

    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext =  applicationContext;
    }

    @TxcTransaction(appName = "myapp")
    public void hello(ModelAndView view) {
        view.addObject("xid", TxcContext.getCurrentXid());
        view.addObject("inc_money", incMoneyService.hello());
        view.addObject("dec_money", decMoneyService.hello());
        view.addObject("level", updateLevelService.hello());
    }

    @TxcTransaction(appName = "myapp2")
    public void moneyChange(int money) throws SQLException {
        //测试先加后减
        incMoneyService.incMoney(money);
        decMoneyService.decMoney(money);
    }

    public IIncMoneyService getIncMoneyService() {
        return incMoneyService;
    }

    public void setIncMoneyService(IIncMoneyService incMoneyService) {
        this.incMoneyService = incMoneyService;
    }

    public IDecMoneyService getDecMoneyService() {
        return decMoneyService;
    }

    public void setDecMoneyService(IDecMoneyService decMoneyService) {
        this.decMoneyService = decMoneyService;
    }

    public IUpdateLevelService getUpdateLevelService() {
        return updateLevelService;
    }

    public void setUpdateLevelService(IUpdateLevelService updateLevelService) {
        this.updateLevelService = updateLevelService;
    }
}
