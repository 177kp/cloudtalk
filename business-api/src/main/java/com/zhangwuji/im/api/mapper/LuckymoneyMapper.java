package com.zhangwuji.im.api.mapper;

import com.zhangwuji.im.api.entity.Luckymoney;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zhangwuji.im.api.entity.LuckymoneyLog;
import org.apache.ibatis.annotations.Update;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author cloudtalk
 * @since 2019-03-23
 */
public interface LuckymoneyMapper extends BaseMapper<Luckymoney> {

    //抢红包执行
    @Update("update on_luckymoney set usenum=usenum+1,usemoney=usemoney+#{money} where id=#{id}")
    Integer setLuckMoneyInfo(double money,Integer id);

}
