package com.zhangwuji.im.api.service;

import com.zhangwuji.im.api.entity.Userpaylog;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author cloudtalk
 * @since 2019-03-23
 */
public interface IUserpaylogService extends IService<Userpaylog> {
    List<Userpaylog> getLogList(int uid, int status,int page, int pagesize);
}
