package com.zhangwuji.im.api.mapper;

import com.zhangwuji.im.api.entity.IMDepart;
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
 * @since 2019-01-15
 */
public interface IMDepartMapper extends BaseMapper<IMDepart> {

    @Select("select id as departId,departName,priority,status,created,updated from on_IMDepart where (uid=0 or uid=#{id}) and status=0 ")
    List<Map<String, Object>> getMyAllDepart(Integer id);

}
