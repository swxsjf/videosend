package com.example.shenweixing.myapplication.bean;

import java.io.Serializable;

/**
 * 诚志海图
 * Created by ShenWeiXing on 2019/9/10.9:38
 * Description:
 */

public class UserMessageBean implements Serializable {
    private String Name;
    private String IpAdress;


    public String getName() {
        return Name;
    }

    public String getIpAdress() {
        return IpAdress;
    }

    public void setName(String name) {
        Name = name;
    }

    public void setIpAdress(String ipAdress) {
        IpAdress = ipAdress;
    }
}
