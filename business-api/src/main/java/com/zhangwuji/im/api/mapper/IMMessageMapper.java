package com.zhangwuji.im.api.mapper;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhangwuji.im.api.entity.IMMessage;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author cloudtalk
 * @since 2019-08-09
 */
public interface IMMessageMapper extends BaseMapper<IMMessage> {


    @Delete("delete  from `on_IMMessage` where  fromId=#{fromid} and toId=#{toid} and msgId=#{msgId}")
    void del_message(@Param("tableid") int tableid, @Param("fromid") int fromId, @Param("toid")int toId, @Param("msgId")int msgId);

    @Select("select t1.* from `on_IMMessage` t1  where (t1.fromId=#{uid} or t1.fromId=#{touid} ) and (t1.toId=#{uid} or t1.toId=#{touid} ) order by t1.id desc ")
    List<Map<String, Object>> get_user_message(Page page, @Param("tableid") int tableid, @Param("uid") int uid, @Param("touid")int touid);

    @Select("select MAX(msgId) as msgId  FROM  `on_IMMessage` t1 where t1.relateId=#{relateId}")
    Map<String, Object> get_user_max_msgid(@Param("tableid") int tableid, @Param("relateId") int relateId);
}
