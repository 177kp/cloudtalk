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
 * @since 2019-01-15
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("on_IMAudio")
public class IMAudio implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 发送者Id
     */
    @TableField("fromId")
    private Integer fromId;

    /**
     * 接收者Id
     */
    @TableField("toId")
    private Integer toId;

    /**
     * 语音存储的地址
     */
    private String path;

    /**
     * 文件大小
     */
    private Integer size;

    /**
     * 语音时长
     */
    private Integer duration;

    /**
     * 创建时间
     */
    private Integer created;


}
