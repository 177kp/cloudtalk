package com.zhangwuji.im.api.task;

import com.zhangwuji.im.api.entity.OrderList;
import com.zhangwuji.im.api.service.WXPayService;
import com.zhangwuji.im.api.service.impl.OrderListServiceImpl;
import com.zhangwuji.im.api.common.DateUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

@Component
@Slf4j
public class PaymentJobs {
    public final static long runTimes = 10 * 1000;

    @Autowired
    private OrderListServiceImpl tshopOrderClient;

    @Autowired
    public WXPayService wxPayService;

    public String orderNo = null;

    //隔一定时间执行一次，不管上次任务执行完毕没。
    @Scheduled(fixedRate = runTimes)
    public void fixedRateJob() {

        System.out.println(new Date() + " >>微信支付查询定时任务启动...");

        List<OrderList> list = tshopOrderClient.getOrderList2(0,10);
        for (OrderList tpayOrder : list) {
            Runnable runnable = () -> {
                try {
                    wxPayService.payOrderquery(tpayOrder.getOrdid());
                } catch (Exception e) {
                    log.error("微信支付定时查询任务执行单个任务时出错.订单ID:" + orderNo + " 出错信息:" + e.getMessage());
                }
            };
            Thread thread = new Thread(runnable);
            thread.start();
        }
    }
}
