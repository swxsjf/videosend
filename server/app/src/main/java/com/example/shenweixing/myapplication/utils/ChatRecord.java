package com.example.shenweixing.myapplication.utils;

import java.io.Serializable;

import io.realm.RealmObject;

/**
 * 诚志海图
 * Created by ShenWeiXing on 2019/9/10.9:39
 * Description:
 */

public class ChatRecord extends RealmObject implements Serializable {
    private String IP;//发送用户IP
    private String Type;//类别 1.普通消息 2.图片 3.视频通话 4.语音
    private String Content;//内容
    private String SendTime;//发送时间
    private boolean SendType;//是否是自己发送 true为自己发送，false为对方发送
    private String IsGroup;//判断是否为群组消息 0为普通消息，1为群组消息


    public void setIsGroup(String isGroup) {
        this.IsGroup = isGroup;
    }

    public String getIsGroup() {
        return IsGroup;
    }

    public void setSendType(boolean sendType) {
        SendType = sendType;
    }

    public boolean getSendType() {
        return SendType;
    }

    public void setIP(String IP) {
        this.IP = IP;
    }

    public String getIP() {

        return IP;
    }

    public boolean isSendType() {
        return SendType;
    }

    public String getType() {
        return Type;
    }

    public String getContent() {
        return Content;
    }

    public String getSendTime() {
        return SendTime;
    }

    public void setType(String type) {
        Type = type;
    }

    public void setContent(String content) {
        Content = content;
    }

    public void setSendTime(String sendTime) {
        SendTime = sendTime;
    }
}

