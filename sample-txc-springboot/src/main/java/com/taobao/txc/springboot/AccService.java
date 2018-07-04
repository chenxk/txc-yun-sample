package com.taobao.txc.springboot;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AccService {
    @Autowired
    AccDAO dao;
    public void resetAccount(int money){dao.reset(money);}
    public void transferAccount(int money){
        dao.transferAccount(money);
    }

    public int getMoney1()
    {
        return  dao.getMoney1().intValue();
    }
    public int getMoney2()
    {
        return  dao.getMoney2().intValue();
    }
}
