package com.zhangwuji.im.api.controller;


import com.alibaba.fastjson.JSON;
import com.zhangwuji.im.api.common.ControllerUtil;
import com.zhangwuji.im.api.common.JavaBeanUtil;
import com.zhangwuji.im.api.config.WechatAccountConfig;
import com.zhangwuji.im.api.entity.IMUser;
import com.zhangwuji.im.api.entity.OrderList;
import com.zhangwuji.im.api.entity.ServerInfoEntity;
import com.zhangwuji.im.api.result.ApiResult;
import com.zhangwuji.im.api.service.WXPayService;
import com.zhangwuji.im.api.service.impl.OrderListServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.apache.commons.io.IOUtils;


/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author cloudtalk
 * @since 2019-08-07
 */
@Slf4j
@RestController
@RequestMapping("/api/order-list")
public class OrderListController {

    @Resource
    ControllerUtil controllerUtil;

    @Resource
    JavaBeanUtil javaBeanUtil;

    private final String WX_RETURN_RES_SUCCESS = "<xml>\n" +
            "\n" +
            "  <return_code><![CDATA[SUCCESS]]></return_code>\n" +
            "  <return_msg><![CDATA[OK]]></return_msg>\n" +
            "</xml>";

    @Resource
    OrderListServiceImpl orderListService;

    @Autowired
    WXPayService wxPayService;

    @RequestMapping(value = "creatOrder", method = RequestMethod.POST)
    public String creatOrder(HttpServletRequest req, HttpServletResponse rsp) {
        ApiResult returnResult = new ApiResult();
        Map<String, Object> returnData = new HashMap<>();
        ServerInfoEntity serverinfo = new ServerInfoEntity();
        Map<String, Object> bmqq_plugin = new HashMap<>();

        IMUser myinfo = controllerUtil.checkToken(req);
        if (myinfo == null) {
            returnResult.setCode(ApiResult.ERROR);
            returnResult.setData(returnData);
            returnResult.setMessage("token验证失败!");
            return JSON.toJSONString(returnResult);
        }

        String fee = controllerUtil.getStringParameter(req, "fee", "");
        OrderList orderList=new OrderList();
        orderList.setUid((long)myinfo.getId());
        orderList.setOrdtitle("充值");
        orderList.setOrdbuynum(1);
        orderList.setOrdfee(Float.parseFloat(fee));
        orderList.setOrdprice(orderList.getOrdfee());
        orderList.setUsername(myinfo.getUsername());
        orderList.setProductid(1);
        orderList.setPaymentType("wx");

        orderList = orderListService.creatOrder(orderList);
        String paystring=wxPayService.unifiedOrderApp(orderList);

        return paystring;
    }

    /**
     * 微信异步通知
     */
    @RequestMapping(value = "/notify", method = RequestMethod.POST)
    @ResponseBody
    public String notify(HttpServletRequest req) {
        String xmlResult = null;
        Map<String, String> resultMap = null;
        try {
            xmlResult = IOUtils.toString(req.getInputStream(), req.getCharacterEncoding());
            resultMap = javaBeanUtil.xmlToMap(xmlResult);
        } catch (Exception e) { throw new RuntimeException("异步通知失败"); }

        OrderList tpayOrder = wxPayService.payResultNotify(resultMap);
        if(tpayOrder == null){
            log.error("【微信支付回调出错】notify, resultMap={}", JSON.toJSON(resultMap));
            throw new RuntimeException("异步通知失败");
        }
        //返回给微信处理结果
        return WX_RETURN_RES_SUCCESS;
    }
}
