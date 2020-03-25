package com.zhangwuji.im.utils;

/**
 * Created by zhujian on 15/1/14.
 */

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.provider.Settings;
import android.view.Window;
import android.view.WindowManager;

import java.lang.reflect.Field;

/**
 * 获取屏幕,分辨率相关
 */
public class ScreenUtil {
    private Context mCtx;
    private static ScreenUtil mScreenTools;

    public static ScreenUtil instance(Context ctx) {
        if (null == mScreenTools) {
            mScreenTools = new ScreenUtil(ctx);
        }
        return mScreenTools;
    }

    private ScreenUtil(Context ctx) {
        mCtx = ctx.getApplicationContext();
    }

    public int getScreenWidth() {
        return mCtx.getResources().getDisplayMetrics().widthPixels;
    }

    public int dip2px(int dip) {
        float density = getDensity(mCtx);
        return (int) (dip * density + 0.5);
    }

    /**
     * 根据手机的分辨率从dp转换成px(像素)
     */
    public static int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    public int px2dip(int px) {
        float density = getDensity(mCtx);
        return (int) ((px - 0.5) / density);
    }

    private float getDensity(Context ctx) {
        return ctx.getResources().getDisplayMetrics().density;
    }

    /**
     * ５40 的分辨率上是85 （
     *
     * @return
     */
    public int getScal() {
        return (int) (getScreenWidth() * 100 / 480);
    }

    /**
     * 宽全屏, 根据当前分辨率　动态获取高度
     * height 在８００*４８０情况下　的高度
     *
     * @return
     */
    public int get480Height(int height480) {
        int width = getScreenWidth();
        return (height480 * width / 480);
    }

    /**
     * 获取状态栏高度
     *
     * @return
     */
    public int getStatusBarHeight() {
        Class<?> c = null;
        Object obj = null;
        Field field = null;
        int x = 0, sbar = 0;
        try {
            c = Class.forName("com.android.internal.R$dimen");
            obj = c.newInstance();
            field = c.getField("status_bar_height");
            x = Integer.parseInt(field.get(obj).toString());
            sbar = mCtx.getResources().getDimensionPixelSize(x);
        } catch (Exception e1) {
            e1.printStackTrace();
        }
        return sbar;
    }

    public int getScreenHeight() {
        return mCtx.getResources().getDisplayMetrics().heightPixels;
    }

    /**
     * 判断是否开启了自动亮度调节
     *
     * @param context
     * @return
     */
    public static boolean isAutoBrightness(Context context) {
        ContentResolver resolver = context.getContentResolver();
        boolean automicBrightness = false;
        try {
            automicBrightness = Settings.System.getInt(resolver,
                    Settings.System.SCREEN_BRIGHTNESS_MODE) == Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC;
        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
        }
        return automicBrightness;
    }

    /**
     * 获取当前屏幕亮度
     *
     * @param context
     * @return
     */
    public static int getScreenBrightness(Context context) {
        int nowBrightnessValue = 0;
        ContentResolver resolver = context.getContentResolver();
        try {
            nowBrightnessValue = android.provider.Settings.System.getInt(resolver, Settings.System.SCREEN_BRIGHTNESS);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return nowBrightnessValue;
    }

    /**
     * 关闭自动亮度调节
     *
     * @param activity
     * @param flag
     * @return
     */
    public static boolean autoBrightness(Context activity, boolean flag) {
        int value = 0;
        if (flag) {
            value = Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC; //开启
        } else {
            value = Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL;//关闭
        }
        return Settings.System.putInt(activity.getContentResolver(),
                Settings.System.SCREEN_BRIGHTNESS_MODE,
                value);
    }

    /**
     * 设置亮度，退出app也能保持该亮度值
     *
     * @param context
     * @param brightness
     */
    public static void saveBrightness(Context context, int brightness) {
        ContentResolver resolver = context.getContentResolver();
        Uri uri = android.provider.Settings.System.getUriFor(Settings.System.SCREEN_BRIGHTNESS);
        android.provider.Settings.System.putInt(resolver, Settings.System.SCREEN_BRIGHTNESS, brightness);
        resolver.notifyChange(uri, null);
    }

    /**
     * 设置当前activity显示的亮度
     *
     * @param activity
     * @param brightness
     */
    public static void setBrightness(Activity activity, float brightness) {
        //        WindowManager.LayoutParams lp = activity.getWindow().getAttributes();
        //        lp.screenBrightness = Float.valueOf(brightness) * (1f / 255f);
        //        activity.getWindow().setAttributes(lp);

        Window window = activity.getWindow();
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.screenBrightness = brightness;
        window.setAttributes(lp);

    }
}