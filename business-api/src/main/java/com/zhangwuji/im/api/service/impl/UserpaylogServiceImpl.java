package com.zhangwuji.im.api.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhangwuji.im.api.entity.OrderList;
import com.zhangwuji.im.api.entity.Userpaylog;
import com.zhangwuji.im.api.mapper.UserpaylogMapper;
import com.zhangwuji.im.api.service.IUserpaylogService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author cloudtalk
 * @since 2019-03-23
 */
@Service
public class UserpaylogServiceImpl extends ServiceImpl<UserpaylogMapper, Userpaylog> implements IUserpaylogService {

    @Override
    public List<Userpaylog> getLogList(int uid, int ctype,int page, int pagesize)
    {
        List<Userpaylog> alllist = this.page(new Page<>(page, pagesize),new QueryWrapper<Userpaylog>().eq("uid",uid).orderByDesc("paytime")).getRecords();
        return  alllist;
    }

}
