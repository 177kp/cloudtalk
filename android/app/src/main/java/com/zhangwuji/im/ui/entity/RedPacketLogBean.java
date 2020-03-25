package com.zhangwuji.im.ui.entity;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Created by chenwang on 2019/4/12.
 */

public class RedPacketLogBean implements Serializable{

    private static final long serialVersionUID = 1L;

    private int id;

    private int pid;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getPid() {
        return pid;
    }

    public void setPid(int pid) {
        this.pid = pid;
    }

    public int getUid() {
        return uid;
    }

    public void setUid(int uid) {
        this.uid = uid;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public double getMoney() {
        return money;
    }

    public void setMoney(double money) {
        this.money = money;
    }

    public int getLv() {
        return lv;
    }

    public void setLv(int lv) {
        this.lv = lv;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getAddtime() {
        return addtime;
    }

    public void setAddtime(String addtime) {
        this.addtime = addtime;
    }

    public int getFuid() {
        return fuid;
    }

    public void setFuid(int fuid) {
        this.fuid = fuid;
    }

    public int getFlag() {
        return flag;
    }

    public void setFlag(int flag) {
        this.flag = flag;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    private int uid;

    private int type;

    private double money;

    private int lv;

    private int status;

    private String addtime;

    /**
     * 内定用户
     */
    private int fuid;

    /**
     * 红包标记
     */
    private int flag;

    private String nickname;
    private String avatar;

}
