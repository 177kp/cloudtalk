package com.zhangwuji.im.api.mapper;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhangwuji.im.api.entity.LuckymoneyLog;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;
import java.util.Map;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author cloudtalk
 * @since 2019-03-23
 */
public interface LuckymoneyLogMapper extends BaseMapper<LuckymoneyLog> {

    //抢红包执行
    @Update("update on_luckymoney_log set uid=#{uid},status=1 where id=#{id}")
    Integer setLuckMoneyLog(Integer uid,Integer id);

    @Select("select id from on_luckymoney_log where uid=0  and status=0  and pid=#{pid}  limit 1 for update")
    Map<String, Object> getLuckMoneyLog(Integer pid);

    @Select("select log.*,user.nickname,user.avatar from on_luckymoney_log log left join on_IMUser user on log.uid=user.id where log.pid=#{pid} and log.status=1")
    List<Map<String, Object>> getLuckMoneyLogs(Page page,Integer pid);

    @Select("select log.*,user.nickname,user.avatar from on_luckymoney_log log left join on_IMUser user on log.uid=user.id where log.uid=#{uid} and log.status=1")
    List<Map<String, Object>> getMyLuckMoneyLog(Page page,Integer uid);
}
