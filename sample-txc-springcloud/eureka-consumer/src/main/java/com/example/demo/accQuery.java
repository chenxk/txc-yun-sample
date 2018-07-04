package com.example.demo;


import com.taobao.txc.client.aop.annotation.TxcTransaction;
import com.taobao.txc.common.TxcContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpMethod;
//import org.springframework.http.HttpMethod;
@Repository
public class accQuery {


    @Autowired
    RestTemplate restTemplate;
    @TxcTransaction(timeout = 1000 * 1200)
    public int  transfer(String money)
    {
        String xid = TxcContext.getCurrentXid();
        HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.add("xid", xid);
        HttpEntity<String> requestEntity = new HttpEntity<String>(null, requestHeaders);

        ResponseEntity<String> response2 = restTemplate.exchange("http://ACCOUNT-SERVICE/add?money={money}", HttpMethod.GET,
                requestEntity,
                String.class,money);

                System.out.println("db2 update");


        ResponseEntity<String> response11 = restTemplate.exchange("http://HELLO-SERVICE/add?money={money}", HttpMethod.GET,
                requestEntity,
                String.class,money);

        int ret = Integer.parseInt(response11.getBody().toString());
        if(ret == -1)
        {
            throw new RuntimeException("error222");
        }
        return 1;
    }


    public String getMoneyA()
    {
        String money = restTemplate.getForObject("http://HELLO-SERVICE/getmoney", String.class);
        return  money;
    }

    public String getMoneyB()
    {
        String money = restTemplate.getForObject("http://ACCOUNT-SERVICE/getmoney", String.class);
        return  money;
    }

    public void resetA()
    {
        String money = restTemplate.getForObject("http://HELLO-SERVICE/resetacc", String.class);
    }

    public void  resetB()
    {
        String money = restTemplate.getForObject("http://ACCOUNT-SERVICE/resetacc", String.class);
    }




}
