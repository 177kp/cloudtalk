package com.zhangwuji.im.api.service.impl;

import com.zhangwuji.im.api.entity.IMTransmitFile;
import com.zhangwuji.im.api.mapper.IMTransmitFileMapper;
import com.zhangwuji.im.api.service.IIMTransmitFileService;
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
@Qualifier(value = "IMTransmitFileService")
public class IMTransmitFileServiceImpl extends ServiceImpl<IMTransmitFileMapper, IMTransmitFile> implements IIMTransmitFileService {

}
