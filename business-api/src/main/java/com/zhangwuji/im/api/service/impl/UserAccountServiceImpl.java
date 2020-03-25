package com.zhangwuji.im.api.service.impl;

import com.zhangwuji.im.api.entity.UserAccount;
import com.zhangwuji.im.api.mapper.UserAccountMapper;
import com.zhangwuji.im.api.service.IUserAccountService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author cloudtalk
 * @since 2019-04-14
 */
@Service
@Qualifier(value = "iUserAccountService")
public class UserAccountServiceImpl extends ServiceImpl<UserAccountMapper, UserAccount> implements IUserAccountService {

}
