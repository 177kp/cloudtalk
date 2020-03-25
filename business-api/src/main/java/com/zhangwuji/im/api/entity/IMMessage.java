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
 * @since 2019-08-09
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("on_IMMessage")
public class IMMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 用户的关系id
     */
    @TableField("relateId")
    private Integer relateId;

    /**
     * 发送用户的id
     */
    @TableField("fromId")
    private Integer fromId;

    /**
     * 接收用户的id
     */
    @TableField("toId")
    private Integer toId;

    /**
     * 消息ID
     */
    @TableField("msgId")
    private Integer msgId;

    /**
     * 消息内容
     */
    private String content;

    /**
     * 消息类型
     */
    private Integer type;

    /**
     * 0正常 1被删除
     */
    private Integer status;

    /**
     * 更新时间
     */
    private Long updated;

    /**
     * 创建时间
     */
    private Long created;

    private Integer flag;


}
