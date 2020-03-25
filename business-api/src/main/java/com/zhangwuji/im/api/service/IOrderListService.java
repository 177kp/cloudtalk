package com.zhangwuji.im.api.service;

import com.zhangwuji.im.api.entity.OrderList;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author cloudtalk
 * @since 2019-08-07
 */
public interface IOrderListService extends IService<OrderList> {

    OrderList creatOrder(OrderList orderModel);
    Boolean updateOrder(String ordId,int status);
    List<OrderList> getOrderList(int uid, int status, int pagesize);

}
