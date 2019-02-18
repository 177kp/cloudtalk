package com.zhangwuji.im.protobuf.base;

import com.zhangwuji.im.config.SysConstant;
import com.zhangwuji.im.support.SequenceNumberMaker;

public class DefaultHeader extends Header {

    public DefaultHeader(int serviceId, int commandId) {
        setVersion((short) SysConstant.PROTOCOL_VERSION);
        setFlag((short) SysConstant.PROTOCOL_FLAG);
        setServiceId((short)serviceId);
        setCommandId((short)commandId);
        short seqNo = SequenceNumberMaker.getInstance().make();
        setSeqnum(seqNo);
        setReserved((short)SysConstant.PROTOCOL_RESERVED);
    }
}
