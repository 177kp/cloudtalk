package com.zhangwuji.im.api.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhangwuji.im.api.entity.IMUser;
import com.zhangwuji.im.api.mapper.IMUserMapper;
import com.zhangwuji.im.api.service.IIMUserService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhangwuji.im.api.service.IIMUserFriendsService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author cloudtalk
 * @since 2019-01-04
 */
@Service
@Qualifier(value = "imUserService")
public class IMUserServiceImpl extends ServiceImpl<IMUserMapper, IMUser> implements IIMUserService {

    @Resource
    @Qualifier(value = "IMUserFriendsService")
    private IIMUserFriendsService iOnImuserfriendsService;

    @Override
    public List<IMUser> findUserById(Integer id) {
        // TODO Auto-generated method stub
        return baseMapper.findUserById(id);
    }

    //通过映射到mapper.xml的方法 查询部分字段，返回的是list map类型
    @Override
    public List<Map<String, Object>> selectUser2() {
        return baseMapper.selectUser2();
    }

    @Override
    public Page<IMUser> getAllUserBypage(Page<IMUser> page) {
        return page.setRecords(this.baseMapper.getAllUserBypage(page));
    }

    @Override
    public List<Map<String, Object>> getUsersInfo(String ids) {
        return baseMapper.getUsersInfo(ids.split("\\,"));
    }

}
