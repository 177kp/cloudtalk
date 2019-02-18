package com.zhangwuji.im.api.mapper;

import com.zhangwuji.im.api.entity.IMUser;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

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

@Component
@Qualifier("onImuserMapper")
public interface IMUserMapper extends BaseMapper<IMUser>
{

    List<Map<String, Object>> selectUser2();

    @Select("select * from on_IMUser where id = #{id}")
    @Results({
            @Result(column="id",property="id"),
            @Result(column="appId",property="appId"),
            @Result(column="outId",property="outId"),
            @Result(column="username",property="username"),
            @Result(column="apiToken",property="apiToken"),
            @Result(column="nickname",property="nickname"),
            @Result(column="realname",property="realname"),
            @Result(column="sex",property="sex"),
            @Result(column="avatar",property="avatar"),
            @Result(column="domain",property="domain"),
            @Result(column="phone",property="phone"),
            @Result(column="email",property="email"),
            @Result(column="departId",property="departId"),
            @Result(column="signInfo",property="signInfo"),
            @Result(column="status",property="status"),
            @Result(column="updated",property="updated"),
            @Result(column="created",property="created"),
            @Result(column="updated",property="updated")
    })

    List<IMUser> findUserById(Integer id);

    @Select("select * from on_IMUser ")
    List<IMUser> getAllUserBypage(Page page);


    List<Map<String, Object>> getUsersInfo(String[] array);

}
