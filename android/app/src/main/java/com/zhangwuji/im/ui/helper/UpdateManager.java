package com.zhangwuji.im.ui.helper;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.qmuiteam.qmui.widget.dialog.QMUIDialog;
import com.qmuiteam.qmui.widget.dialog.QMUIDialogAction;
import com.qmuiteam.qmui.widget.dialog.QMUITipDialog;
import com.zhangwuji.im.BuildConfig;
import com.zhangwuji.im.R;
import com.zhangwuji.im.app.IMApplication;
import com.zhangwuji.im.server.network.BaseAction;
import com.zhangwuji.im.ui.entity.UpgradeEntity;
import com.zhangwuji.im.ui.widget.A_CustomAlertDialog;

import org.json.JSONException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DecimalFormat;

public class UpdateManager {

    private static final int DOWN_NOSDCARD = 0;
    private static final int DOWN_UPDATE = 1;
    private static final int DOWN_OVER = 2;

    private static UpdateManager updateManager;

    private Activity mContext;
    // 下载对话框
    //    private QMUIDialog downloadDialog;

    // 进度条
    private ProgressBar mProgress;
    // 显示下载数值
    private TextView mProgressText;
    // 查询动画
    private QMUITipDialog mProDialog;

    // 进度值
    private int progress;
    // 下载线程
    private Thread downLoadThread;
    // 终止标记
    private boolean interceptFlag;
    // 提示语
    private String updateMsg = "";
    // 返回的安装包url
    private String apkUrl = "";
    // 下载包保存路径
    private String savePath = "";
    // apk保存完整路径
    private String apkFilePath = "";
    // 临时下载文件路径
    private String tmpFilePath = "";
    // 下载文件大小
    private String apkFileSize;
    // 已下载文件大小
    private String tmpFileSize;

    private String curVersionName = "";
    private int curVersionCode;
    private UpgradeEntity mUpdate;

    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            try {
                switch (msg.what) {
                    case DOWN_UPDATE:
                        mProgress.setProgress(progress);
                        mProgressText.setText(tmpFileSize + "/" + apkFileSize);
                        break;
                    case DOWN_OVER:
                        downloadDialog.dismiss();
                        installApk();
                        break;
                    case DOWN_NOSDCARD:
                        downloadDialog.dismiss();
                        Toast.makeText(mContext, "无法下载安装文件，请检查SD卡是否挂载", Toast.LENGTH_SHORT).show();

                        break;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };
    private A_CustomAlertDialog downloadDialog;

    public static UpdateManager getUpdateManager() {
        if (updateManager == null) {
            updateManager = new UpdateManager();
        }
        updateManager.interceptFlag = false;
        return updateManager;
    }

    public void Release() {
        if (mProDialog != null) {
            mProDialog.dismiss();
            mProDialog = null;
        }
        interceptFlag = true;

    }

    /**
     * 检查App更新
     *
     * @param context
     * @param isShowMsg    是否显示提示消息
     * @param isShowUpdate 是否显示更新提示框
     */
    public void checkAppUpdate(final Activity context, final boolean isShowMsg, final boolean isShowUpdate,
                               final OnUpdateAppListenner onUpdateAppListenner) {
        this.mContext = context;
        getCurrentVersion();
        if (isShowMsg) {
            mProDialog = new QMUITipDialog.Builder(context)
                    .setIconType(QMUITipDialog.Builder.ICON_TYPE_LOADING)
                    .setTipWord("正在检测，请稍后...")
                    .create(false);
        }
        final Handler handler = new Handler() {
            public void handleMessage(Message msg) {
                // 关闭并释放释放进度条对话框
                if (mProDialog != null) {
                    mProDialog.dismiss();
                }
                // 显示检测结果
                try {
                    mUpdate = (UpgradeEntity) msg.obj;
                    String serverVersion = mUpdate.getVersionCode();
                    // 如果服务版本和当前版本不一致，则更新,字符串比较
                    if (!curVersionName.equals(serverVersion)) {
                        apkUrl = mUpdate.getDownUrl();
                        updateMsg = mUpdate.getUpgradeLog();
                        if (isShowUpdate) {
                            showNoticeDialog(mUpdate.getUpgradeType(), context);
                        }
                        if (onUpdateAppListenner != null) {
                            onUpdateAppListenner.hasUpdate("s".equals(mUpdate.getUpgradeType()));
                        }
                    } else {
                        if (onUpdateAppListenner != null) {
                            onUpdateAppListenner.noUpdate();
                        }
                    }
                } catch (Exception ex) {
                    if (onUpdateAppListenner != null) {
                        onUpdateAppListenner.error();
                    }
                }
            }
        };
        ThreadPoolFactory.getInstance().execute(new Runnable() {
            @Override
            public void run() {
                final Message msg = handler.obtainMessage();

                ApiAction apiAction = new ApiAction(context);
                apiAction.checkUpdate(new BaseAction.ResultCallback<String>() {
                    @Override
                    public void onSuccess(String s) {

                        org.json.JSONObject jsonObject = null;
                        int code = 0;
                        try {
                            jsonObject = new org.json.JSONObject(s);
                            code = jsonObject.getInt("code");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        if (code == 200) {
                            try {
                                if (!StringUtils.isEmpty(jsonObject.optString("data"))) {
                                    try {
                                        UpgradeEntity entity = UpgradeEntity.parse(jsonObject.optString("data"));
                                        msg.what = 1;
                                        msg.obj = entity;
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                        msg.what = -1;
                                        msg.obj = e;
                                    }
                                } else {
                                    msg.what = -1;
                                    msg.obj = null;
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        } else {
                            //                            UpgradeEntity upgradeEntity = new UpgradeEntity();
                            //                            upgradeEntity.setDownUrl("https://downapp.baidu.com/baidutieba/AndroidPhone/9.5.8.0/1/1021702g/20180627105420/baidutieba_AndroidPhone_9-5-8-0_1021702g.apk?responseContentDisposition=attachment%3Bfilename%3D%22baidutieba_AndroidPhone_v9.5.8.0%289.5.8.0%29_1021702g.apk%22&responseContentType=application%2Fvnd.android.package-archive&request_id=1531303103_5483617452&type=dynamic");
                            //                            upgradeEntity.setUpgradeLog("版本更新了");
                            //                            upgradeEntity.setVersionCode("2.1.0");
                            //                            upgradeEntity.setUpgradeType("s");
                            //                            msg.what = 1;
                            //                            msg.obj = upgradeEntity;

                            msg.what = -1;
                            msg.obj = new Exception();
                        }
                        handler.sendMessage(msg);
                    }

                    @Override
                    public void onError(String errString) {
                        msg.what = -1;
                        msg.obj = new Exception();
                        handler.sendMessage(msg);
                    }
                });

            }
        });
    }

    /**
     * 获取当前客户端版本信息
     */
    private void getCurrentVersion() {
        try {
            PackageInfo info = mContext.getPackageManager().getPackageInfo(
                    mContext.getPackageName(), 0);
            curVersionName = info.versionName;
            curVersionCode = info.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace(System.err);
        }
    }

    /**
     * 显示版本更新通知对话框
     */
    private void showNoticeDialog(final String upgradeType, final Context mContext) {
        if (mContext != null && !((Activity) mContext).isFinishing()) {

            final QMUIDialog.MessageDialogBuilder builder = new QMUIDialog.MessageDialogBuilder(mContext);
            builder.setTitle("软件版本更新")
                    .setCanceledOnTouchOutside(false)
                    .addAction("s".equals(upgradeType) ? "明天再说" : "退出", new QMUIDialogAction.ActionListener() {
                        @Override
                        public void onClick(QMUIDialog dialog, int index) {
                            dialog.dismiss();
                            if (!"s".equals(upgradeType)) {
                                AppManager.getAppManager().AppExit(mContext);
                            } else {
                                (IMApplication.getInstance()).setLateUpdate(1);
                            }
                        }
                    })
                    .addAction("立即更新", new QMUIDialogAction.ActionListener() {
                        @Override
                        public void onClick(final QMUIDialog dialog, int index) {
                            dialog.dismiss();
                            showDownloadDialog(mContext);
                        }
                    });
            builder.show();

        }
    }

    /**
     * 显示下载对话框
     */
    private void showDownloadDialog(Context mContext) {
        final LayoutInflater inflater = LayoutInflater.from(mContext);
        View v = inflater.inflate(R.layout.update_progress, null);
        mProgress = (ProgressBar) v.findViewById(R.id.update_progress);
        mProgressText = (TextView) v
                .findViewById(R.id.update_progress_text);

        downloadDialog = new A_CustomAlertDialog(mContext);
        downloadDialog.reset()
                .setTitle("正在下载最新版本")
                .setView(v, false)
                .setIsCancel(false)
                .show();

        downloadApk();
    }

    private Runnable mdownApkRunnable = new Runnable() {
        @Override
        public void run() {
            try {
                String apkName = "cloudtalk-" + mUpdate.getVersionCode()
                        + ".apk";
                String tmpApk = "cloudtalk-" + mUpdate.getVersionCode()
                        + ".apk";
                // 判断是否挂载了SD卡
                String storageState = Environment.getExternalStorageState();
                if (storageState.equals(Environment.MEDIA_MOUNTED)) {
                    savePath = Environment.getExternalStorageDirectory()
                            .getAbsolutePath() + "/cloudtalk/download/";
                    File file = new File(savePath);
                    if (!file.exists()) {
                        file.mkdirs();
                    }
                    apkFilePath = savePath + apkName;
                    tmpFilePath = savePath + tmpApk;
                }

                // 没有挂载SD卡，无法下载文件
                if (apkFilePath == null || apkFilePath == "") {
                    mHandler.sendEmptyMessage(DOWN_NOSDCARD);
                    return;
                }

                File ApkFile = new File(apkFilePath);

                // 是否已下载更新文件
                if (ApkFile.exists()) {
                    if (downloadDialog != null) {
                        downloadDialog.dismiss();
                    }
                    installApk();
                    return;
                }

                // 输出临时下载文件
                File tmpFile = new File(tmpFilePath);
                FileOutputStream fos = new FileOutputStream(tmpFile);

                URL url = new URL(apkUrl);
                HttpURLConnection conn = (HttpURLConnection) url
                        .openConnection();
                conn.connect();
                int length = conn.getContentLength();
                InputStream is = conn.getInputStream();

                // 显示文件大小格式：2个小数点显示
                DecimalFormat df = new DecimalFormat("0.00");
                // 进度条下面显示的总文件大小
                apkFileSize = df.format((float) length / 1024 / 1024) + "MB";

                int count = 0;
                byte buf[] = new byte[1024];

                do {
                    int numread = is.read(buf);
                    count += numread;
                    // 进度条下面显示的当前下载文件大小
                    tmpFileSize = df.format((float) count / 1024 / 1024) + "MB";
                    // 当前进度值
                    progress = (int) (((float) count / length) * 100);
                    // 更新进度
                    mHandler.sendEmptyMessage(DOWN_UPDATE);
                    if (numread <= 0) {
                        // 下载完成 - 将临时下载文件转成APK文件
                        //if (tmpFile.renameTo(ApkFile)) {
                            // 通知安装
                            mHandler.sendEmptyMessage(DOWN_OVER);
                       // }
                        break;
                    }
                    fos.write(buf, 0, numread);
                } while (!interceptFlag);// 点击取消就停止下载

                fos.close();
                is.close();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    };

    /**
     * 下载apk
     */
    private void downloadApk() {
        downLoadThread = new Thread(mdownApkRunnable);
        downLoadThread.start();
    }

    /**
     * 安装apk
     */
    private void installApk() {
        File apkfile = new File(apkFilePath);
        if (!apkfile.exists()) {
            return;
        }
        Intent i = new Intent(Intent.ACTION_VIEW);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            i.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            Uri contentUri = FileProvider.getUriForFile(mContext,
                    BuildConfig.APPLICATION_ID + ".provider", apkfile);
            i.setDataAndType(contentUri, "application/vnd.android.package-archive");
        } else {
            i.setDataAndType(Uri.fromFile(apkfile), "application/vnd.android.package-archive");
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }

        if (mContext != null) {
            try {
                mContext.startActivity(i);
            } catch (Exception e) {
                e.printStackTrace();
                Log.e("tags", "installApk: " + e.toString());
            }
        }
    }


    public interface OnUpdateAppListenner {
        /**
         * 是否建议升级
         *
         * @param isAdvise true 建议升级，false 强制升级
         */
        void hasUpdate(boolean isAdvise);

        void noUpdate();

        void error();
    }
}
