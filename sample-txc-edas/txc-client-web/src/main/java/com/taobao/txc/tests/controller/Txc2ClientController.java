package com.taobao.txc.tests.controller;

import com.taobao.txc.tests.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.sql.SQLException;

/**
 * Created by simiao.zw on 2016/7/15.
 */
@Controller
public class Txc2ClientController {
    @Autowired
    private BussinessBase bussiness;

    public BussinessBase getBussiness() {
        return bussiness;
    }

    public void setBussiness(BussinessDrds bussiness) {
        this.bussiness = bussiness;
    }

    /**
     * 获取当前存款状态
     */
    @RequestMapping(value = "/client", method = RequestMethod.GET)
    public ModelAndView status(HttpServletRequest request, HttpServletResponse response) throws SQLException {
        ModelAndView view = new ModelAndView("client");
        view.addObject("inc_money", bussiness.getIncMoneyService().getMoney());
        view.addObject("dec_money", bussiness.getDecMoneyService().getMoney());
        view.addObject("level", bussiness.getUpdateLevelService().getLevel());
        return view;
    }

    /**
     * 初始化账户
     */
    @RequestMapping(value = "/client/reset", method = RequestMethod.POST)
    public ModelAndView create(AccountForm accountForm) throws SQLException {
        ModelAndView view = new ModelAndView("redirect:/client");
        bussiness.getIncMoneyService().resetMoney(0);
        bussiness.getDecMoneyService().resetMoney(accountForm.getMoney());
        bussiness.getUpdateLevelService().resetLevel(0);
        return view;
    }

    /**
     * 简单测试服务是否通并且在txd
     */
    @RequestMapping(value = "/client/hello", method = RequestMethod.GET)
    public ModelAndView hello(HttpServletRequest request, HttpServletResponse response) {
        ModelAndView view = new ModelAndView("client");
        bussiness.hello(view);
        return view;
    }

    /**
     * 从账户a转钱到账户b,然后账户b自动转钱到账户c
     */
    @RequestMapping(value = "/client/pay", method = RequestMethod.POST)
    public ModelAndView moneyAToB(TransferForm transferForm) throws SQLException {
        ModelAndView view = new ModelAndView("redirect:/client");
        bussiness.moneyChange(transferForm.getMoney());
        return view;
    }
}
