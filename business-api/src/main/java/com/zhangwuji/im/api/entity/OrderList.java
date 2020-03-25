package com.zhangwuji.im.api.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.time.LocalDateTime;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 
 * </p>
 *
 * @author cloudtalk
 * @since 2019-08-07
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("on_order_list")
public class OrderList implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long uid;

    private String username;

    private String ordid;

    private LocalDateTime ordtime;

    private Integer productid;

    private String ordtitle;

    private Integer ordbuynum;

    private Float ordprice;

    private Float ordfee;

    private Integer ordstatus;

    private String paymentType;

    private String paymentTradeNo;

    private String paymentTradeStatus;

    private String paymentNotifyId;

    private LocalDateTime paymentNotifyTime;

    private String paymentBuyerEmail;

    private String paymentRes;

    private String ordcode;

    private Integer isused;

    private Integer usetime;

    private Integer checkuser;


}
