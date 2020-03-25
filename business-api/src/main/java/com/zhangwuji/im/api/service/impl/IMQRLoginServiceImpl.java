package com.zhangwuji.im.api.service.impl;

import com.zhangwuji.im.api.entity.IMQRLogin;
import com.zhangwuji.im.api.mapper.IMQRLoginMapper;
import com.zhangwuji.im.api.service.IIMQRLoginService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author cloudtalk
 * @since 2019-06-19
 */
@Service
@Qualifier(value = "IMQRLoginService")
public class IMQRLoginServiceImpl extends ServiceImpl<IMQRLoginMapper, IMQRLogin> implements IIMQRLoginService {

}
