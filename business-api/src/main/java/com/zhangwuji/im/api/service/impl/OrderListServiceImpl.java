package com.zhangwuji.im.api.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhangwuji.im.api.common.DateUtil;
import com.zhangwuji.im.api.entity.OrderList;
import com.zhangwuji.im.api.mapper.OrderListMapper;
import com.zhangwuji.im.api.service.IOrderListService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author cloudtalk
 * @since 2019-08-07
 */
@Service
public class OrderListServiceImpl extends ServiceImpl<OrderListMapper, OrderList> implements IOrderListService {

    @Override
    public OrderList creatOrder(OrderList orderModel) {
        String orderno = UUID.randomUUID().toString().replace("-", "");
        orderModel.setOrdid(orderno);
        orderModel.setOrdtime(LocalDateTime.now());
        orderModel.setOrdstatus(0);//未支付
        this.save(orderModel);
        return  orderModel;
    }

    @Override
    public Boolean updateOrder(String ordId,int status)
    {
        OrderList orderList=this.getOne(new QueryWrapper<OrderList>().eq("ordid",ordId));
        if(orderList!=null) {
            orderList.setOrdstatus(status);
            this.saveOrUpdate(orderList);
            return true;
        }
        else
        {
            return false;
        }
    }
    public List<OrderList> getOrderList2(int status,int pagesize)
    {
        String nowtime = DateUtil.DateToString(DateUtil.addSecond(new Date(), -600), "yyyy-MM-dd HH:mm:ss");
        List<OrderList> alllist = this.page(new Page<>(1, pagesize),new QueryWrapper<OrderList>().eq("ordstatus",status).gt("ordtime",nowtime)).getRecords();
        return  alllist;
    }

    @Override
    public List<OrderList> getOrderList(int uid,int status,int pagesize)
    {
        List<OrderList> alllist = this.page(new Page<>(1, pagesize),new QueryWrapper<OrderList>().eq("uid",uid).eq("ordstatus",status)).getRecords();
        return  alllist;
    }

}
