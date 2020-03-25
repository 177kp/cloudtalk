package com.zhangwuji.im.api.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhangwuji.im.api.entity.IMGroupMessage;
import com.zhangwuji.im.api.mapper.IMGroupMessageMapper;
import com.zhangwuji.im.api.service.IIMGroupMessageService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * <p>
 * IM群消息表 服务实现类
 * </p>
 *
 * @author cloudtalk
 * @since 2019-08-09
 */
@Service
public class IMGroupMessageServiceImpl extends ServiceImpl<IMGroupMessageMapper, IMGroupMessage> implements IIMGroupMessageService {

    @Override
    public Page<Map<String, Object>> getMessageList(Page<Map<String, Object>> page, int groupid)
    {
        int tableid=(groupid)%8;
        return page.setRecords(baseMapper.get_group_message(page,tableid,groupid));
    }

    @Override
    public  void del_group_message(int groupid,int msgId)
    {
        int tableid=(groupid)%8;
        baseMapper.del_group_message(tableid,groupid,msgId);
    }

}
