package com.zhangwuji.im.api.mapper;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhangwuji.im.api.entity.IMGroupMessage;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * IM群消息表 Mapper 接口
 * </p>
 *
 * @author cloudtalk
 * @since 2019-08-09
 */
public interface IMGroupMessageMapper extends BaseMapper<IMGroupMessage> {

    @Select("select t1.*,t2.nickname,t2.avatar  from `on_IMGroupMessage` t1 , on_IMUser t2  where t1.userId=t2.id  and t1.groupId=#{groupId}  order by t1.id desc")
    List<Map<String, Object>> get_group_message(Page page, @Param("tableid") int tableid, @Param("groupId") int groupId);

    @Delete("delete from `on_IMGroupMessage`  where groupId=#{groupId} and msgId=#{msgId} ")
    void del_group_message(@Param("tableid") int tableid, @Param("groupId") int groupId, @Param("msgId") int msgId);

}
