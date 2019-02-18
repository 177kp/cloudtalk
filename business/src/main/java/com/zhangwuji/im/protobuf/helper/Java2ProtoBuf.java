package com.zhangwuji.im.protobuf.helper;

import com.zhangwuji.im.config.DBConstant;
import com.zhangwuji.im.protobuf.IMBaseDefine;
public class Java2ProtoBuf {
    /**----enum 转化接口--*/
    public static IMBaseDefine.MsgType getProtoMsgType(int msgType){
        switch (msgType){
            case DBConstant.MSG_TYPE_GROUP_TEXT:
                return IMBaseDefine.MsgType.MSG_TYPE_GROUP_TEXT;
            case DBConstant.MSG_TYPE_GROUP_AUDIO:
                return IMBaseDefine.MsgType.MSG_TYPE_GROUP_AUDIO;
            case DBConstant.MSG_TYPE_SINGLE_AUDIO:
                return IMBaseDefine.MsgType.MSG_TYPE_SINGLE_AUDIO;
            case DBConstant.MSG_TYPE_SINGLE_TEXT:
                return IMBaseDefine.MsgType.MSG_TYPE_SINGLE_TEXT;
            default:
                throw new IllegalArgumentException("msgType is illegal,cause by #getProtoMsgType#" +msgType);
        }
    }


    public static IMBaseDefine.SessionType getProtoSessionType(int sessionType){
        switch (sessionType){
            case DBConstant.SESSION_TYPE_SINGLE:
                return IMBaseDefine.SessionType.SESSION_TYPE_SINGLE;
            case DBConstant.SESSION_TYPE_GROUP:
                return IMBaseDefine.SessionType.SESSION_TYPE_GROUP;
            default:
                throw new IllegalArgumentException("sessionType is illegal,cause by #getProtoSessionType#" +sessionType);
        }
    }
}
