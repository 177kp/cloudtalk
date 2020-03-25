package com.zhangwuji.im.api.service.impl;

import com.zhangwuji.im.api.entity.Luckymoney;
import com.zhangwuji.im.api.mapper.LuckymoneyMapper;
import com.zhangwuji.im.api.service.ILuckymoneyService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author cloudtalk
 * @since 2019-03-23
 */
@Service
public class LuckymoneyServiceImpl extends ServiceImpl<LuckymoneyMapper, Luckymoney> implements ILuckymoneyService {

    public Integer setLuckMoneyInfo(double money,Integer id)
    {
        return baseMapper.setLuckMoneyInfo(money,id);
    }

}
