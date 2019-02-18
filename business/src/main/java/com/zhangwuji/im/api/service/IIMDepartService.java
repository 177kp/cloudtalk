package com.zhangwuji.im.api.service;

import com.zhangwuji.im.api.entity.IMDepart;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;
import java.util.Map;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author cloudtalk
 * @since 2019-01-15
 */
public interface IIMDepartService extends IService<IMDepart> {

    List<Map<String, Object>> getMyAllDepart(Integer id);

}
