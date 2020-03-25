package com.zhangwuji.im.api.entity;

import java.math.BigDecimal;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;
import java.util.Date;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.springframework.format.annotation.DateTimeFormat;

/**
 * <p>
 * 
 * </p>
 *
 * @author cloudtalk
 * @since 2019-03-23
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("on_luckymoney")
public class Luckymoney implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    private Integer senduid;

    private Integer type;

    private Integer type2;

    private String name;

    private double allmoney;

    private double usemoney;

    private Integer allnum;

    private Integer usenum;

    private String msg;

    private Integer status;

    private Integer lv;

    private LocalDateTime addtime;

    /**
     * 群id
     */
    @TableField("groupId")
    private Integer groupId;


}
