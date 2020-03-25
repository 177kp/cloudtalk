package com.zhangwuji.im.api.service.impl;

import com.zhangwuji.im.api.entity.IMDiscovery;
import com.zhangwuji.im.api.mapper.IMDiscoveryMapper;
import com.zhangwuji.im.api.service.IIMDiscoveryService;
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
@Qualifier(value = "IMDiscoveryService")
public class IMDiscoveryServiceImpl extends ServiceImpl<IMDiscoveryMapper, IMDiscovery> implements IIMDiscoveryService {

}
