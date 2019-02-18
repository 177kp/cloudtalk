package com.zhangwuji.im.api.service.impl;

import com.zhangwuji.im.api.entity.IMDepart;
import com.zhangwuji.im.api.mapper.IMDepartMapper;
import com.zhangwuji.im.api.service.IIMDepartService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author cloudtalk
 * @since 2019-01-15
 */
@Service
@Qualifier(value = "IMDepartService")
public class IMDepartServiceImpl extends ServiceImpl<IMDepartMapper, IMDepart> implements IIMDepartService {

    @Override
    public List<Map<String, Object>> getMyAllDepart(Integer id) {
        return baseMapper.getMyAllDepart(id);
    }
}
