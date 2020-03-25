package com.zhangwuji.im.api.common;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.zhangwuji.im.MainConfig;
import com.zhangwuji.im.api.entity.*;
import com.zhangwuji.im.api.result.ApiResult;
import com.zhangwuji.im.api.service.IIMUserService;
import com.zhangwuji.im.api.service.impl.ImgroupExpandServiceImpl;
import com.zhangwuji.im.api.service.impl.LuckymoneyLogServiceImpl;
import com.zhangwuji.im.api.service.impl.LuckymoneyServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.rmi.ServerException;
import java.util.*;

@Component
public class RedPackeUtil {


    @Value("${cloudtalk.tongji.url}")
    public  String cloudTalkTongJiUrl;
    /**
     * 1.总金额不能超过200*100 单位是分
     * 2.每个红包都要有钱，最低不能低于1分，最大金额不能超过200*100
     */
    private static final int MINMONEY = 1;
    private static final int MAXMONEY = 200 * 100;
    /**
     * 这里为了避免某一个红包占用大量资金，我们需要设定非最后一个红包的最大金额，我们把他设置为红包金额平均值的N倍；
     */
    private static final double TIMES = 2.1;

    public static HashMap<String,Object> logInfo=new HashMap<>();

    public int lastToId=0;

    @Resource
    ControllerUtil controllerUtil;

    @Autowired
    MainConfig mainConfig;

    @Autowired
    LuckymoneyServiceImpl luckymoneyService;

    @Autowired
    LuckymoneyLogServiceImpl luckymoneyLogService;

    @Autowired
    ImgroupExpandServiceImpl imgroupExpandService;

    @Resource
    @Qualifier(value = "imUserService")
    private IIMUserService iimUserService;


    private static Logger log = LoggerFactory.getLogger(RedPackeUtil.class);

    public void robotAction(String msgcontent) {
        JSONObject msg = JSON.parseObject(msgcontent);
        int toid = msg.getIntValue("toId");
        int fromid = msg.getIntValue("fromId");
        int toSessionId = msg.getIntValue("toSessionId");
        int lastMsgId2= msg.getIntValue("msgId");

        if (mainConfig.robots.containsKey(toid + "-" + toSessionId) && !logInfo.containsKey(toid+"-"+lastMsgId2)) {

            logInfo.put(toid+"-"+lastMsgId2,toSessionId);

            IMRobot imRobot = (IMRobot) mainConfig.robots.get(toid + "-" + toSessionId);
            String content = msg.getString("content");

            if (imRobot.getCtype() == 1) {
                autoReply(toid, fromid,toSessionId, msg.getIntValue("msgType"), content);
            } else if (imRobot.getCtype() == 2) {
                //没有配置或者配置的自动抢红包开关不为1时。退出。
                ImgroupExpand imgroupExpand = imgroupExpandService.getOne(new QueryWrapper<ImgroupExpand>().eq("group_Id", toSessionId));
                if (imgroupExpand == null || imgroupExpand.getAutoStatus() != 1) {
                    return;
                }
                synchronized(this) {
                    //抢红包
                    getRedPacke(toid, fromid, toSessionId, msg.getIntValue("msgType"), content);
                }
            }

            if(logInfo.size()>100)
            {
                logInfo.clear();
            }
        }

    }


    public void getRedPacke(int toid, int fromid,int toSessionId, int msgtype, String content) {
        if (content.toLowerCase().contains("cloudtalk:redpacket")) {
            JSONObject jsonObject = JSON.parseObject(content);
            int pid = jsonObject.getIntValue("id");
           // getRedPacketFun(toid,pid);

            GetRedPacketListEntity getRedPacketListEntity=new GetRedPacketListEntity();
            getRedPacketListEntity.setGid(toSessionId);
            getRedPacketListEntity.setRedpacketId(pid);
            getRedPacketListEntity.setUtype(1);
            getRedPacketListEntity.setUid(toid);

            mainConfig.autoGetRedPacket.add(getRedPacketListEntity);
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
                        Thread.sleep(new Random().nextInt(800) + 200);
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

    public void autoReply(int toid, int fromid,int toSessionId, int msgtype, String content) {
        String remsg = null;
        if(content.toLowerCase().equals("查我"))
        {
            String tongji=ControllerUtil.getApi(cloudTalkTongJiUrl+"/statistics/groupLuckyData?uid="+fromid+"&gid="+toSessionId);

            if(tongji!=null && !tongji.equals(""))
            {
                double luckyZjAmout=0;
                double luckyAmout=0;
                int luckyCount=0;
                int luckyZjCount=0;

                JSONObject jsonObject=JSON.parseObject(tongji);
                if(jsonObject.getIntValue("code")==0)
                {
                    jsonObject=jsonObject.getJSONObject("data");

                    luckyZjAmout=jsonObject.getDoubleValue("luckyZjAmount");
                    luckyAmout=jsonObject.getDoubleValue("luckyAmount");
                    luckyCount=jsonObject.getIntValue("luckyCount");
                    luckyZjCount=jsonObject.getIntValue("luckyZjCount");
                }

                IMUser imUser=iimUserService.getById(fromid);

                remsg="@"+imUser.getNickname()+"\\n";
                remsg=remsg+"发送"+luckyCount+"个包 发包总金额"+luckyAmout+"元 中奖"+luckyZjCount+"个包 中奖金额"+luckyZjAmout+"元";

            }

        }
        if (remsg != null) {
            controllerUtil.sendRoBotMessage(msgtype, toid, toSessionId, remsg);
        }
    }

    /**
     * 拆分红包
     *
     * @param money ：红包总金额
     * @param count ：个数
     * @return
     */
    public List<Double> splitRedPackets(String msg, int gid, int money, int count) {
        //红包 合法性校验
        if (!isRight(money, count,null)) {
            return null;
        }
        //没有配置或者配置的自动抢红包开关不为1时。退出。
        ImgroupExpand imgroupExpand = imgroupExpandService.getOne(new QueryWrapper<ImgroupExpand>().eq("group_Id", gid));

        //红包列表
        List<Double> list = new ArrayList<Double>();
        //每个红包最大的金额为平均金额的Times 倍
        int max = (int) (money * TIMES / count);

        max = max > MAXMONEY ? MAXMONEY : max;
        //分配红包
        for (int i = 0; i < count; i++) {
            double one = (int) randomRedPacket(msg, imgroupExpand, money, MINMONEY, max, count - i);
            list.add(one);
            money -= one;
        }
        return list;
    }

    /**
     * 随机分配一个红包
     *
     * @param money
     * @param minS  :最小金额
     * @param maxS  ：最大金额(每个红包的默认Times倍最大值)
     * @param count
     * @return
     */
    private int randomRedPacket(String msg, ImgroupExpand imgroupExpand, int money, int minS, int maxS, int count) {
        //若是只有一个，直接返回红包
        if (count == 1) {
            return money;
        }
        //若是最小金额红包 == 最大金额红包， 直接返回最小金额红包
        if (minS == maxS) {
            return minS;
        }
        List<String> mlist = new ArrayList<>();
        //校验 最大值 max 要是比money 金额高的话？ 去 money 金额
        int max = maxS > money ? money : maxS;
        //随机一个红包 = 随机一个数* (金额-最小)+最小
        int one = ((int) Math.rint(Math.random() * (max - minS) + minS));

        //是否开启限制
        if (imgroupExpand != null && imgroupExpand.getPointStatus() == 1) {
            mlist = Arrays.asList(imgroupExpand.getSecondPoint().split(","));
            //第二位小数处理。反正就是如果等于里面的值就一直减。
            if (imgroupExpand.getSecondPoint() != null && imgroupExpand.getSecondPoint().length() > 0) //个位
            {
                if (one >= 0) {
                    int sw1 = one % 10; //个位数
                    for (int ii = 0; ii < imgroupExpand.getSecondPoint().split(",").length; ii++) {
                        if (mlist.get(ii).equals(sw1 + ""))//如果==限制的数。处理。
                        {
                            int i=0;
                            while(i==0){
                                one= ((int) Math.rint(Math.random() * (max - minS) + minS));
                                sw1= one % 10;
                                if(!mlist.contains(String.valueOf(sw1))){
                                    i=1;
                                }

                            }
                        }
                    }
                }
            }
        }
        else  //开始匹配所有红包备注
        {
//            if (isNumeric(msg)) {
//                if (one >= 0) {
//                    int sw1 = one % 10; //个位数
//                    for (int ii = 0; ii < msg.length(); ii++) {
//                        msg.charAt(ii);
//                        if (String.valueOf(msg.charAt(ii)).equals(sw1 + ""))//如果==限制的数。处理。
//                        {
//                            if (sw1 > 0) {
//                                one = one - 1;
//                                sw1 = sw1 - 1;
//                                continue;
//                            } else {
//                                one = one + 1;
//                                sw1 = sw1 + 1;
//                            }
//                        }
//                    }
//                }
//            }
        }

        //剩下的金额
        int moneyOther = money - one;
        //校验这种随机方案是否可行，不合法的话，就要重新分配方案
        if (isRight(moneyOther, count - 1,mlist)) {

            return one;
        } else {
            //重新分配
            double avg = moneyOther / (count - 1);
            //本次红包过大，导致下次的红包过小；如果红包过大，下次就随机一个小值到本次红包金额的一个红包
            if (avg < MINMONEY) {
                //递归调用，修改红包最大金额
                return randomRedPacket(msg, imgroupExpand, money, minS, one, count);

            } else if (avg > MAXMONEY) {
                //递归调用，修改红包最小金额
                return randomRedPacket(msg, imgroupExpand, money, one, maxS, count);
            }
        }
        return one;
    }


    public static boolean isNumeric(String str)
    {
        for (int i = 0; i < str.length(); i++) {
            System.out.println(str.charAt(i));
            if (!Character.isDigit(str.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    /**
     * 红包 合法性校验
     * @param money
     * @param count
     * @return
     */
    private boolean isRight(int money, int count,List<String> list) {
        double avg =money/count;

        //小于最小金额
        if(avg<MINMONEY){
            return false;
            //大于最大金额
        }else if(avg>MAXMONEY){
            return false;
        }
        int  sw1 =money%10;
        if(list!=null&&list.contains(String.valueOf(sw1))){
            return false;
        }
        return true;
    }

}
