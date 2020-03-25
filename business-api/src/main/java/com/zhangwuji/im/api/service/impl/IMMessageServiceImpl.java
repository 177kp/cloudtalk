package com.zhangwuji.im.api.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhangwuji.im.api.entity.IMMessage;
import com.zhangwuji.im.api.entity.Userpaylog;
import com.zhangwuji.im.api.mapper.IMMessageMapper;
import com.zhangwuji.im.api.service.IIMMessageService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author cloudtalk
 * @since 2019-08-09
 */
@Service
public class IMMessageServiceImpl extends ServiceImpl<IMMessageMapper, IMMessage> implements IIMMessageService {

    @Override
    public Page<Map<String, Object>> getMessageList(Page<Map<String, Object>> page,int uid, int touid)
    {
        int tableid=(uid+touid)%8;
        return page.setRecords(baseMapper.get_user_message(page,tableid,uid,touid));
    }

    @Override
    public void deleteMessage(int fromid, int toid,int msgId)
    {
         int tableid=(fromid+toid)%8;
         baseMapper.del_message(tableid,fromid,toid,msgId);
    }

    @Override
    public Map<String, Object> get_user_max_msgid(int tableid, int relateId) {
       return  baseMapper.get_user_max_msgid(tableid,relateId);
    }

}
