package com.zhangwuji.im.api.emun;

import lombok.Getter;

@Getter
public enum ReturnCode {
    SUCCESS("SUCCESS"),
    FAIL("FAIL"),
    ORDERPAID("ORDERPAID"),
    ORDERCLOSED("ORDERCLOSED"),
    INVALID_REQUEST("INVALID_REQUEST"),
    NOTPAY("NOTPAY"),
    OUT_TRADE_NO_USED("OUT_TRADE_NO_USED");
    ReturnCode(String value) {
        this.value = value;
    }
    String value;

}
