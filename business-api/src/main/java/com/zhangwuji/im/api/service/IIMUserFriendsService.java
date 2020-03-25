package com.zhangwuji.im.api.service;

import com.zhangwuji.im.api.entity.IMUserFriends;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;
import java.util.Map;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author cloudtalk
 * @since 2019-01-04
 */

public interface IIMUserFriendsService extends IService<IMUserFriends> {

    List<Map<String, Object>> getMyNewFriends(Integer id);
    List<Map<String, Object>> getMyFriends(Integer id);
    
}
