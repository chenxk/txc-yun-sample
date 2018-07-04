package com.taobao.txc.tests;

/**
 * Created by simiao.zw on 2016/7/15.
 */
public class BussinessDrds extends BussinessBase {
    public void init() {
        incMoneyService = (IIncMoneyService) applicationContext.getBean("incMoneyService_drds");
        decMoneyService = (IDecMoneyService) applicationContext.getBean("decMoneyService_drds");
        updateLevelService = (IUpdateLevelService) applicationContext.getBean("updateLevelService_drds");
    }
}
