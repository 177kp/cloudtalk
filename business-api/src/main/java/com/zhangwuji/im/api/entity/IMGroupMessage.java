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
 * IM群消息表
 * </p>
 *
 * @author cloudtalk
 * @since 2019-08-09
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("on_IMGroupMessage")
public class IMGroupMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 用户的关系id
     */
    @TableField("groupId")
    private Integer groupId;

    /**
     * 发送用户的id
     */
    @TableField("userId")
    private Integer userId;

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
     * 群消息类型,101为群语音,2为文本
     */
    private Integer type;

    /**
     * 消息状态
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


}
