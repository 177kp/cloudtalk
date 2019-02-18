package com.zhangwuji.im.api.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableField;
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
 * @since 2019-01-04
 */

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("on_IMUser")
public class IMUser extends baseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 用户id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;


    @TableField("appId")
    private Integer appId;

    /**
     * 用户关联的对外ID。可用于云服务的架构处理
     */
    @TableField("outId")
    private Integer outId;

    /**
     * 用户名
     */
    private String username;

    /**
     * 密码
     */
    private String password;

    /**
     * 混淆码
     */
    private String salt;

    private String apiToken;

    /**
     * 花名,绰号等
     */
    private String nickname;

    private String realname;

    /**
     * 1男2女0未知
     */
    private int sex;

    /**
     * 自定义用户头像
     */
    private String avatar;

    /**
     * 拼音
     */
    private String domain;

    /**
     * 手机号码
     */
    private String phone;

    /**
     * email
     */
    private String email;

    /**
     * 所属部门Id
     */
    @TableField("departId")
    private Integer departId;

    /**
     * 0关闭勿扰 1开启勿扰
     */
    private Boolean pushShieldStatus;

    /**
     * 个性签名
     */
    private String signInfo;

    /**
     * 1. 试用期 2. 正式 3. 离职 4.实习
     */
    private Integer status;

    /**
     * 更新时间
     */
    private Integer updated;

    /**
     * 创建时间
     */
    private Integer created;

    private String dataSign;

    @TableField("Longitude")
    private Float Longitude;

    @TableField("Latitude")
    private Float Latitude;


}
