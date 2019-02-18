package com.zhangwuji.im.api.service.impl;

import com.zhangwuji.im.api.entity.IMGroup;
import com.zhangwuji.im.api.entity.IMUser;
import com.zhangwuji.im.api.mapper.IMGroupMapper;
import com.zhangwuji.im.api.service.IIMGroupService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * IM群信息 服务实现类
 * </p>
 *
 * @author cloudtalk
 * @since 2019-01-15
 */
@Service
@Qualifier(value = "IMGroupService")
public class IMGroupServiceImpl extends ServiceImpl<IMGroupMapper, IMGroup> implements IIMGroupService {
    @Override
    public List<Map<String, Object>> getGroupList(String ids) {
        return baseMapper.getGroupList(ids.split("\\,"));
    }

    @Override
    public List<Map<String, Object>> getMyGroupList(Integer id)
    {
       return baseMapper.getMyGroupList(id);
    }
}
