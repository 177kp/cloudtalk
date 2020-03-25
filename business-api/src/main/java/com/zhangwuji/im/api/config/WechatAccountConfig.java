package com.zhangwuji.im.api.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Created by wzy
 * 2017-07-03 01:31
 */
@Data
@Component
@ConfigurationProperties(prefix = "cloudtalk.weixin")
public class WechatAccountConfig {
    /**
     * 公众平台id
     */
    //@Value("${cloudtalk.weixin.mpAppId}")
    private String mpAppId;

    /**
     * 公众平台密钥
     */
   //@Value("${cloudtalk.weixin.mpAppSecret}")
    private String mpAppSecret;

    /**
     * 商户号
     */
   //@Value("${cloudtalk.weixin.mchId}")
    private String mchId;

    /**
     * 商户密钥
     */
    //@Value("${cloudtalk.weixin.mchKey}")
    private String mchKey;

    /**
     * 商户证书路径
     */
    //@Value("${cloudtalk.weixin.keyPath}")
    private String keyPath;

    /**
     * 微信支付异步通知地址
     */
    //@Value("${cloudtalk.weixin.notifyUrl}")
    private String notifyUrl;

}
