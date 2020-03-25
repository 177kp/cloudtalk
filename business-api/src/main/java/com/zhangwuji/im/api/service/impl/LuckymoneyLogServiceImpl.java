package com.zhangwuji.im.api.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhangwuji.im.api.entity.LuckymoneyLog;
import com.zhangwuji.im.api.mapper.LuckymoneyLogMapper;
import com.zhangwuji.im.api.service.ILuckymoneyLogService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author cloudtalk
 * @since 2019-03-23
 */
@Service
public class LuckymoneyLogServiceImpl extends ServiceImpl<LuckymoneyLogMapper, LuckymoneyLog> implements ILuckymoneyLogService {

    public Integer setLuckMoneyLog(Integer uid,Integer id)
    {
       return baseMapper.setLuckMoneyLog(uid,id);
    }

    public Map<String, Object> getLuckMoneyLog(Integer pid)
    {

        return baseMapper.getLuckMoneyLog(pid);
    }

    public Page<Map<String, Object>> getLuckymoneyLog(Page<Map<String, Object>> page,Integer id) {
        return page.setRecords(baseMapper.getLuckMoneyLogs(page,id));
    }

    public Page<Map<String, Object>> getMyLuckymoneyLog(Page<Map<String, Object>> page,Integer uid) {
        return page.setRecords(baseMapper.getMyLuckMoneyLog(page,uid));
    }
}
