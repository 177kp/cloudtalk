package com.zhangwuji.im.api.entity;

import com.baomidou.mybatisplus.annotation.TableName;
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
 * @since 2020-03-02
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("on_IMOnline")
public class IMOnline implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 用户id
     */
    private Integer uid;

    private Integer appid;

    /**
     * 1为在线0为下线 3分钟未更新为下线
     */
    private Integer status;

    /**
     * 最后更新时间
     */
    private Long updatetime;


}
