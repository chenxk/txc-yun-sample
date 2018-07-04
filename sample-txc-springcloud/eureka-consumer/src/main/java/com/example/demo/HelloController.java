package com.example.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.client.RestTemplate;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;


@Controller
public class HelloController {

    @Autowired
    RestTemplate restTemplate;

    @Autowired
    accQuery accQuery;


    @RequestMapping(value="/tranferAcc",method= RequestMethod.GET)
    public String helloConsumer(HttpServletRequest request,HttpServletResponse response,ModelMap map)  {
        String str_money = request.getParameter("money");
        try {
            accQuery.transfer(str_money);
        }
        catch (Exception e )
        {
            map.addAttribute("errinfo", "余额不足转账失败，GTS事务已回滚");
        }
        String accountA = accQuery.getMoneyA();
        String accountB = accQuery.getMoneyB();
        map.addAttribute("accountA", accountA);
        map.addAttribute("accountB", accountB);
        return "pay";

    }
    //首页网址  http://127.0.0.1:9000/pay
    @RequestMapping("/pay")
    public String login(HttpServletRequest request,ModelMap map){
        accQuery.resetA();
        accQuery.resetB();
       String accountA = accQuery.getMoneyA();
       String accountB = accQuery.getMoneyB();
       map.addAttribute("accountA", accountA);
       map.addAttribute("accountB", accountB);
       return "pay";
    }




}
