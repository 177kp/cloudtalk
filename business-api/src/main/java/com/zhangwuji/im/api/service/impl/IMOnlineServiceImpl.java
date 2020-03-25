package com.zhangwuji.im.api.service.impl;

import com.zhangwuji.im.api.entity.IMOnline;
import com.zhangwuji.im.api.mapper.IMOnlineMapper;
import com.zhangwuji.im.api.service.IIMOnlineService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author cloudtalk
 * @since 2020-03-02
 */
@Service
@Qualifier(value = "IMOnlineService")
public class IMOnlineServiceImpl extends ServiceImpl<IMOnlineMapper, IMOnline> implements IIMOnlineService {

}
