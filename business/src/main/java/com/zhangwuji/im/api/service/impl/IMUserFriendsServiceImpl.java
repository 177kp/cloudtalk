package com.zhangwuji.im.api.service.impl;

import com.zhangwuji.im.api.entity.IMUserFriends;
import com.zhangwuji.im.api.mapper.IMUserFriendsMapper;
import com.zhangwuji.im.api.service.IIMUserFriendsService;
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
 * @since 2019-01-04
 */
@Service
@Qualifier(value = "IMUserFriendsService")
public class IMUserFriendsServiceImpl extends ServiceImpl<IMUserFriendsMapper, IMUserFriends> implements IIMUserFriendsService {

    public List<Map<String, Object>> getMyNewFriends(Integer id)
    {
        return baseMapper.getMyNewFriends(id);
    }

    @Override
    public List<Map<String, Object>> getMyFriends(Integer id) {
        return baseMapper.getMyFriends(id);
    }
}
