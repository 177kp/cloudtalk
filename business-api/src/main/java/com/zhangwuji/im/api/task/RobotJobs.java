package com.zhangwuji.im.api.task;


import com.zhangwuji.im.MainConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class RobotJobs {


    @Autowired
    MainConfig mainConfig;

    public final static long ONE_Minute = 60*1000;
    public final static long ONE_Minute2 = 100;
    private static final Logger log = LoggerFactory.getLogger(RobotJobs.class);

    //隔一定时间执行一次，等上次任务执行完毕
    @Scheduled(fixedDelay=ONE_Minute)
    public void fixedDelayJob(){
        mainConfig.initRobotList();
    }

    //隔一定时间执行一次，等上次任务执行完毕
    @Scheduled(fixedDelay=ONE_Minute2)
    public void fixedDelayJob2(){
        mainConfig.initGetAutoRedPacket();
    }


    @Scheduled(cron="0 15 3 * * ?")
    public void cronJob(){
        System.out.println(new Date()+" >>cron执行....");
    }

}
