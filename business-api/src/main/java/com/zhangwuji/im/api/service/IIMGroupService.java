package com.zhangwuji.im.api.service;

import com.zhangwuji.im.api.entity.IMGroup;
import com.baomidou.mybatisplus.extension.service.IService;
import com.zhangwuji.im.api.entity.IMUser;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * IM群信息 服务类
 * </p>
 *
 * @author cloudtalk
 * @since 2019-01-15
 */
public interface IIMGroupService extends IService<IMGroup> {

    List<Map<String, Object>> getGroupList(String ids);

    List<Map<String, Object>> getMyGroupList(Integer id);


}
