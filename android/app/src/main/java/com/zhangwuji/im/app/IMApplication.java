package com.zhangwuji.im.app;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.multidex.MultiDex;
import android.support.multidex.MultiDexApplication;

import com.tencent.bugly.crashreport.CrashReport;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;
import com.zhangwuji.im.UrlConstant;
import com.zhangwuji.im.imcore.service.IMService;
import com.zhangwuji.im.utils.FileUtil;
import com.zhangwuji.im.utils.ImageLoaderUtil;
import com.zhangwuji.im.utils.Logger;

import java.text.SimpleDateFormat;
import java.util.Date;

//import com.zhangwuji.im.MyEventBusIndex;
//import com.zhangwuji.im.MyLibEventBusIndex;


public class IMApplication extends MultiDexApplication {

    private Logger logger = Logger.getLogger(IMApplication.class);
    private static IMApplication mInstance = null;
    public static IWXAPI mWxApi;

    private void registerToWX() {
        //第二个参数是指你应用在微信开放平台上的AppID
        mWxApi = WXAPIFactory.createWXAPI(this, UrlConstant.WEIXIN_APP_ID, false);
        // 将该app注册到微信
        mWxApi.registerApp(UrlConstant.WEIXIN_APP_ID);
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;

        registerToWX();

        logger.i("Application starts");
        startIMService();
        ImageLoaderUtil.initImageLoaderConfig(getApplicationContext());
        //	EventBus.builder().addIndex(new MyEventBusIndex()).addIndex(new MyLibEventBusIndex()).installDefaultEventBus();

        //腾讯异常上报平台,配置成自已的appid即可
        CrashReport.initCrashReport(getApplicationContext(), "d38a1aacfc", true);
    }

    private void startIMService() {
        logger.i("start IMService");
        Intent intent = new Intent();
        intent.setClass(this, IMService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent);
        } else {
            startService(intent);
        }
    }

    public static boolean gifRunning = true;//gif是否运行

    public static IMApplication getInstance() {
        return mInstance;
    }

    /*
     * 获取以后再说标志
     */
    public int getLateUpdate() {
        Object obj = FileUtil.readObject(mInstance, "versionupdate_late.dat");
        if (obj != null) {
            String today = formatDate(getNow(),
                    "yyyy-MM-dd");
            if (today.equals(obj.toString())) {
                return 1;
            }
        }
        return 0;
    }

    /*
     * 设置以后再说标志
     */
    public void setLateUpdate(int lateUpdate) {
        if (lateUpdate == 1) {
            String today = formatDate(getNow(),
                    "yyyy-MM-dd");
            FileUtil.saveObject(mInstance, today, "versionupdate_late.dat");
        } else {
            FileUtil.saveObject(mInstance, "", "versionupdate_late.dat");
        }
    }

    public static String formatDate(Date date, String format) {
        if (date == null) {
            return "";
        }
        return new SimpleDateFormat(format).format(date);
    }

    public static Date getNow() {
        return new Date(System.currentTimeMillis());
    }

}
