package com.zhangwuji.im.ui.plugin.message.entity;


import com.zhangwuji.im.imcore.entity.IMessage;
import com.zhangwuji.im.imcore.entity.MessageTag;

@MessageTag(value = "cloudtalk:redpacket")
public class RedPacketMessage extends IMessage {
    int id;
    String info;
    int sender;
    int type;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    public int getSender() {
        return sender;
    }

    public void setSender(int sender) {
        this.sender = sender;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }
}
