package com.zhangwuji.im.api.mapper;

import com.zhangwuji.im.api.entity.IMGroupMember;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 用户和群的关系表 Mapper 接口
 * </p>
 *
 * @author cloudtalk
 * @since 2019-01-15
 */
public interface IMGroupMemberMapper extends BaseMapper<IMGroupMember> {
    List<Map<String, Object>> getGroupMemberList(String[] array);

    @Select("select a.id as groupId,u.id,u.id as peerId,u.username,u.nickname,u.realname,u.sex,u.avatar,u.domain,u.phone,u.email,u.sign_info,u.updated,u.created from  on_IMGroupMember a LEFT JOIN on_IMUser u ON a.userId=u.id  where  a.groupId=#{id}")
    List<Map<String, Object>> getGroupMemberInfoById(Integer id);


}
