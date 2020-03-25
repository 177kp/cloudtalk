package com.zhangwuji.im.ui.helper;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;

import java.util.List;
import java.util.Stack;

/**
 * Activity的创建和销毁
 *
 * @author raoh
 * @data: 2013-12-31 下午5:22:21
 * @version: V1.0
 */
public class AppManager {
    private static Object obj = new Object();
    private static Stack<Activity> activityStack;
    private static AppManager instance;

    private AppManager() {
    }

    /**
     * 单一实例
     */
    public static AppManager getAppManager() {
        if (instance == null) {
            synchronized (obj) {
                if (instance == null) {
                    instance = new AppManager();
                }
            }
        }
        return instance;
    }

    /**
     * 返回Activity个数
     */
    public int getActivityCount() {
        return isEmptyList(activityStack) ? 0 : activityStack.size();
    }

    /**
     * 判断数据集是否为空，为空则返回true
     *
     * @param list input
     * @return boolean
     */
    public static boolean isEmptyList(List list) {
        if (list == null || list.size() == 0) {
            return true;
        }
        return false;
    }

    /**
     * 添加Activity到堆栈
     */
    public void addActivity(Activity activity) {
        synchronized (obj) {
            if (activityStack == null) {
                activityStack = new Stack<Activity>();
            }
            activityStack.add(activity);
            // 回收页面，超过10个页面开始回收老页面
            recycleOldPages();
        }
    }

    /**
     * 回收老的页面，一次回收一个，因为一次也就添加一个
     */
    public void recycleOldPages() {
        if (getCanRecycledPageCount() > 10) {
            for (int i = 0; i < activityStack.size(); i++) {
                Activity act = activityStack.get(i);
                activityStack.remove(act);
                act.finish();
                break;
            }
        }
    }

    /**
     * 计算处理首页的几个页面，其他页面的个数
     */
    public int getCanRecycledPageCount() {
        return activityStack.size();
    }


    /**
     * 删除除了MainActivity和根页面的其他页面
     */
    public void deleteToBaseActivities() {
        for (int i = activityStack.size() - 1; i >= 0; i--) {
            if (null != activityStack.get(i)) {

                Activity act = activityStack.get(i);
                activityStack.remove(act);
                act.finish();
            }
        }
    }

    /**
     * 获取当前Activity（堆栈中最后一个压入的）
     */
    public Activity currentActivity() {
        Activity activity = activityStack.lastElement();
        return activity;
    }

    /**
     * 结束当前Activity（堆栈中最后一个压入的）
     */
    public void finishActivity() {
        Activity activity = activityStack.lastElement();
        finishActivity(activity);
    }

    /**
     * 结束指定的Activity
     */
    public void finishActivity(Activity activity) {
        if (activity != null) {
            activityStack.remove(activity);
            activity.finish();
        }
    }

    /**
     * 结束指定类名的Activity
     */
    public void finishActivity(Class<?> cls) {
        for (Activity activity : activityStack) {
            if (activity.getClass().equals(cls)) {
                finishActivity(activity);
            }
        }
    }

    /**
     * 结束所有Activity
     */
    public void finishAllActivity() {
        finishAllActivity(null);
    }

    public void finishAllActivity(Class<?> exceptcls) {
        try {
            int size = activityStack.size();
            for (int i = size - 1; i >= 0; i--) {
                if (null != activityStack.get(i)) {
                    Activity ati = activityStack.get(i);
                    if (exceptcls == null || !exceptcls.equals(ati.getClass())) {
                        activityStack.remove(i);
                        ati.finish();
                        ati = null;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 删除栈中该页面的实例
     */
    public void finishFormalAndRemainLastActivity(String activity) {
        int remainCount = 0;// 保留页面个数，超过1个，finish掉
        for (int i = activityStack.size() - 1; i >= 1; i--) {
            if (activityStack.get(i).getClass().getSimpleName().equals(activity)) {
                remainCount++;
                if (remainCount > 1) {
                    Activity tempActivity = activityStack.get(i);
                    activityStack.remove(i);
                    tempActivity.finish();
                }
            }
        }
    }

    /**
     * 结束到第几个页面 ,第一个页面是MainActivity
     */
    public void finshToActivity(int index) {
        for (int i = activityStack.size() - 1, j = activityStack.size() - 1 - index; i > j; i--) {
            if (i > 0 && i < activityStack.size() && null != activityStack.get(i)) {

                Activity tempActivity = activityStack.get(i);
                activityStack.remove(i);
                tempActivity.finish();
            }
        }
    }

    /**
     * 打印页面
     */
    public void printActivityInfo() {
        //	String activities = "";
        //	for (int i = 0; i < activityStack.size(); i++) {
        //	    activities += activityStack.get(i).getClass().getSimpleName() + " ";
        //	}
        // LogUtil.log_msg(activities);
    }

    /**
     * （直接）退出应用程序
     */
    public void AppExit(Context context) {
        try {
            finishAllActivity();

            ActivityManager activityMgr = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            activityMgr.restartPackage(context.getPackageName());
            System.exit(0);
        } catch (Exception e) {
        }
    }
}
