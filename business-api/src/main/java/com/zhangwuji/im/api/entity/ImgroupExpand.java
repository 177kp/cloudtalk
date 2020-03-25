package com.zhangwuji.im.api.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableId;
import java.time.LocalDateTime;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 群组拓展表
 * </p>
 *
 * @author cloudtalk
 * @since 2019-04-24
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("on_IMGroup_expand")
public class ImgroupExpand implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 群组id
     */
    @TableId("group_Id")
    private Long groupId;

    /**
     * 是否开启机器人自动抢红包0：不开启 1：开启
     */
    private Integer autoStatus;

    /**
     * 开启限制0：不开启 1：开启
     */
    private Integer pointStatus;

    /**
     * 第一位小数点限制的数字，多个已逗号隔开；1,2,3
     */
    private String firstPoint;

    /**
     * 第二位小数点限制的数字，多个已逗号隔开；1,2,3
     */
    private String secondPoint;

    /**
     * 添加时间
     */
    private LocalDateTime addTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;


}
