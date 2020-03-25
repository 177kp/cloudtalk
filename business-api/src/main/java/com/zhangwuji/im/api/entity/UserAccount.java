package com.zhangwuji.im.api.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
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
 * @since 2019-04-14
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("on_user_account")
public class UserAccount implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer uid;

    private String payPassword;

    private Double availableMoney;

    private Double freezeMoney;

    private String realname;

    private String idcard;

    private LocalDateTime updatetime;


}
