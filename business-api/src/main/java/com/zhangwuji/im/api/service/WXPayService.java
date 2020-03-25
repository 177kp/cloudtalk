package com.zhangwuji.im.api.service;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.zhangwuji.im.api.common.MathTool;
import com.zhangwuji.im.api.common.MathUtil;
import com.zhangwuji.im.api.config.WechatAccountConfig;
import com.zhangwuji.im.api.emun.ReturnCode;
import com.zhangwuji.im.api.entity.OrderList;
import com.zhangwuji.im.api.entity.UserAccount;
import com.zhangwuji.im.api.entity.Userpaylog;
import com.zhangwuji.im.api.service.impl.OrderListServiceImpl;
import com.zhangwuji.im.api.service.impl.UserAccountServiceImpl;
import com.zhangwuji.im.api.service.impl.UserpaylogServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import weixin.popular.api.PayMchAPI;
import weixin.popular.bean.paymch.*;
import weixin.popular.util.PayUtil;
import weixin.popular.util.SignatureUtil;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
public class WXPayService {

    @Autowired
    private  WechatAccountConfig wechatAccountConfig;

    @Resource
    private OrderListServiceImpl orderListService;

    @Autowired
    UserAccountServiceImpl userAccountService;

    @Autowired
    UserpaylogServiceImpl userpaylogService;

    @Autowired
    public WXPayService(WechatAccountConfig wechatAccountConfig) {
        this.wechatAccountConfig = wechatAccountConfig;
    }

    /**
     * APP微信统一下单
     * 外部调用方法进行事务控制
     * issueDesc 商品描述 必传 对应微信端字段 body
     * payOrderId 本地支付订单ID 必传 对应微信端字段 out_trade_no
     * totalFee 申请支付金额 必传 对应微信端字段 total_fee
     * clientIp 请求端ip 必传 对应微信端字段  spbill_create_ip
     * notifyUrl 微信支付结果通知地址  必传 对应微信端字段  notify_url
     * tradeType 微信交易类型  必传 对应微信端字段  trade_type :JSAPI--JSAPI支付（或小程序支付）、NATIVE--Native支付、APP--app支付，MWEB--H5支付，MICROPAY--付款码支付
     *
     * @return UnifiedorderResult
     */

    public String unifiedOrderApp(OrderList orderMaster) {

        log.debug("支付参数" + orderMaster.toString());
        //微信统一下单接口参数拼接
        Unifiedorder unifiedorder = new Unifiedorder();
        //如果是公众号支付。调用公众号ID.其它的调用App开放平台的apid
        unifiedorder.setAppid(wechatAccountConfig.getMpAppId());
        unifiedorder.setMch_id(wechatAccountConfig.getMchId());
        unifiedorder.setNonce_str(UUID.randomUUID().toString().replaceAll("-", "").substring(0, 32)); //通过工具类获取随机字符串
        unifiedorder.setSign_type("MD5");

        unifiedorder.setBody(orderMaster.getOrdtitle());//支付标题

        unifiedorder.setOut_trade_no(orderMaster.getOrdid()); //支付订单
        Integer totalFee = MathTool.multiply(orderMaster.getOrdfee(), 100).intValue(); //总金额
        unifiedorder.setTotal_fee(totalFee.toString());
        unifiedorder.setSpbill_create_ip("127.0.0.1"); //终端IP
        unifiedorder.setNotify_url(wechatAccountConfig.getNotifyUrl());//通知回调url
        unifiedorder.setTrade_type("APP");

        log.info("【微信支付】发起支付, request={}", JSON.toJSON(unifiedorder));

        //调用微信api进行微信统一下单
        UnifiedorderResult result = PayMchAPI.payUnifiedorder(unifiedorder,
                wechatAccountConfig.getMchKey());

        log.info("【微信支付】发起支付, response={}", JSON.toJSON(result));

        // 判断通信标识
        if (!ReturnCode.SUCCESS.getValue().equals(result.getReturn_code())) {
            log.error("[微信下单] 微信下单通信失败,失败原因:{}", result.getReturn_msg());
            throw new RuntimeException("微信下单失败");
        }

        // 判断业务标识
        if (!ReturnCode.SUCCESS.getValue().equals(result.getResult_code())) {
            log.error("[微信下单] 微信下单业务失败,错误码:{},错误原因:{}", result.getErr_code(), result.getErr_code_des());
            throw new RuntimeException(result.getErr_code_des());
        }
        orderMaster.setOrdcode(result.getPrepay_id());
        orderMaster.setPaymentRes(JSON.toJSONString(result));

        //更新支付订单表
        orderListService.updateById(orderMaster);

        MchPayApp mchPayApp= PayUtil.generateMchAppData(result.getPrepay_id(),wechatAccountConfig.getMpAppId(),wechatAccountConfig.getMchId(),wechatAccountConfig.getMchKey());
        String s = JSON.toJSONString(mchPayApp);
        return s;
    }

    /**
     * 微信支付结果异步通知处理方法
     * 外部调用方法进行事务控制
     * return_code 返回状态码 SUCCESS
     * return_msg 返回信息 OK
     * appid 公众账号ID
     * mch_id 商户号
     * nonce_str 随机字符串
     * sign 签名
     * sign_type 签名类型
     * result_code 业务结果: SUCCESS/FAIL
     * openid 用户标识
     * is_subscribe  是否关注公众账号
     * trade_type 交易类型
     * 付款银行
     * total_fee 订单金额
     * cash_fee 现金支付金额
     * transaction_id 微信支付订单号
     * out_trade_no 商户订单号
     * time_end 支付完成时间,格式为yyyyMMddHHmmss
     *
     * @return payOrder
     */
    public OrderList payResultNotify(Map<String, String> resultMap) {
        //如果return_code不为SUCCESS,不做任何处理,返回错误信息
        String returnCode = resultMap.get("return_code");
        //返回状态码为成功
        if (ReturnCode.SUCCESS.getValue().equals(returnCode)) {
            //判断业务结果
            String resultCode = resultMap.get("result_code");
            if (ReturnCode.SUCCESS.getValue().equals(resultCode)) {
                //进行签名验证
                if (!SignatureUtil.validateSign(resultMap, wechatAccountConfig.getMchKey())) {
                    log.error("签名验证不通过,签名为{}", resultMap.get("sign"));
                    throw new RuntimeException("签名认证失败");
                }
                OrderList tpayOrder = check(resultMap);
                return tpayOrder;
            }
        }
        return null;
    }

    /**
     * 微信支付订单查询
     *
     * @param orderNo
     * @author clh
     * @version 1.0
     */
    public OrderList payOrderquery(String orderNo)
    {
        MchOrderquery mchOrderquery = new MchOrderquery();
        mchOrderquery.setAppid(wechatAccountConfig.getMpAppId()); //  map.get("wx_app_appid")
        mchOrderquery.setMch_id(wechatAccountConfig.getMchId());//map.get("wx_mch_id")
        mchOrderquery.setNonce_str(UUID.randomUUID().toString().replace("-", ""));
        mchOrderquery.setSign_type( "MD5");
        mchOrderquery.setOut_trade_no(orderNo);
        MchOrderInfoResult result = PayMchAPI.payOrderquery(mchOrderquery, wechatAccountConfig.getMchKey());//map.get("wx_api_key")
        String openid = result.getOpenid();

        if (!ReturnCode.SUCCESS.getValue().equals(result.getReturn_code())) {
            throw new RuntimeException("查询微信支付订单结果出错," + result.getReturn_msg());
        }
        if (!ReturnCode.SUCCESS.getValue().equals(result.getResult_code())) {
            throw new RuntimeException("查询微信支付订单结果出错," + result.getErr_code_des());
        }
        String payOrderId = result.getOut_trade_no();

        OrderList tpayOrder =  orderListService.getOne(new QueryWrapper<OrderList>().eq("ordid",payOrderId));
        Map<String, String> resultMap = new HashMap<>();
        resultMap.put("openid", openid);
        resultMap.put("out_trade_no", result.getOut_trade_no());
        resultMap.put("sign_type", "MD5");
        resultMap.put("sign", result.getSign());
        resultMap.put("total_fee", String.valueOf(result.getTotal_fee()));
        resultMap.put("transaction_id",result.getTransaction_id());
        if (result.getTrade_state().equals(ReturnCode.SUCCESS.getValue())) {
            tpayOrder = check(resultMap);
        } else if (result.getTrade_state().equals(ReturnCode.NOTPAY.getValue())) {
            return tpayOrder;
        }
        return tpayOrder;
    }

    @Transactional
    public OrderList check(Map<String, String> resultMap) {

        String payOrderId = resultMap.get("out_trade_no");//得到本地系统支付订单ID
        OrderList tpayOrder= orderListService.getOne(new QueryWrapper<OrderList>().eq("ordid",payOrderId));
        if (tpayOrder == null) {
            log.error("支付订单不存在,该订单为{}", payOrderId);
            throw new RuntimeException("支付订单不存在");
        }
        //验证订单金额
        String total_fee = resultMap.get("total_fee");
        if (StringUtils.isBlank(total_fee)) {
            log.error("金额为空");
            throw new RuntimeException("订单金额不正确");
        }
        int totalFee2 = Integer.valueOf(total_fee);
        double totalFee = MathTool.divideToScale(2, new Object[]{totalFee2, 100}).doubleValue();

        if (!MathUtil.equals(tpayOrder.getOrdfee().doubleValue(), totalFee)) {
            log.error("订单金额与商户侧的订单金额不一致支付为:{},回调金额为:{}", tpayOrder.getOrdfee().doubleValue(), total_fee);
            throw new RuntimeException("订单金额不一致");
        }

        //更新账户余额
        UserAccount userAccount=userAccountService.getOne(new QueryWrapper<UserAccount>().eq("uid",tpayOrder.getUid()));
        userAccount.setAvailableMoney(userAccount.getAvailableMoney()+tpayOrder.getOrdfee());
        userAccount.setUpdatetime(LocalDateTime.now());
        userAccountService.update(userAccount,new QueryWrapper<UserAccount>().eq("uid", tpayOrder.getUid()));//更新账户余额

        Userpaylog userpaylog=new Userpaylog();
        userpaylog.setOrderno(tpayOrder.getOrdid());
        userpaylog.setPaymoney(BigDecimal.valueOf(tpayOrder.getOrdfee()));
        userpaylog.setAllmoney(BigDecimal.valueOf(userAccount.getAvailableMoney()));
        userpaylog.setLv(0);
        userpaylog.setPaytouid("0");
        userpaylog.setPaymsg("微信充值");
        userpaylog.setPaytype(1);//2为支出
        userpaylog.setPaytime(LocalDateTime.now());
        userpaylog.setStatus(1);
        userpaylog.setUid(tpayOrder.getUid()+"");
        userpaylogService.save(userpaylog);

        tpayOrder.setPaymentRes(resultMap.toString());
        tpayOrder.setPaymentTradeNo(resultMap.get("transaction_id"));
        tpayOrder.setPaymentNotifyTime(LocalDateTime.now());
        tpayOrder.setOrdstatus(1);
        //修改订单状态
        orderListService.saveOrUpdate(tpayOrder);

        return tpayOrder;
    }


}
