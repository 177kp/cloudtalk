package com.zhangwuji.im.imcore.entity;

import com.alibaba.fastjson.JSON;
import com.zhangwuji.im.DB.entity.Message;
import com.zhangwuji.im.DB.entity.PeerEntity;
import com.zhangwuji.im.DB.entity.User;
import com.zhangwuji.im.Security;
import com.zhangwuji.im.config.DBConstant;
import com.zhangwuji.im.config.MessageConstant;
import com.zhangwuji.im.imcore.support.SequenceNumberMaker;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;

/**
 * @author : yingmu on 14-12-31.
 * @email : yingmu@mogujie.com.
 */
public class SystemMessage extends Message implements Serializable {

     public SystemMessage(){
         msgId = SequenceNumberMaker.getInstance().makelocalUniqueMsgId();
     }

     private SystemMessage(Message entity){
         /**父类的id*/
         id =  entity.getId();
         msgId  = entity.getMsgId();
         fromId = entity.getFromId();
         toId   = entity.getToId();
         sessionKey = entity.getSessionKey();
         content=entity.getContent();
         msgType=entity.getMsgType();
         sessionType=entity.getSessionType();
         displayType=entity.getDisplayType();
         status = entity.getStatus();
         created = entity.getCreated();
         updated = entity.getUpdated();
     }

     public static SystemMessage parseFromNet(Message entity){
         SystemMessage textMessage = new SystemMessage(entity);
         textMessage.setStatus(MessageConstant.MSG_SUCCESS);
         textMessage.setDisplayType(DBConstant.SHOW_ORIGIN_TEXT_TYPE);
         return textMessage;
     }

    public static SystemMessage parseFromDB(Message entity){
        if(entity.getDisplayType()!=DBConstant.SHOW_ORIGIN_TEXT_TYPE){
            throw new RuntimeException("#TextMessage# parseFromDB,not SHOW_ORIGIN_TEXT_TYPE");
        }
        SystemMessage textMessage = new SystemMessage(entity);
        return textMessage;
    }

    public static SystemMessage buildIMessageFosend(IMessage iMessage, User fromUser, PeerEntity peerEntity){
        iMessage.setTag(((MessageTag)iMessage.getClass().getAnnotation(MessageTag.class)).value());
        SystemMessage textMessage = new SystemMessage();
        int nowTime = (int) (System.currentTimeMillis() / 1000);
        textMessage.setFromId(fromUser.getPeerId());
        textMessage.setToId(peerEntity.getPeerId());
        textMessage.setUpdated(nowTime);
        textMessage.setCreated(nowTime);
        textMessage.setDisplayType(DBConstant.SHOW_ORIGIN_TEXT_TYPE);
        textMessage.setGIfEmo(true);
        textMessage.setMsgType(DBConstant.MSG_TYPE_NOTICE_SYSTEM);
        textMessage.setSessionType(DBConstant.SESSION_TYPE_SYSTEM);
        textMessage.setStatus(MessageConstant.MSG_SENDING);
        // 内容的设定
        textMessage.setContent(JSON.toJSONString(iMessage));
        textMessage.buildSessionKey(true);
        return textMessage;
    }

    public static SystemMessage buildForSend(String content, User fromUser, PeerEntity peerEntity){
        SystemMessage textMessage = new SystemMessage();
        int nowTime = (int) (System.currentTimeMillis() / 1000);
        textMessage.setFromId(fromUser.getPeerId());
        textMessage.setToId(peerEntity.getPeerId());
        textMessage.setUpdated(nowTime);
        textMessage.setCreated(nowTime);
        textMessage.setDisplayType(DBConstant.SHOW_ORIGIN_TEXT_TYPE);
        textMessage.setGIfEmo(true);
        textMessage.setMsgType(DBConstant.MSG_TYPE_NOTICE_SYSTEM);
        textMessage.setSessionType(DBConstant.SESSION_TYPE_SYSTEM);
        textMessage.setStatus(MessageConstant.MSG_SENDING);
        // 内容的设定
        textMessage.setContent(content);
        textMessage.buildSessionKey(true);
        return textMessage;
    }


    /**
     * Not-null value.
     * DB的时候需要
     */
    @Override
    public String getContent() {
        return content;
    }

    @Override
    public byte[] getSendContent() {
        try {
            /** 加密*/
          //  String sendContent =new String(Security.getInstance().EncryptMsg(content));
            String  sendContent =content;
            return sendContent.getBytes("utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }
}
