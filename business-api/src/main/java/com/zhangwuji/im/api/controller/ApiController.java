package com.zhangwuji.im.api.controller;


import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhangwuji.im.api.common.ControllerUtil;
import com.zhangwuji.im.api.common.JavaBeanUtil;
import com.zhangwuji.im.api.common.QRUtil;
import com.zhangwuji.im.api.common.RedPackeUtil;
import com.zhangwuji.im.api.entity.*;
import com.zhangwuji.im.api.result.ApiResult;
import com.zhangwuji.im.api.service.*;
import com.zhangwuji.im.api.service.impl.IMQRLoginServiceImpl;
import com.zhangwuji.im.config.RedisCacheHelper;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort;
import org.springframework.data.geo.GeoResult;
import org.springframework.data.geo.GeoResults;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import sun.rmi.runtime.Log;
import weixin.popular.api.UserAPI;
import weixin.popular.bean.user.User;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.time.LocalDateTime;
import java.util.*;

/**
 * <p>
 * 前端控制器
 * </p>
 * @author cloudtalk
 * @since 2019-01-04
 */
@RestController
@RequestMapping(value = "/api")
public class ApiController {
    @Value("${cloudtalk.bqmmplugin.appid}")
    public String bqmmplugin_appid;
    @Value("${cloudtalk.bqmmplugin.appsecret}")
    public String bqmmplugin_appsecret;
    @Value("${cloudtalk.files.msfsprior}")
    public String files_msfsprior;
    @Value("${cloudtalk.files.msfspriorbackup}")
    public String files_msfspriorbackup;

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
    @Qualifier(value = "IMQRLoginService")
    private  IIMQRLoginService iimqrLoginService;

    @Resource
    @Qualifier(value = "IMOnlineService")
    private IIMOnlineService iimOnlineService;

    public void initUserOnline(Integer uid)
    {
        IMOnline online=iimOnlineService.getOne(new QueryWrapper<IMOnline>().eq("uid",uid));
        if(online==null)
        {
            online=new IMOnline();
            online.setAppid(88888);
            online.setStatus(2);
            online.setUid(uid);
            iimOnlineService.save(online);
        }
    }

    @RequestMapping(value = "test", method = RequestMethod.GET, produces = "application/json;charset=UTF-8")
    public Object test(HttpServletRequest req, HttpServletResponse rsp) {

        return "helloworld!";
    }

    @RequestMapping(value = "get_user_online_status", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
    public ApiResult getUserOnlineStatus(HttpServletRequest req, HttpServletResponse rsp) {
        ApiResult returnResult = new ApiResult();
        Map<String, Object> returnData = new HashMap<>();
        int uid = controllerUtil.getIntParameter(req, "uid", 0);
        if(uid<=0)
        {
            returnResult.setCode(ApiResult.ERROR);
            returnResult.setMessage("参数错误!");
            return  returnResult;
        }
        IMOnline online=iimOnlineService.getOne(new QueryWrapper<IMOnline>().eq("uid",uid));
        if(online!=null)
        {
            long nowtime=controllerUtil.timestamp()-60;
            if(online.getUpdatetime()==null || online.getStatus()==2 || online.getUpdatetime()<nowtime)
            {
                returnData.put("onlineStatus",2);
                returnData.put("onlineText","用户没有在线!");
            }
            else
            {
                returnData.put("onlineStatus",1);
                returnData.put("onlineText","用户在线!");
            }
            returnResult.setCode(ApiResult.SUCCESS);
            returnResult.setData(returnData);
            returnResult.setMessage("获取成功!");
        }
        else
        {
            returnResult.setCode(ApiResult.ERROR);
            returnResult.setMessage("没有此用户!");
        }
        return  returnResult;
    }

    @RequestMapping(value = "getUserToken", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
    public ApiResult getUserToken(HttpServletRequest req, HttpServletResponse rsp) {
        ApiResult returnResult = new ApiResult();
        Map<String, Object> returnData = new HashMap<>();
        ServerInfoEntity serverinfo = new ServerInfoEntity();

        String openid = controllerUtil.getStringParameter(req, "openid", "");
        String username = controllerUtil.getStringParameter(req, "phone", "");
        String nickname = controllerUtil.getStringParameter(req, "nickname", "");
        String avatar = controllerUtil.getStringParameter(req, "avatar", "");
        String regfrom = controllerUtil.getStringParameter(req, "regfrom", "phone");
        IMUser users=null;

        if(regfrom.equals("openid"))
        {
            users=iimUserService.getOne(new QueryWrapper<IMUser>().eq("open_id",openid));
        }
        else
        {
            users=iimUserService.getOne(new QueryWrapper<IMUser>().eq("username",username));
        }

        serverinfo.server_ip="test.cs.zhensuo.tv";
        serverinfo.server_ip2="test.cs.zhensuo.tv";
        serverinfo.server_port=9326;
        returnData.put("serverInfo", serverinfo);

        Integer uid=0;
        if(users!=null)
        {
            Map<String, Object> userinfo = new HashMap<>();
            userinfo.put("userid",users.getId());
            userinfo.put("token",users.getApiToken());
            returnData.put("userInfo", userinfo);
            uid = users.getId();
        }
        else
        {
            int salt = new Random().nextInt(8888) + 1000;
            users = new IMUser();
            users.setAppId(88888);
            users.setAvatar(avatar);
            users.setOutId(0);
            if(regfrom.equals("openid")) {
                users.setUsername(openid);
                users.setPhone("");
            }
            else
            {
                users.setUsername(username);
                users.setPhone(username);
            }
            users.setSalt(salt + "");
            users.setPassword("");
            if (nickname.equals("")) {
                nickname = "cloudtalk"+salt;
            }
            users.setNickname(nickname);
            users.setRealname(nickname);
            users.setApiToken(controllerUtil.getRandomString(32));
            users.setCreated(controllerUtil.timestamp2());
            users.setUpdated(controllerUtil.timestamp2());
            users.setSex(1);
            users.setDomain("0");
            users.setDepartId(1);
            users.setOpenId(openid);
            users.setUnionid("");
            users.setAccessToken("");
            users.setStatus(1);
            users.setRegFrom("zhensuo");
            iimUserService.save(users);

            Map<String, Object> userinfo = new HashMap<>();
            userinfo.put("userid",users.getId());
            userinfo.put("token",users.getApiToken());
            returnData.put("userInfo", userinfo);

            uid = users.getId();
        }

        //初始化在线用户表
        initUserOnline(uid);

        returnResult.setCode(ApiResult.SUCCESS);
        returnResult.setData(returnData);
        returnResult.setMessage("获取成功!");
        return  returnResult;

    }

    @RequestMapping(value = "bingAccount", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
    public ApiResult bingAccount(HttpServletRequest req, HttpServletResponse rsp) {
        ApiResult returnResult = new ApiResult();
        Map<String, Object> returnData = new HashMap<>();
        ServerInfoEntity serverinfo = new ServerInfoEntity();
        Map<String, Object> bmqq_plugin = new HashMap<>();

        int appid = controllerUtil.getIntParameter(req, "appid", 88888);
        String openid = controllerUtil.getStringParameter(req, "openid", "");
        String username = controllerUtil.getStringParameter(req, "username", "");
        String code = controllerUtil.getStringParameter(req, "code", "");
        String tjcode = controllerUtil.getStringParameter(req, "tjcode", "");

        IMUser users=iimUserService.getOne(new QueryWrapper<IMUser>().eq("open_id",openid));
        IMUser users2=iimUserService.getOne(new QueryWrapper<IMUser>().eq("code",tjcode));
        if(users!=null && username!=null)
        {
            IMUser bindUser=iimUserService.getOne(new QueryWrapper<IMUser>().eq("username",username));
            if(bindUser!=null)
            {
                bindUser.setOpenId(users.getOpenId());
                bindUser.setAccessToken(users.getAccessToken());
                bindUser.setAvatar(users.getAvatar());
                bindUser.setUnionid(users.getUnionid());

                //删掉原来的临时用户
                iimUserService.removeById(users.getId());

                users=bindUser;
            }
            if(users2!=null)
            {
                users.setTopuid(users2.getId());
            }
            users.setStatus(0);
            iimUserService.updateById(users);

            //*********从redis中获取 负载量小的 聊天服务器************
            //****************************************************
            Map<Object, Object> serverlistmap = new HashMap<>();
            String selectServerInfo = "";
            serverlistmap = redisHelper.hmget("msg_srv_list");
            if (serverlistmap != null && serverlistmap.size() > 0) {
                serverlistmap = javaBeanUtil.sortMapByValue(serverlistmap);
                selectServerInfo = javaBeanUtil.getFirstKeyFromMap(serverlistmap).toString();
                serverinfo.setServer_ip(selectServerInfo.split("\\|")[0]);
                serverinfo.setServer_ip2(selectServerInfo.split("\\|")[1]);
                serverinfo.setServer_port(Integer.parseInt(selectServerInfo.split("\\|")[2]));
                serverinfo.setMsfsPrior(files_msfsprior);
                serverinfo.setMsfsBackup(files_msfspriorbackup);
            }

            bmqq_plugin.put("appid", bqmmplugin_appid);
            bmqq_plugin.put("appsecret", bqmmplugin_appsecret);


            Map<String, Object> returnUsers = JavaBeanUtil.convertBeanToMap(users);
            returnUsers.put("peerId", users.getId());
            returnUsers.remove("password");

            returnData.put("token", users.getApiToken());
            returnData.put("userinfo", returnUsers);
            returnData.put("serverinfo", serverinfo);
            returnData.put("bqmmplugin", bmqq_plugin);
            iimUserService.updateById(users);


            //初始化在线用户表
            initUserOnline(users.getId());


            returnResult.setCode(ApiResult.SUCCESS);
            returnResult.setData(returnData);
            returnResult.setMessage("登录成功!");

            return  returnResult;
        }

        returnResult.setCode(100);
        returnResult.setData(null);
        returnResult.setMessage("参数不全!");
        return returnResult;
    }

    @RequestMapping(value = "bingWeiXinLogin", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
    public ApiResult bindWeiXinLogin(HttpServletRequest req, HttpServletResponse rsp) {

        ApiResult returnResult = new ApiResult();
        Map<String, Object> returnData = new HashMap<>();
        ServerInfoEntity serverinfo = new ServerInfoEntity();
        Map<String, Object> bmqq_plugin = new HashMap<>();


        int appid = controllerUtil.getIntParameter(req, "appid", 88888);
        String openid = controllerUtil.getStringParameter(req, "openid", "");
        String access_token = controllerUtil.getStringParameter(req, "access_token", "");
        String unionid = controllerUtil.getStringParameter(req, "unionid", "");
        String nickname = controllerUtil.getStringParameter(req, "nickname", "");
        String avatar = controllerUtil.getStringParameter(req, "avatar", "");
        int sex = controllerUtil.getIntParameter(req, "sex", 0);

        if(openid.equals("")||access_token.equals(""))
        {
            returnResult.setCode(100);
            returnResult.setData(null);
            returnResult.setMessage("参数不全!");
            return returnResult;
        }

        int salt = new Random().nextInt(8888) + 1000;
        IMUser users=iimUserService.getOne(new QueryWrapper<IMUser>().eq("open_id",openid));
        if(users==null)
        {
            users = new IMUser();
            users.setAppId(appid);
            users.setAvatar(avatar);
            users.setOutId(0);
            users.setUsername(openid);
            users.setSalt(salt + "");
            users.setPassword("");
            if (nickname.equals("")) {
                nickname = "cloudtalk"+openid;
            }
            users.setNickname(nickname);
            users.setRealname(nickname);
            users.setApiToken(controllerUtil.getRandomString(32));
            users.setCreated(controllerUtil.timestamp2());
            users.setUpdated(controllerUtil.timestamp2());
            users.setSex(sex+1);
            users.setDomain("0");
            users.setPhone("");
            users.setDepartId(1);
            users.setOpenId(openid);
            users.setUnionid(unionid);
            users.setAccessToken(access_token);
            users.setStatus(9);
            users.setRegFrom("wx");
            iimUserService.save(users);

            returnResult.setCode(201);
            returnResult.setData(null);
            returnResult.setMessage("需要绑定账号!");
            return returnResult;
        }
        else
        {
            //需要绑定账号
            if(users.getStatus()==9)
            {
                returnResult.setCode(201);
                returnResult.setData(null);
                returnResult.setMessage("需要绑定账号!");
                return returnResult;
            }
            else
            {
                //*********从redis中获取 负载量小的 聊天服务器************
                //****************************************************
                Map<Object, Object> serverlistmap = new HashMap<>();
                String selectServerInfo = "";
                serverlistmap = redisHelper.hmget("msg_srv_list");
                if (serverlistmap != null && serverlistmap.size() > 0) {
                    serverlistmap = javaBeanUtil.sortMapByValue(serverlistmap);
                    selectServerInfo = javaBeanUtil.getFirstKeyFromMap(serverlistmap).toString();
                    serverinfo.setServer_ip(selectServerInfo.split("\\|")[0]);
                    serverinfo.setServer_ip2(selectServerInfo.split("\\|")[1]);
                    serverinfo.setServer_port(Integer.parseInt(selectServerInfo.split("\\|")[2]));
                    serverinfo.setMsfsPrior(files_msfsprior);
                    serverinfo.setMsfsBackup(files_msfspriorbackup);
                }

                bmqq_plugin.put("appid", bqmmplugin_appid);
                bmqq_plugin.put("appsecret", bqmmplugin_appsecret);


                Map<String, Object> returnUsers = JavaBeanUtil.convertBeanToMap(users);
                returnUsers.put("peerId", users.getId());
                returnUsers.remove("password");

                returnData.put("token", users.getApiToken());
                returnData.put("userinfo", returnUsers);
                returnData.put("serverinfo", serverinfo);
                returnData.put("bqmmplugin", bmqq_plugin);
                iimUserService.updateById(users);


                //初始化在线用户表
                initUserOnline(users.getId());

                returnResult.setCode(ApiResult.SUCCESS);
                returnResult.setData(returnData);
                returnResult.setMessage("登录成功!");

                return  returnResult;
            }
        }
    }

    @RequestMapping(value = "addQRLogin", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
    public ApiResult addQRLogin(HttpServletRequest req, HttpServletResponse rsp) {

        String code = controllerUtil.getStringParameter(req, "code", "");
        String qrcode = controllerUtil.getStringParameter(req, "qrcode", "");
        ApiResult returnResult = new ApiResult();
        Map<String, Object> returnData = new HashMap<>();

        IMQRLogin imqrLogin= iimqrLoginService.getOne(new QueryWrapper<IMQRLogin>().eq("code",code));
        if(imqrLogin==null)
        {
            imqrLogin=new IMQRLogin();
            imqrLogin.setCode(code);
            imqrLogin.setUid(0);
            imqrLogin.setLogintime(LocalDateTime.now());
            iimqrLoginService.save(imqrLogin);
        }
        else
        {
            imqrLogin.setUid(0);
            iimqrLoginService.updateById(imqrLogin);
        }

        String base64code="";
        if(!qrcode.equals(""))
        {
            base64code= "data:image/jpg;base64,"+QRUtil.toBASE64Encoder(qrcode).replaceAll("\r|\n", "");
        }
        returnData.put("qrcode",base64code);
        returnResult.setCode(ApiResult.SUCCESS);
        returnResult.setData(returnData);
        returnResult.setMessage("操作成功!");
        return returnResult;
    }

    @RequestMapping(value = "agreeQRLogin", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
    public ApiResult agreeQRLogin(HttpServletRequest req, HttpServletResponse rsp) {

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
        String code = controllerUtil.getStringParameter(req, "code", "");
        IMQRLogin imqrLogin= iimqrLoginService.getOne(new QueryWrapper<IMQRLogin>().eq("code",code));
        if(imqrLogin==null || imqrLogin.getUid()>0)
        {
            returnResult.setCode(ApiResult.ERROR);
            returnResult.setData(returnData);
            returnResult.setMessage("请刷新二维码!");
            return returnResult;
        }
        else
        {
            imqrLogin.setUid(myinfo.getId());
            imqrLogin.setLogintime(LocalDateTime.now());
            iimqrLoginService.updateById(imqrLogin);
        }

        returnResult.setCode(ApiResult.SUCCESS);
        returnResult.setData(null);
        returnResult.setMessage("操作成功!");
        return returnResult;
    }

    @RequestMapping(value = "checkQRLogin", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
    public ApiResult checkQRLogin(HttpServletRequest req, HttpServletResponse rsp) {

        String code = controllerUtil.getStringParameter(req, "code", "");

        ApiResult returnResult = new ApiResult();
        Map<String, Object> returnData = new HashMap<>();
        ServerInfoEntity serverinfo = new ServerInfoEntity();
        Map<String, Object> bmqq_plugin = new HashMap<>();

        IMQRLogin imqrLogin= iimqrLoginService.getOne(new QueryWrapper<IMQRLogin>().eq("code",code));
        if(imqrLogin!=null && imqrLogin.getUid()>0)
        {
            int uid=imqrLogin.getUid();

            IMUser users=iimUserService.getById(uid);

            //*********从redis中获取 负载量小的 聊天服务器************
            //****************************************************
            Map<Object, Object> serverlistmap = new HashMap<>();
            String selectServerInfo = "";
            serverlistmap = redisHelper.hmget("msg_srv_list");
            if (serverlistmap != null && serverlistmap.size() > 0) {
                serverlistmap = javaBeanUtil.sortMapByValue(serverlistmap);
                selectServerInfo = javaBeanUtil.getFirstKeyFromMap(serverlistmap).toString();
                serverinfo.setServer_ip(selectServerInfo.split("\\|")[0]);
                serverinfo.setServer_ip2(selectServerInfo.split("\\|")[1]);
                serverinfo.setServer_port(Integer.parseInt(selectServerInfo.split("\\|")[2]));
                serverinfo.setMsfsPrior(files_msfsprior);
                serverinfo.setMsfsBackup(files_msfspriorbackup);
            }

            bmqq_plugin.put("appid", bqmmplugin_appid);
            bmqq_plugin.put("appsecret", bqmmplugin_appsecret);


            Map<String, Object> returnUsers = JavaBeanUtil.convertBeanToMap(users);
            returnUsers.put("peerId", users.getId());
            returnUsers.remove("password");

            returnData.put("token", users.getApiToken());
            returnData.put("userinfo", returnUsers);
            returnData.put("serverinfo", serverinfo);
            returnData.put("bqmmplugin", bmqq_plugin);

            returnResult.setCode(ApiResult.SUCCESS);
            returnResult.setData(returnData);
            returnResult.setMessage("登录成功!");

            return returnResult;
        }
        else
        {
            returnResult.setCode(ApiResult.ERROR);
            returnResult.setData(returnData);
            returnResult.setMessage("未确认!");
            return returnResult;
        }
    }


    @RequestMapping(value = "addFriend", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
    public ApiResult addFriend(HttpServletRequest req, HttpServletResponse rsp) {

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
        int friduid = controllerUtil.getIntParameter(req, "friuid", 0);
        IMUserFriends imUserFriends = iimUserFriendsService.getOne(new QueryWrapper<IMUserFriends>().eq("uid", friduid).eq("friuid", myinfo.getId()));
        if (imUserFriends == null || imUserFriends.getId() <= 0) {
            IMUserFriends addFriend = new IMUserFriends();
            addFriend.setUid(friduid);
            addFriend.setFriuid(myinfo.getId());
            addFriend.setFriName(myinfo.getNickname());
            addFriend.setGroupId(1);
            addFriend.setMessage("已通过好友请求!");
            addFriend.setStatus(22);
            addFriend.setUpdated(controllerUtil.timestamp2());
            addFriend.setCreated(controllerUtil.timestamp2());
            iimUserFriendsService.save(addFriend);

            controllerUtil.sendIMSystemMessage(137, friduid, "FRIEND_INVITE",1,1);

            returnResult.setCode(200);
            returnResult.setData(null);
            returnResult.setMessage("请求成功!");
            return returnResult;
        } else {
            if (imUserFriends.getStatus() == 1) {
                returnResult.setCode(201);
                returnResult.setData(null);
                returnResult.setMessage("已经是好友!");
                return returnResult;
            } else {
                if ((controllerUtil.timestamp2() - imUserFriends.getUpdated()) > 1000 * 60 * 60 * 24) {
                    imUserFriends.setMessage("请求加为好友");
                    imUserFriends.setStatus(2);
                    imUserFriends.setUpdated(controllerUtil.timestamp2());
                    iimUserFriendsService.updateById(imUserFriends);

                    controllerUtil.sendIMSystemMessage(137, friduid, "FRIEND_INVITE",1,1);
                }

                returnResult.setCode(200);
                returnResult.setData(null);
                returnResult.setMessage("请求成功!");
                return returnResult;
            }

        }
    }

    @RequestMapping(value = "sendphoneMsg", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
    public ApiResult sendphoneMsg(HttpServletRequest req, HttpServletResponse rsp) {

        ApiResult returnResult = new ApiResult();
        Map<String, Object> returnData = new HashMap<>();

        String phone = controllerUtil.getStringParameter(req, "phone", "");
        Object code = redisHelper.get("code_" + phone);
        if (code != null) {
            returnResult.setCode(ApiResult.ERROR);
            returnResult.setData(returnData);
            returnResult.setMessage("请稍等后再试!");
            return returnResult;
        }

        try {
           // code = controllerUtil.sendMsg(phone);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (code != null) {
            redisHelper.set("code_" + phone, code, 120);
            returnResult.setCode(ApiResult.SUCCESS);
            returnResult.setData(returnData);
            returnResult.setMessage("验证码发送成功!");
            return returnResult;
        } else {
            returnResult.setCode(ApiResult.ERROR);
            returnResult.setData(returnData);
            returnResult.setMessage("短信发送失败!");
            return returnResult;
        }


    }

    @RequestMapping(value = "agreeFriend", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
    public ApiResult agreeFriend(HttpServletRequest req, HttpServletResponse rsp) {

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

        int friduid = controllerUtil.getIntParameter(req, "friuid", 0);

        IMUserFriends imUserFriends = iimUserFriendsService.getOne(new QueryWrapper<IMUserFriends>().eq("uid", myinfo.getId()).eq("friuid", friduid));
        if (imUserFriends == null || imUserFriends.getId() <= 0) {
            returnResult.setCode(201);
            returnResult.setData(null);
            returnResult.setMessage("没有找到好友请求!");
            return returnResult;
        } else {
            if (imUserFriends.getStatus() == 1) {
                returnResult.setCode(201);
                returnResult.setData(null);
                returnResult.setMessage("已经是好友!");
                return returnResult;
            } else {

                //更新当前记录的状态
                imUserFriends.setMessage("请求加为好友");
                imUserFriends.setStatus(1);
                imUserFriends.setUpdated(controllerUtil.timestamp2());
                iimUserFriendsService.updateById(imUserFriends);


                //给对方加上自已的好友记录
                IMUserFriends addFriend = new IMUserFriends();
                addFriend.setUid(friduid);
                addFriend.setFriuid(myinfo.getId());
                addFriend.setFriName(myinfo.getNickname());
                addFriend.setGroupId(1);
                addFriend.setMessage("已通过好友请求!");
                addFriend.setStatus(1);
                addFriend.setUpdated(controllerUtil.timestamp2());
                addFriend.setCreated(controllerUtil.timestamp2());
                iimUserFriendsService.save(addFriend);

                controllerUtil.sendIMSystemMessage(137, friduid, "FRIEND_AGEREE",1,1);

                returnResult.setCode(200);
                returnResult.setData(null);
                returnResult.setMessage("请求成功!");
                return returnResult;

            }
        }
    }

    @RequestMapping(value = "getFriends", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
    public ApiResult getFriends(HttpServletRequest req, HttpServletResponse rsp) {

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

        //给对方加上自已的好友记录
        IMUserFriends usercheck = iimUserFriendsService.getOne(new QueryWrapper<IMUserFriends>().eq("uid", myinfo.getId()).eq("friuid", 8));
        if(usercheck==null) {
            IMUserFriends addFriend = new IMUserFriends();
            addFriend.setUid(8);
            addFriend.setFriuid(myinfo.getId());
            addFriend.setFriName(myinfo.getNickname());
            addFriend.setGroupId(1);
            addFriend.setMessage("已通过好友请求!");
            addFriend.setStatus(1);
            addFriend.setUpdated(controllerUtil.timestamp2());
            addFriend.setCreated(controllerUtil.timestamp2());
            iimUserFriendsService.save(addFriend);

            IMUserFriends addFriend2 = new IMUserFriends();
            addFriend2.setUid(myinfo.getId());
            addFriend2.setFriuid(8);
            addFriend2.setFriName("CloudTalk官方");
            addFriend2.setGroupId(1);
            addFriend2.setMessage("已通过好友请求!");
            addFriend2.setStatus(1);
            addFriend2.setUpdated(controllerUtil.timestamp2());
            addFriend2.setCreated(controllerUtil.timestamp2());
            iimUserFriendsService.save(addFriend2);

            controllerUtil.sendIMSystemMessage(1, myinfo.getId(), "你好,欢迎体验CloudTalk 3.0,我是官方技术支持,官方提供定制开发服务,有需要联系QQ: 689541 ",1,8);

        }


        returnFriendsList = iimUserFriendsService.getMyFriends(myinfo.getId());
        userDepartList = iimDepartService.getMyAllDepart(myinfo.getId());
        for (Map<String, Object> userMap : returnFriendsList) {
            for (Map<String, Object> departMap : userDepartList) {
                if (userMap.get("departmentId").toString().equals(departMap.get("departId").toString())) {
                    userMap.put("departName", departMap.get("departName").toString());
                    friendsList.add(userMap);
                }
            }
        }

        returnData.put("friendlist", friendsList);
        returnData.put("grouplist", userDepartList);

        returnResult.setCode(ApiResult.SUCCESS);
        returnResult.setData(returnData);
        returnResult.setMessage("查询成功!");
        return returnResult;
    }


    @RequestMapping(value = "getNewFriends", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
    public ApiResult getNewFriends(HttpServletRequest req, HttpServletResponse rsp) {

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

        returnFriendsList = iimUserFriendsService.getMyNewFriends(myinfo.getId());
        returnResult.setCode(ApiResult.SUCCESS);
        returnResult.setData(returnFriendsList);
        returnResult.setMessage("查询成功!");
        return returnResult;
    }

    @RequestMapping(value = "getGroupMembers", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
    public ApiResult getGroupMembers(HttpServletRequest req, HttpServletResponse rsp) {

        ApiResult returnResult = new ApiResult();
        Map<String, Object> returnData = new HashMap<>();

        IMUser myinfo = controllerUtil.checkToken(req);
        if (myinfo == null) {
            returnResult.setCode(ApiResult.ERROR);
            returnResult.setData(returnData);
            returnResult.setMessage("token验证失败!");
            return returnResult;
        }

        int groupId = controllerUtil.getIntParameter(req, "groupid", 0);
        List<Map<String, Object>> groupmemberlist = iimGroupMemberService.getGroupMemberInfoById(groupId);
        returnData.put("memberlist", groupmemberlist);

        returnResult.setCode(ApiResult.SUCCESS);
        returnResult.setData(returnData);
        returnResult.setMessage("查询成功!");
        return returnResult;
    }

    @RequestMapping(value = "getGroupMembers2", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
    public HashMap<String, Object> getGroupMembers2(HttpServletRequest req, HttpServletResponse rsp) {

        ApiResult returnResult = new ApiResult();
        HashMap<String, Object> returnData = new HashMap<>();

        IMUser myinfo = controllerUtil.checkToken(req);
        if (myinfo == null) {
            return null;
        }

        int groupId = controllerUtil.getIntParameter(req, "id", 0);
        IMGroup imGroup=iimGroupService.getById(groupId);

        List<Map<String, Object>> groupmemberlist = iimGroupMemberService.getGroupMemberInfoById(groupId);
        List<HashMap<String, Object>> allmemberlist=new LinkedList<>();
        HashMap<String, Object> data=new HashMap<String, Object>();

        for (Map<String, Object> map:groupmemberlist) {

            HashMap<String, Object> newmap=new HashMap<String, Object>();
            newmap.put("username",map.get("nickname"));
            newmap.put("id",map.get("peerId"));
            newmap.put("avatar",map.get("avatar"));
            newmap.put("sign",map.get("sign_info"));

            if(map.get("peerId").toString().equals(imGroup.getCreator().toString()))
            {
                data.put("owner",newmap);
            }
            else
            {
                allmemberlist.add(newmap);
            }
        }

        data.put("list",allmemberlist);

        returnData.put("code",0);
        returnData.put("msg","");
        returnData.put("data",data);
        returnData.put("members",allmemberlist.size());
        returnData.put("list", groupmemberlist);
        return returnData;
    }

    @RequestMapping(value = "getGroupList", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
    public ApiResult getGroupList(HttpServletRequest req, HttpServletResponse rsp) {

        ApiResult returnResult = new ApiResult();
        Map<String, Object> returnData = new HashMap<>();
        List<Map<String, Object>> returnGrouplist = new LinkedList<>();
        IMUser myinfo = controllerUtil.checkToken(req);

        if (myinfo == null) {
            returnResult.setCode(ApiResult.ERROR);
            returnResult.setData(returnData);
            returnResult.setMessage("token验证失败!");
            return returnResult;
        }


        //群信息列表。
        List<Map<String, Object>> grouplist = iimGroupService.getMyGroupList(myinfo.getId());
        List<String> ids = new LinkedList<>();
        for (Map<String, Object> gmap : grouplist) {
            if (Integer.parseInt(gmap.get("type").toString()) < 3) {
                ids.add(gmap.get("id").toString());
            }
        }

        List<Map<String, Object>> groupmemberlist = iimGroupMemberService.getGroupMemberList(StringUtils.join(ids, ","));

        if (grouplist != null && grouplist.size() > 0) {
            for (Map<String, Object> gmap : grouplist) {
                List<String> uids = new LinkedList<>();
                if (groupmemberlist != null && groupmemberlist.size() > 0) {
                    for (Map<String, Object> umap : groupmemberlist) {
                        if (gmap.get("id").toString().equals(umap.get("groupId").toString())) {
                            uids.add(umap.get("userId").toString());
                        }
                    }
                }
                gmap.put("userlist", StringUtils.join(uids, ","));
                returnGrouplist.add(gmap);
            }
        }

        returnData.put("grouplist", returnGrouplist);
        returnResult.setCode(ApiResult.SUCCESS);
        returnResult.setData(returnData);
        returnResult.setMessage("查询成功!");
        return returnResult;
    }

    @RequestMapping(value = "addGroupMember", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
    public ApiResult addGroupMember(HttpServletRequest req, HttpServletResponse rsp) {
        ApiResult returnResult = new ApiResult();
        Map<String, Object> returnData = new HashMap<>();
        List<Map<String, Object>> returnGrouplist = new LinkedList<>();
        IMUser myinfo = controllerUtil.checkToken(req);
        if (myinfo == null) {
            returnResult.setCode(ApiResult.ERROR);
            returnResult.setData(returnData);
            returnResult.setMessage("token验证失败!");
            return returnResult;
        }

        int id = controllerUtil.getIntParameter(req, "id", 0);
        String uids = controllerUtil.getStringParameter(req, "uids", "");

        IMGroup imGroup=iimGroupService.getById(id);
        if(imGroup!=null)
        {
            for(String userid:uids.split(","))
            {
                IMGroupMember imGroupMember = iimGroupMemberService.getOne(new QueryWrapper<IMGroupMember>().eq("groupId", id).eq("userId", userid));
                if (imGroupMember == null) {
                    imGroupMember = new IMGroupMember();
                    imGroupMember.setCreated(controllerUtil.timestamp2());
                    imGroupMember.setGroupId(id);
                    imGroupMember.setStatus(0);
                    imGroupMember.setUserId(Integer.parseInt(userid));
                    imGroupMember.setUpdated(controllerUtil.timestamp2());
                    imGroupMember.setRemak("");
                    iimGroupMemberService.save(imGroupMember);
                }
            }

            returnResult.setCode(ApiResult.SUCCESS);
            returnResult.setData(returnData);
            returnResult.setMessage("操作成功!");
            return returnResult;
        }
        else
        {
            returnResult.setCode(ApiResult.ERROR);
            returnResult.setData(returnData);
            returnResult.setMessage("群组不存在!");
            return returnResult;
        }

    }

    @RequestMapping(value = "creatGroup", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
    public ApiResult creatGroup(HttpServletRequest req, HttpServletResponse rsp) {

        ApiResult returnResult = new ApiResult();
        Map<String, Object> returnData = new HashMap<>();
        List<Map<String, Object>> returnGrouplist = new LinkedList<>();
        IMUser myinfo = controllerUtil.checkToken(req);

        if (myinfo == null) {
            returnResult.setCode(ApiResult.ERROR);
            returnResult.setData(returnData);
            returnResult.setMessage("token验证失败!");
            return returnResult;
        }

        String name = controllerUtil.getStringParameter(req, "name", "");
        String avatar = controllerUtil.getStringParameter(req, "avatar", "");
        String uids = controllerUtil.getStringParameter(req, "uids", "");

        if(name.equals("")||uids.equals(""))
        {
            returnResult.setCode(ApiResult.ERROR);
            returnResult.setData(returnData);
            returnResult.setMessage("参数不能为空!");
            return returnResult;
        }

        IMGroup group=new IMGroup();
        group.setName(name);
        group.setAvatar(avatar);
        group.setStatus(0);
        group.setUserCnt(uids.split(",").length+1);
        group.setCreator(myinfo.getId());
        group.setVersion(1);
        group.setType(1);
        group.setCreated(controllerUtil.timestamp2());
        group.setUpdated(controllerUtil.timestamp2());
        group.setLastChated(controllerUtil.timestamp2());
        group.setFlag(0);
        iimGroupService.save(group);

        int groupId = group.getId();
        for (String uid:uids.split(","))
        {
            if(!uid.equals("")) {
                IMGroupMember imGroupMember = new IMGroupMember();
                imGroupMember.setCreated(controllerUtil.timestamp2());
                imGroupMember.setGroupId(groupId);
                imGroupMember.setStatus(0);
                imGroupMember.setUserId(Integer.parseInt(uid));
                imGroupMember.setUpdated(controllerUtil.timestamp2());
                imGroupMember.setRemak("");
                iimGroupMemberService.save(imGroupMember);
            }
        }

        returnData.put("groupinfo",group);
        returnResult.setCode(ApiResult.SUCCESS);
        returnResult.setData(returnData);
        returnResult.setMessage("操作成功!");
        return returnResult;
    }


    @RequestMapping(value = "getGroupInfo", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
    public ApiResult getGroupInfo(HttpServletRequest req, HttpServletResponse rsp) {

        ApiResult returnResult = new ApiResult();
        Map<String, Object> returnData = new HashMap<>();
        List<Map<String, Object>> returnGrouplist = new LinkedList<>();
        IMUser myinfo = controllerUtil.checkToken(req);

        if (myinfo == null) {
            returnResult.setCode(ApiResult.ERROR);
            returnResult.setData(returnData);
            returnResult.setMessage("token验证失败!");
            return returnResult;
        }

        String groupIds = controllerUtil.getStringParameter(req, "groupIds", "");

        //群信息列表。
        List<Map<String, Object>> grouplist = iimGroupService.getGroupList(groupIds);
        List<Map<String, Object>> groupmemberlist = iimGroupMemberService.getGroupMemberList(groupIds);
        if (grouplist != null && grouplist.size() > 0) {
            for (Map<String, Object> gmap : grouplist) {
                List<String> uids = new LinkedList<>();
                int flag=0;
                if (groupmemberlist != null && groupmemberlist.size() > 0) {
                    for (Map<String, Object> umap : groupmemberlist) {
                        if (gmap.get("id").toString().equals(umap.get("groupId").toString())) {
                            uids.add(umap.get("userId").toString());
                            if(umap.get("userId").toString().equals(myinfo.getId()+""))
                            {
                                flag= Integer.parseInt(umap.get("disable_send_msg").toString());
                            }
                        }
                    }
                }
                gmap.put("userlist", StringUtils.join(uids, ","));
                gmap.put("MeIsDisableSendMsg",flag);//自已是否被禁言
                returnGrouplist.add(gmap);
            }
        }
        returnData.put("grouplist", returnGrouplist);
        returnResult.setCode(ApiResult.SUCCESS);
        returnResult.setData(returnData);
        returnResult.setMessage("查询成功!");
        return returnResult;
    }


    @RequestMapping(value = "getChatRoomList", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
    public ApiResult getChatRoomList(HttpServletRequest req, HttpServletResponse rsp) {

        ApiResult returnResult = new ApiResult();
        Map<String, Object> returnData = new HashMap<>();
        Map<String, Double> geodata = new HashMap<>();
        IMUser myinfo = controllerUtil.checkToken(req);
        if (myinfo == null) {
            returnResult.setCode(ApiResult.ERROR);
            returnResult.setData(returnData);
            returnResult.setMessage("token验证失败!");
            return returnResult;
        }


        Map<String, Object> groups = iimGroupService.getMap(new QueryWrapper<IMGroup>().eq("type", 3).eq("status", 0));
        returnResult.setCode(ApiResult.SUCCESS);
        returnResult.setData(groups);
        returnResult.setMessage("查询成功!");
        return returnResult;
    }


    @RequestMapping(value = "getNearByUser", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
    public ApiResult getNearByUser(HttpServletRequest req, HttpServletResponse rsp) {

        ApiResult returnResult = new ApiResult();
        Map<String, Object> returnData = new HashMap<>();
        Map<String, Double> geodata = new HashMap<>();


        IMUser myinfo = controllerUtil.checkToken(req);
        if (myinfo == null) {
            returnResult.setCode(ApiResult.ERROR);
            returnResult.setData(returnData);
            returnResult.setMessage("token验证失败!");
            return returnResult;
        }

        int page = controllerUtil.getIntParameter(req, "page", 1);
        int pagesize = controllerUtil.getIntParameter(req, "pagesize", 20);
        double lng = controllerUtil.getDoubleParameter(req, "lng", 0);
        double lat = controllerUtil.getDoubleParameter(req, "lat", 0);


        List<GeoBean> geoBeanList = new LinkedList<>();
        List<GeoBean> pageGeoList = new LinkedList<>();

        //redisHelper.cacheGeo("hunan22",113.37322,23.126153,"1",13600*10);
        redisHelper.cacheGeo("hunan22", 113.37322, 23.126153, "2", 13600 * 1000);
        redisHelper.cacheGeo("hunan22", 113.37322, 23.126153, "3", 13600 * 1000);
        redisHelper.cacheGeo("hunan22", 113.37322, 23.126153, "4", 13600 * 1000);

        redisHelper.cacheGeo("hunan22", lng, lat, myinfo.getId().toString(), 13600 * 10000);

        String geojson = "";


        //流程:先从数据库查找缓存。看有没有缓存数据，如果有的话，直接读取缓存数据进行查分页查找。没有缓存数据时，用redis geo里面进行搜索
        IMUserGeoData imUserGeoData2 = iimUserGeoDataService.getOne(new QueryWrapper<IMUserGeoData>().eq("uid", myinfo.getId()));
        if (imUserGeoData2 != null && imUserGeoData2.getId() > 0 && imUserGeoData2.getUpdated() > 0 && ((controllerUtil.timestamp() - imUserGeoData2.getUpdated()) < 60 * 10)) {
            geojson = imUserGeoData2.getData();
            geoBeanList = JSON.parseArray(geojson, GeoBean.class);
        } else {
            GeoResults<RedisGeoCommands.GeoLocation<Object>> geoResults = redisHelper.radiusGeo("hunan22", lng, lat, 10000000, Sort.Direction.ASC, 100);
            List<GeoResult<RedisGeoCommands.GeoLocation<Object>>> geoResults1 = geoResults.getContent();
            for (GeoResult<RedisGeoCommands.GeoLocation<Object>> item : geoResults) {
                GeoBean geoBean = new GeoBean();
                geoBean.setDis(item.getDistance().getValue());
                geoBean.setKey(item.getContent().getName().toString());
                geoBeanList.add(geoBean);
            }
            //将json存到数据库埋在去
            geojson = JSON.toJSONString(geoBeanList);
        }

        if (imUserGeoData2 == null || imUserGeoData2.getUid() <= 0) {
            IMUserGeoData imUserGeoData = new IMUserGeoData();
            imUserGeoData.setId(null);
            imUserGeoData.setUid(myinfo.getId());
            imUserGeoData.setData(geojson);
            imUserGeoData.setStatus(1);
            imUserGeoData.setLat(lat);
            imUserGeoData.setLng(lng);

            imUserGeoData.setUpdated(controllerUtil.timestamp());
            iimUserGeoDataService.save(imUserGeoData);
        }

        pageGeoList = javaBeanUtil.sublist(geoBeanList, page, pagesize);
        List<String> userids = new LinkedList<>();
        for (GeoBean geoBean : pageGeoList) {
            if (!geoBean.getKey().equals(myinfo.getId())) {//把自已排除
                userids.add(geoBean.getKey());
            }
        }

        String uids = StringUtils.join(userids, ",");
        List<Map<String, Object>> userslist = iimUserService.getUsersInfo(uids);


        //哈哈。连环for。主要是为了排序和输出dists
        List<Map<String, Object>> returndatalist = new LinkedList<>();
        for (GeoBean geoBean : pageGeoList) {
            for (Map<String, Object> map : userslist) {
                if (geoBean.getKey().equals(map.get("id").toString())) {
                    map.put("dists", geoBean.getDis());
                    returndatalist.add(map);
                    break;
                }
            }
        }

        returnResult.setCode(ApiResult.SUCCESS);
        returnResult.setData(returndatalist);
        returnResult.setMessage("查询成功!");
        return returnResult;
    }

    @RequestMapping(value = "reg", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
    public ApiResult reg(HttpServletRequest req, HttpServletResponse rsp) {

        ApiResult returnResult = new ApiResult();
        Map<String, Object> returnData = new HashMap<>();
        ServerInfoEntity serverinfo = new ServerInfoEntity();
        Map<String, Object> bmqq_plugin = new HashMap<>();

        int appid = controllerUtil.getIntParameter(req, "appId", 88888);
        int outid = controllerUtil.getIntParameter(req, "outid", 0);
        String username = controllerUtil.getStringParameter(req, "username", "0");
        String password = controllerUtil.getStringParameter(req, "password", "0");
        String code = controllerUtil.getStringParameter(req, "code", "0");
        String nickname = controllerUtil.getStringParameter(req, "nickname", "");

        String tjcode = controllerUtil.getStringParameter(req, "tjcode", "");

        int salt = new Random().nextInt(8888) + 1000;

//        if (username.length() != 11) {
//            returnResult.setCode(ApiResult.ERROR);
//            returnResult.setData(returnData);
//            returnResult.setMessage("请使用正确的手机号码!");
//            return returnResult;
//        }

        String vcode = null;
        try {
        //    vcode = redisHelper.get("code_" + username).toString();
        } catch (Exception ee) {
        }

//        if (vcode == null || vcode == "") {
////            returnResult.setCode(ApiResult.ERROR);
////            returnResult.setData(returnData);
////            returnResult.setMessage("请先获取验证码!");
////            return returnResult;
////        } else {
////            if (!code.toLowerCase().equals(vcode.toLowerCase())) {
////                returnResult.setCode(ApiResult.ERROR);
////                returnResult.setData(returnData);
////                returnResult.setMessage("验证码不匹配!");
////                return returnResult;
////            }
////        }

        IMUser users = iimUserService.getOne(new QueryWrapper<IMUser>().eq("appId", appid).eq("username", username));
        if (users != null && users.getId() > 0) {
            returnResult.setCode(ApiResult.ERROR);
            returnResult.setData(returnData);
            returnResult.setMessage("账号已存在!");
            return returnResult;
        }

        users = new IMUser();
        users.setAppId(appid);
        users.setAvatar("http://webim.b56.cn/images/avatar_2754583.png");
        users.setOutId(outid);
        users.setUsername(username);
        users.setSalt(salt + "");
        //对密码进行加密
        String enPass = DigestUtils.md5Hex(password + users.getSalt()).toLowerCase();
        users.setPassword(enPass);
        if (nickname.equals("")) {
            nickname = "cloudtalk" + salt;
        }
        users.setNickname(nickname);
        users.setRealname(nickname);
        users.setApiToken(controllerUtil.getRandomString(32));
        users.setCreated(controllerUtil.timestamp2());
        users.setUpdated(controllerUtil.timestamp2());
        users.setSex(1);
        users.setDomain("0");
        users.setPhone(username);
        users.setDepartId(1);
        users.setStatus(0);
        iimUserService.save(users);

        users.setCode(users.getId()+"");
        if(!tjcode.equals("")) {
            IMUser users2 = iimUserService.getOne(new QueryWrapper<IMUser>().eq("code", tjcode));
            if(users2!=null && users2.getId()>0) {
                users.setTopuid(users2.getId());

                //给对方加上自已的好友记录
                IMUserFriends addFriend = new IMUserFriends();
                addFriend.setUid(users.getId());
                addFriend.setFriuid(users2.getId());
                addFriend.setFriName(users2.getNickname());
                addFriend.setGroupId(1);
                addFriend.setMessage("已通过好友请求!");
                addFriend.setStatus(1);
                addFriend.setUpdated(controllerUtil.timestamp2());
                addFriend.setCreated(controllerUtil.timestamp2());
                iimUserFriendsService.save(addFriend);

                IMUserFriends addFriend2 = new IMUserFriends();
                addFriend2.setUid(users2.getId());
                addFriend2.setFriuid(users.getId());
                addFriend2.setFriName(users.getNickname());
                addFriend2.setGroupId(1);
                addFriend2.setMessage("已通过好友请求!");
                addFriend2.setStatus(1);
                addFriend2.setUpdated(controllerUtil.timestamp2());
                addFriend2.setCreated(controllerUtil.timestamp2());
                iimUserFriendsService.save(addFriend2);



            }
        }

        iimUserService.updateById(users);

        //初始化在线用户表
        initUserOnline(users.getId());

        returnResult.setCode(ApiResult.SUCCESS);
        returnResult.setData(null);
        returnResult.setMessage("注册成功!");
        return returnResult;
    }

    @RequestMapping(value = "checkLogin", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
    public ApiResult checkLogin(HttpServletRequest req, HttpServletResponse rsp) {
        //
        ApiResult returnResult = new ApiResult();
        Map<String, Object> returnData = new HashMap<>();
        ServerInfoEntity serverinfo = new ServerInfoEntity();
        Map<String, Object> bmqq_plugin = new HashMap<>();

        String appid = req.getParameter("appId");
        String username = req.getParameter("username");
        String password = req.getParameter("password");

        IMUser users = iimUserService.getOne(new QueryWrapper<IMUser>().eq("appId", appid).eq("username", username));
        if (users == null || users.getId() == 0) {
            returnResult.setCode(ApiResult.ERROR);
            returnResult.setData(returnData);
            returnResult.setMessage("账号不存在!");
            return returnResult;
        }

        password = DigestUtils.md5Hex(password + users.getSalt()).toLowerCase();
        if (!users.getPassword().equals(password)) {
            returnResult.setCode(ApiResult.ERROR);
            returnResult.setData(returnData);
            returnResult.setMessage("密码错误!");
            return returnResult;
        }

        //*********从redis中获取 负载量小的 聊天服务器************
        //****************************************************
        Map<Object, Object> serverlistmap = new HashMap<>();
        String selectServerInfo = "";
        serverlistmap = redisHelper.hmget("msg_srv_list");
        if (serverlistmap != null && serverlistmap.size() > 0) {
            serverlistmap = javaBeanUtil.sortMapByValue(serverlistmap);
            selectServerInfo = javaBeanUtil.getFirstKeyFromMap(serverlistmap).toString();
            serverinfo.setServer_ip(selectServerInfo.split("\\|")[0]);
            serverinfo.setServer_ip2(selectServerInfo.split("\\|")[1]);
            serverinfo.setServer_port(Integer.parseInt(selectServerInfo.split("\\|")[2]));
            serverinfo.setMsfsPrior(files_msfsprior);
            serverinfo.setMsfsBackup(files_msfspriorbackup);
        }

        bmqq_plugin.put("appid", bqmmplugin_appid);
        bmqq_plugin.put("appsecret", bqmmplugin_appsecret);


        Map<String, Object> returnUsers = JavaBeanUtil.convertBeanToMap(users);
        returnUsers.put("peerId", users.getId());
        returnUsers.remove("password");

        returnData.put("token", users.getApiToken());
        returnData.put("userinfo", returnUsers);
        returnData.put("serverinfo", serverinfo);
        returnData.put("bqmmplugin", bmqq_plugin);
        iimUserService.updateById(users);

        //初始化在线用户表
        initUserOnline(users.getId());

        returnResult.setCode(ApiResult.SUCCESS);
        returnResult.setData(returnData);
        returnResult.setMessage("登录成功!");

        return returnResult;
    }

    @RequestMapping(value = "editPWD", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
    public ApiResult editPWD(HttpServletRequest req, HttpServletResponse rsp) {

        ApiResult returnResult = new ApiResult();
        Map<String, Object> returnData = new HashMap<>();
        IMUser myinfo = controllerUtil.checkToken(req);
        if (myinfo == null) {
            returnResult.setCode(ApiResult.ERROR);
            returnResult.setData(returnData);
            returnResult.setMessage("token验证失败!");
            return returnResult;
        }

        String oldPWD = req.getParameter("oldPWD");
        String newPWD = req.getParameter("newPWD");

        oldPWD = DigestUtils.md5Hex(oldPWD + myinfo.getSalt()).toLowerCase();
        if (oldPWD.equals(myinfo.getPassword())) {
            newPWD = DigestUtils.md5Hex(newPWD + myinfo.getSalt()).toLowerCase();
            myinfo.setPassword(newPWD);
            iimUserService.update(myinfo, new QueryWrapper<IMUser>().eq("id", myinfo.getId()));

            returnResult.setCode(ApiResult.SUCCESS);
            returnResult.setData(returnData);
            returnResult.setMessage("密码修改成功!");
            return returnResult;
        } else {
            returnResult.setCode(ApiResult.ERROR);
            returnResult.setData(returnData);
            returnResult.setMessage("旧密码验证失败!");
            return returnResult;
        }
    }

    @RequestMapping(value = "getUserInfo", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
    public ApiResult getUserInfo(HttpServletRequest req, HttpServletResponse rsp) {

        ApiResult returnResult = new ApiResult();
        Map<String, Object> returnData = new HashMap<>();

        IMUser myinfo = controllerUtil.checkToken(req);
        if (myinfo == null) {
            returnResult.setCode(ApiResult.ERROR);
            returnResult.setData(returnData);
            returnResult.setMessage("token验证失败!");
            return returnResult;
        }


        String friuid = req.getParameter("friuids");
        List<Map<String, Object>> userslist = iimUserService.getUsersInfo(friuid);

        if (userslist.size() > 0) {
            returnData.put("userinfo", userslist);

            returnResult.setCode(ApiResult.SUCCESS);
            returnResult.setData(returnData);
        } else {
            returnResult.setCode(ApiResult.ERROR);
        }
        returnResult.setMessage("查询成功!");
        return returnResult;
    }


    @RequestMapping(value = "getUserInfoByUserName", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
    public ApiResult getUserInfoByUserName(HttpServletRequest req, HttpServletResponse rsp) {

        ApiResult returnResult = new ApiResult();
        Map<String, Object> returnData = new HashMap<>();

        IMUser myinfo = controllerUtil.checkToken(req);
        if (myinfo == null) {
            returnResult.setCode(ApiResult.ERROR);
            returnResult.setData(returnData);
            returnResult.setMessage("token验证失败!");
            return returnResult;
        }

        String username = req.getParameter("username");
        IMUser users = iimUserService.getOne(new QueryWrapper<IMUser>().eq("appId", myinfo.getAppId()).eq("username", username).or().eq("id",username));

        List<Map<String, Object>> userslist = iimUserService.getUsersInfo(users.getId().toString());
        if (userslist.size() > 0) {
            returnData.put("userinfo", userslist);

            returnResult.setCode(ApiResult.SUCCESS);
            returnResult.setData(returnData);
        } else {
            returnResult.setCode(ApiResult.ERROR);
        }
        returnResult.setMessage("查询成功!");
        return returnResult;
    }

    @RequestMapping(value = "getSrvInfo", method = RequestMethod.GET, produces = "application/json;charset=UTF-8")
    public ApiResult getSrvInfo(HttpServletRequest req, HttpServletResponse rsp) {

        ApiResult returnResult = new ApiResult();
        Map<String, Object> returnData = new HashMap<>();
        ServerInfoEntity serverinfo = new ServerInfoEntity();

        //*********从redis中获取 负载量小的 聊天服务器************
        //***************************************************
        Map<Object, Object> serverlistmap = new HashMap<>();
        String selectServerInfo = "";
        serverlistmap = redisHelper.hmget("msg_srv_list");
        if (serverlistmap != null && serverlistmap.size() > 0) {
            serverlistmap = javaBeanUtil.sortMapByValue(serverlistmap);
            selectServerInfo = javaBeanUtil.getFirstKeyFromMap(serverlistmap).toString();
            serverinfo.setServer_ip(selectServerInfo.split("\\|")[0]);
            serverinfo.setServer_ip2(selectServerInfo.split("\\|")[1]);
            serverinfo.setServer_port(Integer.parseInt(selectServerInfo.split("\\|")[2]));
        }

        serverinfo.setMsfsPrior(files_msfsprior);
        serverinfo.setMsfsBackup(files_msfspriorbackup);
        returnResult.setCode(ApiResult.SUCCESS);
        returnResult.setData(serverinfo);
        returnResult.setMessage("登录成功!");

        return returnResult;
    }

    @RequestMapping(value = "pubRoBotMessage", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
    public ApiResult pubRoBotMessage(HttpServletRequest req, HttpServletResponse rsp) {
        ApiResult returnResult = new ApiResult();
        Map<String, Object> returnData = new HashMap<>();

        String msgcontent = controllerUtil.getStringParameter(req, "msgcontent", "");
        redPackeUtil.robotAction(msgcontent);

        returnResult.setCode(ApiResult.SUCCESS);
        returnResult.setMessage("登录成功!");

        return returnResult;
    }

    @RequestMapping("/users/{page}/{size}")
    public Map<String, Object> users(@PathVariable Integer page, @PathVariable Integer size) {
        Map<String, Object> map = new HashMap<>();
        Page<IMUser> questionStudent = iimUserService.getAllUserBypage(new Page<>(page, size));

        IMUser users = iimUserService.getById(1);

        List<Map<String, Object>> list = iimUserService.selectUser2();

        if (questionStudent.getRecords().size() == 0) {
            map.put("code", 400);
        } else {
            map.put("code", 200);
            map.put("data", questionStudent);
        }
        return map;
    }

    @RequestMapping(value = "setUserInfo", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
    public ApiResult setUserInfo(HttpServletRequest req, HttpServletResponse rsp) {

        ApiResult returnResult = new ApiResult();
        Map<String, Object> returnData = new HashMap<>();

        IMUser myinfo = controllerUtil.checkToken(req);
        if (myinfo == null) {
            returnResult.setCode(ApiResult.ERROR);
            returnResult.setData(returnData);
            returnResult.setMessage("token验证失败!");
            return returnResult;
        }

        String avatar= controllerUtil.getStringParameter(req, "avatar", "");
        String nickname= controllerUtil.getStringParameter(req, "nickname", "");
        String sign_info= controllerUtil.getStringParameter(req, "sign_info", "");
        int sex= controllerUtil.getIntParameter(req, "sex", 0);

        if(!avatar.equals(""))
        {
            myinfo.setAvatar(avatar);
            myinfo.setUpdated(controllerUtil.timestamp2());
        }
        if(!nickname.equals(""))
        {
            myinfo.setNickname(nickname);
            myinfo.setUpdated(controllerUtil.timestamp2());
        }
        if(!sign_info.equals(""))
        {
            myinfo.setSignInfo(sign_info);
            myinfo.setUpdated(controllerUtil.timestamp2());
        }

        if(sex>0)
        {
            myinfo.setSex(sex);
            myinfo.setUpdated(controllerUtil.timestamp2());
        }
        iimUserService.updateById(myinfo);

        returnResult.setCode(ApiResult.SUCCESS);
        returnResult.setData(null);
        returnResult.setMessage("修改成功!");
        return  returnResult;
    }
        /**
         * 忘记密码
         *
         * @param req
         * @param rsp
         * @return
         */
    @PostMapping("/forgetPsw")
    public Map<String, Object> forgetPsw(HttpServletRequest req, HttpServletResponse rsp) {
        Map result = new HashMap();
        String passWord = req.getParameter("password");
        if (passWord == null) {
            result.put("code", "100");
            result.put("msg", "密码长度不符");
            return result;
        }
        String yzm = req.getParameter("yzm");
        String phone = req.getParameter("phone");
        int appid = controllerUtil.getIntParameter(req, "appId", 88888);
        Object code = redisHelper.get("code_" + phone);
        if (code == null) {
            result.put("code", "100");
            result.put("msg", "验证码超时");
            return result;
        }
        if (code.toString().equals(yzm)) {
            IMUser users = iimUserService.getOne(new QueryWrapper<IMUser>().eq("appId", appid).eq("phone", phone));
            if (users == null) {
                result.put("msg", "此用户不存在");
                result.put("code", "100");
                return result;
            }
            //对密码进行加密
            String enPass = DigestUtils.md5Hex(passWord + users.getSalt()).toLowerCase();
            users.setPassword(enPass);
            iimUserService.updateById(users);
            result.put("code", 200);
            result.put("data", "");
        } else {
            result.put("code", 100);
            result.put("data", "");
            result.put("msg", "验证码错误");
        }

        return result;
    }

    @GetMapping("/test")
    public Object test() {
        redisHelper.set("chenanxinest", "oooooooooo", 120);
        return redisHelper.get("chenanxinest");
    }


}
