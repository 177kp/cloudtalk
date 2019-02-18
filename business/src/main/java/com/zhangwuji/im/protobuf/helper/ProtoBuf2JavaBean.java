package com.zhangwuji.im.protobuf.helper;

import com.alibaba.fastjson.JSONException;
import com.google.protobuf.ByteString;
import com.zhangwuji.im.protobuf.IMBaseDefine;
import com.zhangwuji.im.protobuf.IMGroup;
import com.zhangwuji.im.protobuf.IMMessage;
import com.zhangwuji.im.config.DBConstant;
import com.zhangwuji.im.config.MessageConstant;
import com.zhangwuji.im.protobuf.MessageEntity;

import java.io.UnsupportedEncodingException;


public class ProtoBuf2JavaBean {
    /**
     * 拆分消息在上层做掉 图文混排
     * 在这判断
    */
    public static MessageEntity getMessageEntity(IMBaseDefine.MsgInfo msgInfo) {
        MessageEntity messageEntity = null;
        IMBaseDefine.MsgType msgType = msgInfo.getMsgType();
        switch (msgType) {
            case MSG_TYPE_SINGLE_AUDIO:
            case MSG_TYPE_GROUP_AUDIO:
                try {
                    /**语音的解析不能转自 string再返回来*/
                    //messageEntity = analyzeAudio(msgInfo);
                } catch (JSONException e) {
                    return null;
                } catch (Exception e) {
                    return null;
                }
                break;

            case MSG_TYPE_GROUP_TEXT:
            case MSG_TYPE_SINGLE_TEXT:
                messageEntity = analyzeText(msgInfo);
                break;
            default:
                throw new RuntimeException("ProtoBuf2JavaBean#getMessageEntity wrong type!");
        }
        return messageEntity;
    }

    public static MessageEntity analyzeText(IMBaseDefine.MsgInfo msgInfo){
       return MsgAnalyzeEngine.analyzeMessage(msgInfo);
    }





    public static MessageEntity getMessageEntity(IMMessage.IMMsgData msgData){

        MessageEntity messageEntity = null;
        IMBaseDefine.MsgType msgType = msgData.getMsgType();
        IMBaseDefine.MsgInfo msgInfo = IMBaseDefine.MsgInfo.newBuilder()
                .setMsgData(msgData.getMsgData())
                .setMsgId(msgData.getMsgId())
                .setMsgType(msgType)
                .setCreateTime(msgData.getCreateTime())
                .setFromSessionId(msgData.getFromUserId())
                .build();

        switch (msgType) {
            case MSG_TYPE_SINGLE_AUDIO:
            case MSG_TYPE_GROUP_AUDIO:
                try {
                    messageEntity = analyzeText(msgInfo);
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case MSG_TYPE_GROUP_TEXT:
            case MSG_TYPE_SINGLE_TEXT:
                messageEntity = analyzeText(msgInfo);
                break;
            default:
                throw new RuntimeException("ProtoBuf2JavaBean#getMessageEntity wrong type!");
        }
        if(messageEntity != null){
            messageEntity.setToId(msgData.getToSessionId());
        }

        /**
         消息的发送状态与 展示类型需要在上层做掉
         messageEntity.setStatus();
         messageEntity.setDisplayType();
         */
        return messageEntity;
    }

   

    /**----enum 转化接口--*/
    public static int getJavaMsgType(IMBaseDefine.MsgType msgType){
        switch (msgType){
            case MSG_TYPE_GROUP_TEXT:
                return DBConstant.MSG_TYPE_GROUP_TEXT;
            case MSG_TYPE_GROUP_AUDIO:
                return DBConstant.MSG_TYPE_GROUP_AUDIO;
            case MSG_TYPE_SINGLE_AUDIO:
                return DBConstant.MSG_TYPE_SINGLE_AUDIO;
            case MSG_TYPE_SINGLE_TEXT:
                return DBConstant.MSG_TYPE_SINGLE_TEXT;
            default:
                throw new IllegalArgumentException("msgType is illegal,cause by #getProtoMsgType#" +msgType);
        }
    }

    public static int getJavaSessionType(IMBaseDefine.SessionType sessionType){
        switch (sessionType){
            case SESSION_TYPE_SINGLE:
                return DBConstant.SESSION_TYPE_SINGLE;
            case SESSION_TYPE_GROUP:
                return DBConstant.SESSION_TYPE_GROUP;
            default:
                throw new IllegalArgumentException("sessionType is illegal,cause by #getProtoSessionType#" +sessionType);
        }
    }

    public static int getJavaGroupType(IMBaseDefine.GroupType groupType){
        switch (groupType){
            case GROUP_TYPE_NORMAL:
                return DBConstant.GROUP_TYPE_NORMAL;
            case GROUP_TYPE_TMP:
                return DBConstant.GROUP_TYPE_TEMP;
            default:
                throw new IllegalArgumentException("sessionType is illegal,cause by #getProtoSessionType#" +groupType);
        }
    }

    public static int getGroupChangeType(IMBaseDefine.GroupModifyType modifyType){
        switch (modifyType){
            case GROUP_MODIFY_TYPE_ADD:
                return DBConstant.GROUP_MODIFY_TYPE_ADD;
            case GROUP_MODIFY_TYPE_DEL:
                return DBConstant.GROUP_MODIFY_TYPE_DEL;
            default:
                throw new IllegalArgumentException("GroupModifyType is illegal,cause by " +modifyType);
        }
    }

    public static int getDepartStatus(IMBaseDefine.DepartmentStatusType statusType){
        switch (statusType){
            case DEPT_STATUS_OK:
                return DBConstant.DEPT_STATUS_OK;
            case DEPT_STATUS_DELETE:
                return DBConstant.DEPT_STATUS_DELETE;
            default:
                throw new IllegalArgumentException("getDepartStatus is illegal,cause by " +statusType);
        }

    }
}
