package com.zhangwuji.im.api.service.impl;

import com.zhangwuji.im.api.entity.IMUserGeoData;
import com.zhangwuji.im.api.mapper.IMUserGeoDataMapper;
import com.zhangwuji.im.api.service.IIMUserGeoDataService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author cloudtalk
 * @since 2019-01-10
 */
@Service
@Qualifier(value = "imUserGeoDataService")
public class IMUserGeoDataServiceImpl extends ServiceImpl<IMUserGeoDataMapper, IMUserGeoData> implements IIMUserGeoDataService {

}
