package com.zhangwuji.im.ui.helper;

import android.content.Context;
import android.content.SharedPreferences;

import com.zhangwuji.im.DB.sp.SystemConfigSp;
import com.zhangwuji.im.server.network.BaseAction;

import java.util.HashMap;

public class ApiAction extends BaseAction {

    private final String CONTENT_TYPE = "application/json";
    private final String ENCODING = "utf-8";
    private SharedPreferences sp;

    /**
     * 构造方法
     *
     * @param context 上下文
     */
    public ApiAction(Context context) {
        super(context);
        sp = context.getSharedPreferences("config", context.MODE_PRIVATE);
    }

    public void UserLogin(String username, String password, final ResultCallback<String> callback) {
        String url = getURL("/api/checkLogin");
        HashMap parms = new HashMap();
        parms.put("appid", SystemConfigSp.instance().getStrConfig(SystemConfigSp.SysCfgDimension.APPID));
        parms.put("username", username);
        parms.put("password", password);
        IMHttpPostCallBack(url, parms, callback);
    }

    public void UserReg(String nickname, String username, String password, String code,String tjcode, final ResultCallback<String> callback) {
        String url = getURL("/api/reg");
        HashMap parms = new HashMap();
        parms.put("appid", SystemConfigSp.instance().getStrConfig(SystemConfigSp.SysCfgDimension.APPID));
        parms.put("username", username);
        parms.put("password", password);
        parms.put("code", code);
        parms.put("nickname", nickname);
        parms.put("tjcode",tjcode);
        IMHttpPostCallBack(url, parms, callback);
    }

    public void getNearByUser(String citycode, String lng, String lat, int page, final ResultCallback<String> callback) {
        String url = getURL("/api/getNearByUser");
        HashMap parms = new HashMap();
        parms.put("lng", lng);
        parms.put("lat", lat);
        parms.put("citycode", citycode);
        parms.put("page", page + "");
        parms.put("pagesize", "20");
        IMHttpPostCallBack(url, parms, callback);
    }

    public void addFriend(int userid, final ResultCallback<String> callback) {
        String url = getURL("/api/addFriend");
        HashMap parms = new HashMap();
        parms.put("friuid", userid + "");
        IMHttpPostCallBack(url, parms, callback);
    }

    public void beFriend(String userid, final ResultCallback<String> callback) {
        String url = getURL("/api/agreeFriend");
        HashMap parms = new HashMap();
        parms.put("friuid", userid);
        IMHttpPostCallBack(url, parms, callback);
    }

    public void checkRedPacket(String pid, final ResultCallback<String> callback) {
        String url = getURL("/api/luckymoney/checkRedPacket");
        HashMap parms = new HashMap();
        parms.put("pid", pid);
        IMHttpPostCallBack(url, parms, callback);
    }

    public void getRedPacket(String pid, final ResultCallback<String> callback) {
        String url = getURL("/api/luckymoney/getRedPacket");
        HashMap parms = new HashMap();
        parms.put("pid", pid);
        IMHttpPostCallBack(url, parms, callback);
    }

    public void sendRedPacket(String paypwd, int type, int type2, double allmoney, int allnum, String groupId, String msg, final ResultCallback<String> callback) {
        String url = getURL("/api/luckymoney/sendRedPacket");
        HashMap parms = new HashMap();
        parms.put("paypwd", paypwd);
        parms.put("type", type + "");
        parms.put("type2", type2 + "");
        parms.put("allmoney", allmoney + "");
        parms.put("allnum", allnum + "");
        parms.put("groupId", groupId);
        parms.put("msg", msg);
        IMHttpPostCallBack(url, parms, callback);
    }

    public void getRedPacketInfo(String pid, final ResultCallback<String> callback) {
        String url = getURL("/api/luckymoney/getRedPacketInfo");
        HashMap parms = new HashMap();
        parms.put("pid", pid);
        IMHttpPostCallBack(url, parms, callback);
    }

    public void getRedPacketLog(String pid, final ResultCallback<String> callback) {
        String url = getURL("/api/luckymoney/getRedPacketLog");
        HashMap parms = new HashMap();
        parms.put("pid", pid);
        IMHttpPostCallBack(url, parms, callback);
    }

    public void getUserInfoByPhone(String phone, final ResultCallback<String> callback) {
        String url = getURL("/api/getUserInfoByUserName");
        HashMap parms = new HashMap();
        parms.put("username", phone);
        IMHttpPostCallBack(url, parms, callback);
    }

    public void checkPayPWD(final ResultCallback<String> callback) {
        String url = getURL("/api/user-account/check_paypwd");
        HashMap parms = new HashMap();
        IMHttpPostCallBack(url, parms, callback);
    }
    public void setPayPWD(String payPWD,final ResultCallback<String> callback) {
        String url = getURL("/api/user-account/set_paypwd");
        HashMap parms = new HashMap();
        parms.put("paypwd",payPWD);
        IMHttpPostCallBack(url, parms, callback);
    }

    public void checkUpdate(final ResultCallback<String> callback) {
        String url = "http://im.chaoliaochat.com/update/ver.json";
        HashMap parms = new HashMap();
        IMHttpGetCallBack(url, callback);
    }

    public void agreeQRLogin(String code, final ResultCallback<String> callback) {
        String url = getURL("/api/agreeQRLogin");
        HashMap parms = new HashMap();
        parms.put("code", code);
        IMHttpPostCallBack(url, parms, callback);
    }

    public void bingWeiXinLogin(String openid,String access_token,String unionid,String nickname,String avatar,String sex,final ResultCallback<String> callback) {
        String url = getURL("/api/bingWeiXinLogin ");
        HashMap parms = new HashMap();
        parms.put("openid", openid);
        parms.put("access_token", access_token);
        parms.put("unionid", unionid);
        parms.put("nickname", nickname);
        parms.put("avatar", avatar);
        parms.put("sex", sex);
        IMHttpPostCallBack(url, parms, callback);
    }

    public void bingWeiXinAccount(String openid,String username,String code,String tjcode,String password,final ResultCallback<String> callback) {
        String url = getURL("/api/bingAccount");
        HashMap parms = new HashMap();
        parms.put("openid", openid);
        parms.put("username", username);
        parms.put("code", code);
        parms.put("tjcode", tjcode);
        parms.put("password", password);
        IMHttpPostCallBack(url, parms, callback);
    }
    public void getMyAccount(final ResultCallback<String> callback) {
        String url = getURL("/api/user-account/my_account");
        HashMap parms = new HashMap();
        IMHttpPostCallBack(url, parms, callback);
    }

    public void getWXAccessToken(String url, final ResultCallback<String> callback) {
        IMHttpGetCallBack(url, callback);
    }

    public void delete_message(String fromId,String toId,String msgId,String sessionType,final ResultCallback<String> callback) {
        String url = getURL("/api/message/del_message");
        HashMap parms = new HashMap();
        parms.put("fromId", fromId);
        parms.put("toId", toId);
        parms.put("msgId", msgId);
        parms.put("sessionType", sessionType);
        IMHttpPostCallBack(url, parms, callback);
    }
    public void set_dis_send_msg(Long groupId,String uids,int flag,final ResultCallback<String> callback) {
        String url = getURL("/api/i-mgroup-message/disable_send_msg");
        HashMap parms = new HashMap();
        parms.put("groupId",groupId+"");
        parms.put("flag",flag+"");
        parms.put("uids",uids);
        IMHttpPostCallBack(url, parms, callback);
    }

    public void edit_userinfo(int sex,String nickname,String avatar,String sign_info, ResultCallback<String> callback) {
        String url = getURL("/api/setUserInfo");
        HashMap parms = new HashMap();
        parms.put("sex",sex+"");
        parms.put("nickname",nickname);
        parms.put("sign_info",sign_info);
        parms.put("avatar",avatar);
        IMHttpPostCallBack(url, parms, callback);
    }
}
