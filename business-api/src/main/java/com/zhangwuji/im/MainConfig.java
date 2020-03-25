package com.zhangwuji.im;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.zhangwuji.im.api.common.RedPackeUtil;
import com.zhangwuji.im.api.entity.GetRedPacketListEntity;
import com.zhangwuji.im.api.entity.IMRobot;
import com.zhangwuji.im.api.entity.Luckymoney;
import com.zhangwuji.im.api.entity.LuckymoneyLog;
import com.zhangwuji.im.api.service.IIMRobotService;
import com.zhangwuji.im.api.service.IIMUserFriendsService;
import com.zhangwuji.im.api.service.impl.IMRobotServiceImpl;
import com.zhangwuji.im.api.service.impl.LuckymoneyLogServiceImpl;
import com.zhangwuji.im.api.service.impl.LuckymoneyServiceImpl;
import lombok.Synchronized;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.*;

@Component
public class MainConfig {

    @Resource
    private IMRobotServiceImpl imRobotService;

    @Autowired
    LuckymoneyServiceImpl luckymoneyService;

    @Autowired
    LuckymoneyLogServiceImpl luckymoneyLogService;


    public  HashMap<String,Object> robots=new HashMap<String,Object>();

    public  List<GetRedPacketListEntity> autoGetRedPacket=new LinkedList<>();

    @Resource
    RedPackeUtil redPackeUtil;

    public void initGetAutoRedPacket()
    {
        if(autoGetRedPacket.size()>0) {
            GetRedPacketListEntity getRedPacketListEntity = autoGetRedPacket.get(0);
            if (getRedPacketListEntity != null) {
                autoGetRedPacket.remove(0);
            }
            synchronized(this) {
                getRedPacketFun(getRedPacketListEntity.getUid(), getRedPacketListEntity.getRedpacketId());
            }
        }

    }

    public  void getRedPacketFun(int toid,int pid)
    {
        Luckymoney luckymoney = luckymoneyService.getById(pid);
        if (luckymoney != null) {
            LuckymoneyLog luckymoneyLog = luckymoneyLogService.getOne(new QueryWrapper<LuckymoneyLog>().eq("pid", pid).eq("uid", toid));
            if (luckymoneyLog == null || luckymoneyLog.getUid() == 0) {
                int haveNum = luckymoney.getAllnum() - luckymoney.getUsenum();
                //这里开始抢红包了。
                for (int ii = 0; ii < haveNum; ii++) {

                    try {
                        Thread.sleep(new Random().nextInt(300) + 200);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    LuckymoneyLog myluckymoneyLog = luckymoneyLogService.getOne(new QueryWrapper<LuckymoneyLog>().eq("pid", pid).eq("uid", toid));
                    if (myluckymoneyLog != null && myluckymoneyLog.getUid() > 0) {
                        return;
                    }
                    //有值说明可能还抢得到红包
                    Map<String, Object> redmap = luckymoneyLogService.getLuckMoneyLog(pid);
                    if (redmap != null) {
                        myluckymoneyLog = luckymoneyLogService.getOne(new QueryWrapper<LuckymoneyLog>().eq("pid", pid).eq("uid", toid));
                        if (myluckymoneyLog != null && myluckymoneyLog.getUid() > 0) {
                            return;
                        }

                        //抢红包的两条关健语句
                        int res = luckymoneyLogService.setLuckMoneyLog(toid, Integer.parseInt(redmap.get("id").toString()));
                        LuckymoneyLog luckymoneyLog2 = luckymoneyLogService.getOne(new QueryWrapper<LuckymoneyLog>().eq("pid", pid).eq("uid", toid));
                        if (luckymoneyLog2 != null && luckymoneyLog2.getId() > 0) {
                            //更新红包记录。将已领的数量加上
                            luckymoneyService.setLuckMoneyInfo(luckymoneyLog2.getMoney(), luckymoneyLog2.getPid());
                            return;
                        }
                    }
                }
            }
        }
    }

    public void initRobotList()
    {
        robots.clear();
       List<IMRobot> robotList=imRobotService.list(new QueryWrapper<IMRobot>().eq("status", 1));
       for (IMRobot imRobot:robotList)
       {
           robots.put(imRobot.getUid()+"-"+imRobot.getGroupid(),imRobot);
       }

    }

}
