package com.zhangwuji.im.api.service.impl;

import com.zhangwuji.im.api.entity.IMRecentSession;
import com.zhangwuji.im.api.mapper.IMRecentSessionMapper;
import com.zhangwuji.im.api.service.IIMRecentSessionService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author cloudtalk
 * @since 2019-01-15
 */
@Service
@Qualifier(value = "IMRecentSessionService")
public class IMRecentSessionServiceImpl extends ServiceImpl<IMRecentSessionMapper, IMRecentSession> implements IIMRecentSessionService {

}
