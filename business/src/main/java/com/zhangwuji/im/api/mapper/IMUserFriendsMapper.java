package com.zhangwuji.im.api.mapper;

import com.zhangwuji.im.api.entity.IMUserFriends;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author cloudtalk
 * @since 2019-01-04
 */
public interface IMUserFriendsMapper extends BaseMapper<IMUserFriends> {

    @Select("select user.id,nickname,avatar,friends.remark as displayName,friends.message,friends.status,friends.updated from on_IMUserFriends friends LEFT JOIN on_IMUser user ON user.id = friends.friuid  where friends.uid=#{id} order by friends.status desc ")
    List<Map<String, Object>> getMyNewFriends(Integer id);

    @Select("select user.id,user.id as peerId,username,nickname,realname,sex,avatar,domain,phone,email,sign_info,user.updated,user.created,friends.groupId as departmentId from on_IMUserFriends friends LEFT JOIN on_IMUser user ON user.id = friends.friuid  where friends.uid=#{id}  and friends.status=1 ")
    List<Map<String, Object>> getMyFriends(Integer id);
}
