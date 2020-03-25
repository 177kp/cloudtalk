package com.zhangwuji.im.api.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhangwuji.im.api.entity.IMMessage;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.Map;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author cloudtalk
 * @since 2019-08-09
 */
public interface IIMMessageService extends IService<IMMessage> {

    Page<Map<String, Object>> getMessageList(Page<Map<String, Object>> page,int uid, int touid);
    public void deleteMessage(int fromid, int toid,int msgId);
    Map<String, Object>  get_user_max_msgid(int tableid,int relateId);

}
