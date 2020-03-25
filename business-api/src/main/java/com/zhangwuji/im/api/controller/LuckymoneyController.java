package com.zhangwuji.im.api.controller;


import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.zhangwuji.im.MainConfig;
import com.zhangwuji.im.api.common.ControllerUtil;
import com.zhangwuji.im.api.common.JavaBeanUtil;
import com.zhangwuji.im.api.common.RedPackeUtil;
import com.zhangwuji.im.api.entity.*;
import com.zhangwuji.im.api.result.ApiResult;
import com.zhangwuji.im.api.service.IIMUserService;
import com.zhangwuji.im.api.service.ILuckymoneyService;
import com.zhangwuji.im.api.service.impl.LuckymoneyLogServiceImpl;
import com.zhangwuji.im.api.service.impl.LuckymoneyServiceImpl;
import com.zhangwuji.im.api.service.impl.UserAccountServiceImpl;
import com.zhangwuji.im.api.service.impl.UserpaylogServiceImpl;
import com.zhangwuji.im.config.RedisCacheHelper;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.annotation.Transient;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author cloudtalk
 * @since 2019-03-23
 */
@RestController
@RequestMapping("/api/luckymoney")
@JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
public class LuckymoneyController {

    @Autowired
    MainConfig mainConfig;

    @Resource
    ControllerUtil controllerUtil;

    @Resource
    RedPackeUtil redPackeUtil;

    @Autowired
    LuckymoneyServiceImpl luckymoneyService;

    @Autowired
    LuckymoneyLogServiceImpl luckymoneyLogService;

    @Autowired
    UserAccountServiceImpl userAccountService;

    @Autowired
    UserpaylogServiceImpl userpaylogService;

    @Resource
    @Qualifier(value = "imUserService")
    private IIMUserService iimUserService;


    @Transactional
    @RequestMapping(value = "sendRedPacket", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
    public ApiResult sendRedPacket(HttpServletRequest req, HttpServletResponse rsp) {

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
        String paypwd = controllerUtil.getStringParameter(req, "paypwd", "");
        int type = controllerUtil.getIntParameter(req, "type", 0);
        int type2 = controllerUtil.getIntParameter(req, "type2", 0);
        double allmoney = controllerUtil.getDoubleParameter(req, "allmoney", 0);
        int allnum = controllerUtil.getIntParameter(req, "allnum", 0);
        int groupId = controllerUtil.getIntParameter(req, "groupId", 0);
        String msg = controllerUtil.getStringParameter(req, "msg", "");


        UserAccount userAccount=userAccountService.getOne(new QueryWrapper<UserAccount>().eq("uid", myinfo.getId()));
        if(userAccount==null || userAccount.getUid()<=0)
        {
            returnResult.setCode(201);
            returnResult.setData(null);
            returnResult.setMessage("没有设置支付密码!");
            return returnResult;
        }
        else
        {
            String md5pwd= DigestUtils.md5Hex(paypwd + myinfo.getSalt()).toLowerCase();
            if(!userAccount.getPayPassword().equals(md5pwd))
            {
                returnResult.setCode(202);
                returnResult.setData(null);
                returnResult.setMessage("支付密码验证失败!");
                return returnResult;
            }
            else if(userAccount.getAvailableMoney()<allmoney)
            {
                returnResult.setCode(203);
                returnResult.setData(null);
                returnResult.setMessage("余额不足!");
                return returnResult;
            }
            else
            {
                userAccount.setAvailableMoney(userAccount.getAvailableMoney()-allmoney);
            }
        }

        Luckymoney luckymoney = new Luckymoney();
        luckymoney.setAddtime(LocalDateTime.now());
        luckymoney.setSenduid(myinfo.getId());
        luckymoney.setType(type);
        luckymoney.setType2(type2);
        luckymoney.setAllmoney(allmoney);
        if (type2 == 1) {
            luckymoney.setAllnum(1);
        } else {
            luckymoney.setAllnum(allnum);
        }
        luckymoney.setUsemoney(0);
        luckymoney.setUsenum(0);
        luckymoney.setGroupId(groupId);
        luckymoney.setStatus(1);
        luckymoney.setLv(0);
        luckymoney.setMsg(msg);
        luckymoneyService.save(luckymoney);
        int pid = luckymoney.getId();

        List<Double> alllist = redPackeUtil.splitRedPackets(msg,groupId,(int)(allmoney * 100), luckymoney.getAllnum());
        for (Double ii : alllist) {
            LuckymoneyLog luckymoneyLog = new LuckymoneyLog();
            luckymoneyLog.setAddtime(LocalDateTime.now());
            luckymoneyLog.setFlag(0);
            luckymoneyLog.setFuid(0);
            luckymoneyLog.setLv(0);
            luckymoneyLog.setPid(pid);
            luckymoneyLog.setMoney(ii / 100);
            luckymoneyLog.setStatus(0);
            luckymoneyLog.setUid(0);
            luckymoneyLog.setType(0);
            luckymoneyLogService.save(luckymoneyLog);
        }

        LuckymoneyLog luckymoneyLog1 = luckymoneyLogService.getOne(new QueryWrapper<LuckymoneyLog>().eq("pid", pid).orderByDesc("money"));
        luckymoneyLog1.setLv(1);
        luckymoneyLogService.updateById(luckymoneyLog1);



        userAccount.setUpdatetime(LocalDateTime.now());
        userAccountService.update(userAccount,new QueryWrapper<UserAccount>().eq("uid", myinfo.getId()));//更新账户余额

        Userpaylog userpaylog=new Userpaylog();
        userpaylog.setOrderno("红包ID:"+pid);
        userpaylog.setPaymoney(BigDecimal.valueOf(allmoney));
        userpaylog.setAllmoney(BigDecimal.valueOf(userAccount.getAvailableMoney()));
        userpaylog.setLv(0);
        userpaylog.setPaytouid("0");
        userpaylog.setPaymsg("发送红包");
        userpaylog.setPaytype(2);//2为支出
        userpaylog.setPaytime(LocalDateTime.now());
        userpaylog.setStatus(1);
        userpaylog.setUid(myinfo.getId()+"");
        userpaylogService.save(userpaylog);

        returnData.put("pid", luckymoney.getId());
        returnResult.setCode(ApiResult.SUCCESS);
        returnResult.setData(returnData);
        returnResult.setMessage("发布成功!");
        return returnResult;
    }



    @RequestMapping(value = "getRedPacket", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
    public ApiResult getRedPacket(HttpServletRequest req, HttpServletResponse rsp) {

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

        int pid = controllerUtil.getIntParameter(req, "pid", 0);

        //随机休息几毫秒秒喽
        try {
            Thread.sleep(new Random().nextInt(1000) + 300);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        UserAccount userAccount=userAccountService.getOne(new QueryWrapper<UserAccount>().eq("uid", myinfo.getId()));
        if(userAccount==null || userAccount.getUid()<=0)
        {
            returnResult.setCode(201);
            returnResult.setData(null);
            returnResult.setMessage("没有设置支付密码!");
            return returnResult;
        }

        Luckymoney luckymoney = luckymoneyService.getById(pid);
        if (luckymoney == null || luckymoney.getId() == 0) {
            returnResult.setCode(101);
            returnResult.setData(null);
            returnResult.setMessage("没有该红包!");
            return returnResult;
        } else if (luckymoney.getAllnum() <= luckymoney.getUsenum()) {
            returnResult.setCode(102);
            returnResult.setData(null);
            returnResult.setMessage("来晚了,红包已抢完了!");
            return returnResult;
        }

        synchronized(this) {
            //先看看红包有没有领取过
            LuckymoneyLog luckymoneyLog = luckymoneyLogService.getOne(new QueryWrapper<LuckymoneyLog>().eq("pid", pid).eq("uid", myinfo.getId()));
            if (luckymoneyLog != null && luckymoneyLog.getUid() > 0) {
                returnResult.setCode(103);
                returnResult.setData(null);
                returnResult.setMessage("已领取过红包!");
                return returnResult;
            } else {
                int haveNum = luckymoney.getAllnum() - luckymoney.getUsenum();
                //这里开始抢红包了。
                for (int ii = 0; ii < haveNum; ii++) {
                    //有值说明可能还抢得到红包
                    Map<String, Object> redmap = luckymoneyLogService.getLuckMoneyLog(pid);
                    if (redmap == null || redmap.size() < 1) {
                        break;
                    } else {

                        //抢红包的两条关健语句
                        int res = luckymoneyLogService.setLuckMoneyLog(myinfo.getId(), Integer.parseInt(redmap.get("id").toString()));
                        try {
                            Thread.sleep(new Random().nextInt(500));
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        LuckymoneyLog luckymoneyLog2 = luckymoneyLogService.getOne(new QueryWrapper<LuckymoneyLog>().eq("pid", pid).eq("uid", myinfo.getId()));
                        if (luckymoneyLog2 != null && luckymoneyLog2.getId() > 0) {
                            //更新红包记录。将已领的数量加上
                            luckymoneyService.setLuckMoneyInfo(luckymoneyLog2.getMoney(), luckymoneyLog2.getPid());

                            luckymoneyLog2.setAddtime(LocalDateTime.now());
                            luckymoneyLogService.saveOrUpdate(luckymoneyLog2);

                            userAccount.setUpdatetime(LocalDateTime.now());
                            userAccount.setAvailableMoney(userAccount.getAvailableMoney() + luckymoneyLog2.getMoney());
                            userAccountService.update(userAccount, new QueryWrapper<UserAccount>().eq("uid", myinfo.getId()));

                            Userpaylog userpaylog = new Userpaylog();
                            userpaylog.setOrderno("红包ID:" + pid);
                            userpaylog.setPaymoney(BigDecimal.valueOf(luckymoneyLog2.getMoney()));
                            userpaylog.setAllmoney(BigDecimal.valueOf(userAccount.getAvailableMoney()));
                            userpaylog.setLv(0);
                            userpaylog.setPaytouid("0");
                            userpaylog.setPaymsg("领取红包");
                            userpaylog.setPaytype(1);//2为支出  1为进账
                            userpaylog.setPaytime(LocalDateTime.now());
                            userpaylog.setStatus(1);
                            userpaylog.setUid(myinfo.getId() + "");
                            userpaylogService.save(userpaylog);


                            returnResult.setCode(ApiResult.SUCCESS);
                            returnResult.setData(luckymoneyLog2); //抢到的红包记录
                            returnResult.setMessage("成功抢到红包!");
                            return returnResult;
                        } else {
                            //这都没更新到。缓一下
                            try {
                                Thread.sleep(new Random().nextInt(500) + 100);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }
        }

        returnResult.setCode(102);
        returnResult.setData(returnData);
        returnResult.setMessage("来晚了,红包已抢完了!");
        return returnResult;

    }

    @RequestMapping(value = "checkRedPacket", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
    public ApiResult checkRedPacket(HttpServletRequest req, HttpServletResponse rsp) {

        ApiResult returnResult = new ApiResult();
        Map<String, Object> returnData = new HashMap<>();

        IMUser myinfo = controllerUtil.checkToken(req);
        if (myinfo == null) {
            returnResult.setCode(ApiResult.ERROR);
            returnResult.setData(returnData);
            returnResult.setMessage("token验证失败!");
            return returnResult;
        }

        int pid = controllerUtil.getIntParameter(req, "pid", 0);

        Luckymoney luckymoney = luckymoneyService.getById(pid);
        if (luckymoney == null || luckymoney.getId() == 0) {
            returnResult.setCode(101);
            returnResult.setData(null);
            returnResult.setMessage("没有该红包!");
            return returnResult;
        } else {
            LuckymoneyLog luckymoneyLog = luckymoneyLogService.getOne(new QueryWrapper<LuckymoneyLog>().eq("pid", pid).eq("uid", myinfo.getId()));
            if (luckymoneyLog != null && luckymoneyLog.getUid() > 0) {
                returnResult.setCode(103);
                returnResult.setData(luckymoneyLog);
                returnResult.setMessage("已领取过红包!");
                return returnResult;
            } else if (luckymoney.getAllnum() <= luckymoney.getUsenum()) {
                returnResult.setCode(102);
                returnResult.setData(null);
                returnResult.setMessage("来晚了,红包已抢完了!");
                return returnResult;
            } else {
                returnResult.setCode(100);
                returnResult.setData(null);
                returnResult.setMessage("红包还有可以领!");
                return returnResult;
            }
        }
    }

    @RequestMapping(value = "getRedPacketLog", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    public ApiResult getRedPacketLog(HttpServletRequest req, HttpServletResponse rsp) {
        ApiResult returnResult = new ApiResult();
        Map<String, Object> returnData = new HashMap<>();

        IMUser myinfo = controllerUtil.checkToken(req);
        if (myinfo == null) {
            returnResult.setCode(ApiResult.ERROR);
            returnResult.setData(returnData);
            returnResult.setMessage("token验证失败!");
            return returnResult;
        }
        int pid = controllerUtil.getIntParameter(req, "pid", 0);
        int page = controllerUtil.getIntParameter(req, "page", 1);
        int pagesize = controllerUtil.getIntParameter(req, "pagesize", 200);

        Luckymoney luckymoney = luckymoneyService.getById(pid);
        if (luckymoney == null || luckymoney.getId() == 0) {
            returnResult.setCode(101);
            returnResult.setData(null);
            returnResult.setMessage("没有该红包!");
            return returnResult;
        }

        Page<Map<String, Object>> pagelist = luckymoneyLogService.getLuckymoneyLog(new Page<>(page, pagesize), pid);

        List<Map<String, Object>> list = pagelist.getRecords();

        returnResult.setCode(ApiResult.SUCCESS);
        returnResult.setData(list);
        returnResult.setMessage("查询成功!");
        return returnResult;

    }

    @RequestMapping(value = "getRedPacketInfo", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
    public ApiResult getRedPacketInfo(HttpServletRequest req, HttpServletResponse rsp) {
        ApiResult returnResult = new ApiResult();
        Map<String, Object> returnData = new HashMap<>();

        IMUser myinfo = controllerUtil.checkToken(req);
        if (myinfo == null) {
            returnResult.setCode(ApiResult.ERROR);
            returnResult.setData(returnData);
            returnResult.setMessage("token验证失败!");
            return returnResult;
        }
        int pid = controllerUtil.getIntParameter(req, "pid", 0);
        Luckymoney luckymoney = luckymoneyService.getById(pid);
        if (luckymoney == null || luckymoney.getId() == 0) {
            returnResult.setCode(101);
            returnResult.setData(null);
            returnResult.setMessage("没有该红包!");
            return returnResult;
        }

        IMUser user=iimUserService.getById(luckymoney.getSenduid());

        returnData.put("redpacketinfo",luckymoney);
        returnData.put("userinfo",user);
        returnResult.setCode(ApiResult.SUCCESS);
        returnResult.setData(returnData);
        returnResult.setMessage("查询成功!");
        return returnResult;

    }

    @RequestMapping(value = "getMyRedPacketList", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
    public ApiResult getMyRedPacketList(HttpServletRequest req, HttpServletResponse rsp) {
        ApiResult returnResult = new ApiResult();
        Map<String, Object> returnData = new HashMap<>();

        IMUser myinfo = controllerUtil.checkToken(req);
        if (myinfo == null) {
            returnResult.setCode(ApiResult.ERROR);
            returnResult.setData(returnData);
            returnResult.setMessage("token验证失败!");
            return returnResult;
        }

        int page = controllerUtil.getIntParameter(req, "page", 1);
        int pagesize = controllerUtil.getIntParameter(req, "pagesize", 50);

        Page<Map<String, Object>> pagelist = luckymoneyLogService.getMyLuckymoneyLog(new Page<>(page, pagesize), myinfo.getId());
        List<Map<String, Object>> list = pagelist.getRecords();

        returnResult.setCode(ApiResult.SUCCESS);
        returnResult.setData(list);
        returnResult.setMessage("查询成功!");
        return returnResult;

    }
}