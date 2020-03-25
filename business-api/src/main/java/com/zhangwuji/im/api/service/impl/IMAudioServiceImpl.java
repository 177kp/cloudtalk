package com.zhangwuji.im.api.service.impl;

import com.zhangwuji.im.api.entity.IMAudio;
import com.zhangwuji.im.api.mapper.IMAudioMapper;
import com.zhangwuji.im.api.service.IIMAudioService;
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
@Qualifier(value = "IMAudioService")
public class IMAudioServiceImpl extends ServiceImpl<IMAudioMapper, IMAudio> implements IIMAudioService {

}
