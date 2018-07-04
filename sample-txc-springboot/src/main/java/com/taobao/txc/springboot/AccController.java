package com.taobao.txc.springboot;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;


@SpringBootApplication
@Controller
public class AccController {

    @Autowired
    private AccService accService;
    //登录首页 http://127.0.0.1:8080/pay
    @RequestMapping("/pay")
    public String login(){
        return "pay";
    }


    @RequestMapping("/reset")
    public String reset(ModelMap map){
        int accountA = accService.getMoney1();
        int accountB = accService.getMoney2();
        map.addAttribute("accountA", accountA);
        map.addAttribute("accountB", accountB);
        return "pay";
    }

    @RequestMapping("/setmoney")
    public String reset(HttpServletRequest request, ModelMap map)
    {
        String str_money = request.getParameter("money");
        int money = Integer.parseInt(str_money);
        accService.resetAccount(money);
        int accountA = accService.getMoney1();
        int accountB = accService.getMoney2();
        map.addAttribute("accountA", accountA);
        map.addAttribute("accountB", accountB);
        return "pay";

    }


    @RequestMapping("paymoney")
    public String  transferAccount(HttpServletRequest request,ModelMap map)
    {
        String str_money = request.getParameter("money");
        int money = Integer.parseInt(str_money);
        try {
            accService.transferAccount(money);
        }
        catch (RuntimeException e)
        {
            map.addAttribute("errinfo", "余额不足转账失败，GTS事务已回滚");
        }
        finally {
            int accountA = accService.getMoney1();
            int accountB = accService.getMoney2();
            map.addAttribute("accountA", accountA);
            map.addAttribute("accountB", accountB);
            return "pay";

        }

    }


    public static void main(String[] args) {

        SpringApplication.run(AccController.class, args);
    }
}
