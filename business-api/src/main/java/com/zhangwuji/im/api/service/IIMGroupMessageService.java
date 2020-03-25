package com.zhangwuji.im.api.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhangwuji.im.api.entity.IMGroupMessage;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.Map;

/**
 * <p>
 * IM群消息表 服务类
 * </p>
 *
 * @author cloudtalk
 * @since 2019-08-09
 */
public interface IIMGroupMessageService extends IService<IMGroupMessage> {
     Page<Map<String, Object>> getMessageList(Page<Map<String, Object>> page, int groupid);
     void del_group_message(int groupid,int msgId);
}
