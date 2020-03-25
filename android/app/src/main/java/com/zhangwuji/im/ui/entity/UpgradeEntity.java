package com.zhangwuji.im.ui.entity;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * TODO<应用程序更新实体类>
 */
public class UpgradeEntity {

    private String versionCode; // 最新版本号
    private String downUrl;// apk下载地址
    private String upgradeType;// 升级类型 f强制 s建议
    private String upgradeLog;// 更新内容

    public String getVersionCode() {
        return versionCode;
    }

    public void setVersionCode(String versionCode) {
        this.versionCode = versionCode;
    }

    public String getDownUrl() {
        return downUrl;
    }

    public void setDownUrl(String downUrl) {
        this.downUrl = downUrl;
    }

    public String getUpgradeType() {
        return upgradeType;
    }

    public void setUpgradeType(String upgradeType) {
        this.upgradeType = upgradeType;
    }

    public String getUpgradeLog() {
        return upgradeLog;
    }

    public void setUpgradeLog(String upgradeLog) {
        this.upgradeLog = upgradeLog;
    }

    /*
     * 解析json
     */
    public static UpgradeEntity parse(String input) throws JSONException {
        UpgradeEntity entity = null;
        JSONObject object = new JSONObject(input);
        if (object != null) {
            entity = new UpgradeEntity();
            entity.setDownUrl(object.optString("downUrl"));
            entity.setUpgradeLog(object.optString("upgradeLog"));
            entity.setUpgradeType(object.optString("upgradeType"));
            entity.setVersionCode(object.optString("versionCode"));
        }
        return entity;
    }
}
