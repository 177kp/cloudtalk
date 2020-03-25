package com.zhangwuji.im.api.controller;


import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.zhangwuji.im.api.common.ControllerUtil;
import com.zhangwuji.im.api.common.JavaBeanUtil;
import com.zhangwuji.im.api.common.RedPackeUtil;
import com.zhangwuji.im.api.entity.IMUser;
import com.zhangwuji.im.api.entity.UserAccount;
import com.zhangwuji.im.api.result.ApiResult;
import com.zhangwuji.im.api.service.*;
import com.zhangwuji.im.config.RedisCacheHelper;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author cloudtalk
 * @since 2019-04-14
 */
@RestController
@RequestMapping("/api/user-account")
public class UserAccountController {


    @Resource
    RedisCacheHelper redisHelper;
    @Resource
    JavaBeanUtil javaBeanUtil;

    @Resource
    ControllerUtil controllerUtil;

    @Resource
    RedPackeUtil redPackeUtil;

    @Resource
    @Qualifier(value = "imUserService")
    private IIMUserService iimUserService;
    @Resource
    @Qualifier(value = "imUserGeoDataService")
    private IIMUserGeoDataService iimUserGeoDataService;
    @Resource
    @Qualifier(value = "IMGroupService")
    private IIMGroupService iimGroupService;
    @Resource
    @Qualifier(value = "IMGroupMemberService")
    private IIMGroupMemberService iimGroupMemberService;

    @Resource
    @Qualifier(value = "IMUserFriendsService")
    private IIMUserFriendsService iimUserFriendsService;

    @Resource
    @Qualifier(value = "IMDepartService")
    private IIMDepartService iimDepartService;

    @Resource
    @Qualifier(value = "iUserAccountService")
    private IUserAccountService iUserAccountService;

    @RequestMapping(value = "check_paypwd", method = RequestMethod.POST,produces="application/json;charset=UTF-8")
    public ApiResult check_paypwd(HttpServletRequest req, HttpServletResponse rsp) {

        ApiResult returnResult = new ApiResult();
        Map<String, Object> returnData = new HashMap<>();
        List<Map<String, Object>> returnFriendsList = new LinkedList<>();
        List<Map<String, Object>> userDepartList = new LinkedList<>();
        List<Map<String, Object>> friendsList = new LinkedList<>();
        IMUser myinfo = controllerUtil.checkToken(req);
        if (myinfo == null) {
            returnResult.setCode(ApiResult.ERROR);
            returnResult.setData(returnData);
            returnResult.setMessage("token验证失败!");
            return returnResult;
        }

        UserAccount userAccount=iUserAccountService.getOne(new QueryWrapper<UserAccount>().eq("uid",myinfo.getId()));
        if(userAccount!=null && userAccount.getPayPassword().length()>0)
        {
            returnResult.setCode(ApiResult.SUCCESS);
            returnResult.setData(returnFriendsList);
            returnResult.setMessage("账户正常!");
            return returnResult;
        }
        else
        {
            returnResult.setCode(ApiResult.ERROR);
            returnResult.setData(returnFriendsList);
            returnResult.setMessage("需要设置支付密码!");
            return returnResult;
        }
    }

    @RequestMapping(value = "edit_paypwd", method = RequestMethod.POST,produces="application/json;charset=UTF-8")
    public ApiResult edit_paypwd(HttpServletRequest req, HttpServletResponse rsp) {

        ApiResult returnResult = new ApiResult();
        Map<String, Object> returnData = new HashMap<>();
        List<Map<String, Object>> returnFriendsList = new LinkedList<>();
        List<Map<String, Object>> userDepartList = new LinkedList<>();
        List<Map<String, Object>> friendsList = new LinkedList<>();
        IMUser myinfo = controllerUtil.checkToken(req);
        if (myinfo == null) {
            returnResult.setCode(ApiResult.ERROR);
            returnResult.setData(returnData);
            returnResult.setMessage("token验证失败!");
            return returnResult;
        }

        String oldPWD=req.getParameter("oldPWD");
        String newPWD=req.getParameter("newPWD");
        UserAccount userAccount=iUserAccountService.getOne(new QueryWrapper<UserAccount>().eq("uid",myinfo.getId()));
        if(userAccount!=null && userAccount.getPayPassword().length()>0)
        {
            oldPWD=DigestUtils.md5Hex(oldPWD+myinfo.getSalt()).toLowerCase();
            if(oldPWD.equals(userAccount.getPayPassword()))
            {
                newPWD=DigestUtils.md5Hex(newPWD+myinfo.getSalt()).toLowerCase();
                userAccount.setPayPassword(newPWD);
                iUserAccountService.update(userAccount,new QueryWrapper<UserAccount>().eq("uid",myinfo.getId()));

                returnResult.setCode(ApiResult.SUCCESS);
                returnResult.setData(returnData);
                returnResult.setMessage("支付密码修改成功!");
                return returnResult;
            }
            else
            {
                returnResult.setCode(ApiResult.ERROR);
                returnResult.setData(returnData);
                returnResult.setMessage("支付密码验证失败!");
                return returnResult;
            }
        }
        else
        {
            returnResult.setCode(101);
            returnResult.setData(returnData);
            returnResult.setMessage("还未设置支付密码!");
            return returnResult;
        }


    }
    @RequestMapping(value = "my_account", method = RequestMethod.POST,produces="application/json;charset=UTF-8")
    public ApiResult my_account(HttpServletRequest req, HttpServletResponse rsp) {

        ApiResult returnResult = new ApiResult();
        Map<String, Object> returnData = new HashMap<>();
        List<Map<String, Object>> returnFriendsList = new LinkedList<>();

        IMUser myinfo = controllerUtil.checkToken(req);
        if (myinfo == null) {
            returnResult.setCode(ApiResult.ERROR);
            returnResult.setData(returnData);
            returnResult.setMessage("token验证失败!");
            return returnResult;
        }

        UserAccount userAccount=iUserAccountService.getOne(new QueryWrapper<UserAccount>().eq("uid",myinfo.getId()));
        if(userAccount!=null && userAccount.getPayPassword().length()>0)
        {
            returnResult.setCode(ApiResult.SUCCESS);
            returnResult.setData(userAccount);
            returnResult.setMessage("账户正常!");
            return returnResult;
        }
        else
        {
            returnResult.setCode(ApiResult.ERROR);
            returnResult.setData(returnFriendsList);
            returnResult.setMessage("需要设置支付密码!");
            return returnResult;
        }
    }

    @RequestMapping(value = "set_paypwd", method = RequestMethod.POST,produces="application/json;charset=UTF-8")
    public ApiResult set_paypwd(HttpServletRequest req, HttpServletResponse rsp) {

        ApiResult returnResult = new ApiResult();
        Map<String, Object> returnData = new HashMap<>();
        List<Map<String, Object>> returnFriendsList = new LinkedList<>();
        List<Map<String, Object>> userDepartList = new LinkedList<>();
        List<Map<String, Object>> friendsList = new LinkedList<>();
        IMUser myinfo = controllerUtil.checkToken(req);
        if (myinfo == null) {
            returnResult.setCode(ApiResult.ERROR);
            returnResult.setData(returnData);
            returnResult.setMessage("token验证失败!");
            return returnResult;
        }

        UserAccount userAccount=iUserAccountService.getOne(new QueryWrapper<UserAccount>().eq("uid",myinfo.getId()));
        if(userAccount!=null && userAccount.getPayPassword().length()>0)
        {
            returnResult.setCode(ApiResult.ERROR);
            returnResult.setData(returnFriendsList);
            returnResult.setMessage("已经设置过支付密码!");
            return returnResult;
        }
        else
        {
            String paypwd=controllerUtil.getStringParameter(req,"paypwd","");
            String enPass= DigestUtils.md5Hex(paypwd + myinfo.getSalt()).toLowerCase();

            userAccount=new UserAccount();
            userAccount.setUid(myinfo.getId());
            userAccount.setAvailableMoney(0.00);
            userAccount.setFreezeMoney(0.00);
            userAccount.setRealname(myinfo.getRealname());
            userAccount.setIdcard("0");
            userAccount.setPayPassword(enPass);
            userAccount.setUpdatetime(LocalDateTime.now());
            userAccount.setIdcard("");
            iUserAccountService.save(userAccount);


            returnResult.setCode(ApiResult.SUCCESS);
            returnResult.setData(returnFriendsList);
            returnResult.setMessage("设置成功!");
            return returnResult;
        }
    }




}
