package com.zhangwuji.im.wxapi;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Window;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.qmuiteam.qmui.widget.dialog.QMUITipDialog;
import com.tencent.mm.opensdk.modelbase.BaseReq;
import com.tencent.mm.opensdk.modelbase.BaseResp;
import com.tencent.mm.opensdk.modelmsg.SendAuth;
import com.tencent.mm.opensdk.openapi.IWXAPIEventHandler;
import com.zhangwuji.im.R;
import com.zhangwuji.im.UrlConstant;
import com.zhangwuji.im.app.IMApplication;
import com.zhangwuji.im.server.network.BaseAction;
import com.zhangwuji.im.ui.activity.LoginActivity;
import com.zhangwuji.im.ui.activity.MainActivity;
import com.zhangwuji.im.ui.activity.RegActivity;
import com.zhangwuji.im.ui.base.TTBaseActivity;
import com.zhangwuji.im.ui.helper.ApiAction;

/**
 * Created by chenwang on 2019/8/11.
 */
public class WXEntryActivity  extends FragmentActivity implements IWXAPIEventHandler {

    private static final String TAG = "WXEntryActivity";
    private static final int RETURN_MSG_TYPE_LOGIN = 1; //登录
    private static final int RETURN_MSG_TYPE_SHARE = 2; //分享
    private Context mContext;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_wxlogin);
        mContext = this;
        //这句没有写,是不能执行回调的方法的
        IMApplication.mWxApi.handleIntent(getIntent(), this);

    }

    @Override
    public void onReq(BaseReq baseReq) {

    }

    @Override
    public void onResp(BaseResp baseResp) {
        int type = baseResp.getType(); //类型：分享还是登录
        switch (baseResp.errCode) {
            case BaseResp.ErrCode.ERR_AUTH_DENIED:
                Toast.makeText(mContext,"拒绝授权微信登录",Toast.LENGTH_SHORT).show();
                finish();
                //用户拒绝授权
            case BaseResp.ErrCode.ERR_USER_CANCEL:
                //用户取消
                String message = "";
                if (type == RETURN_MSG_TYPE_LOGIN) {
                    message = "取消了微信登录";
                } else if (type == RETURN_MSG_TYPE_SHARE) {
                    message = "取消了微信分享";
                }
                Toast.makeText(mContext,message,Toast.LENGTH_SHORT).show();
                finish();
                break;
            case BaseResp.ErrCode.ERR_OK:
                //用户同意
                if (type == RETURN_MSG_TYPE_LOGIN) {
                    //用户换取access_token的code，仅在ErrCode为0时有效
                    String code = ((SendAuth.Resp) baseResp).code;
                    Log.i(TAG, "code:------>" + code);
                    //这里拿到了这个code，去做2次网络请求获取access_token和用户个人信息

                    LoginActivity.getIns().onWXLoading();

                    ApiAction apiAction=new ApiAction(WXEntryActivity.this);
                    //获取access_token
                    String url = "https://api.weixin.qq.com/sns/oauth2/access_token?" +
                            "appid="+ UrlConstant.WEIXIN_APP_ID+"&secret="+UrlConstant.WEIXIN_APP_SECRET+
                            "&code="+code+"&grant_type=authorization_code";

                    apiAction.getWXAccessToken(url, new BaseAction.ResultCallback<String>() {
                        @Override
                        public void onSuccess(String s) {
                            JSONObject jsonObject=JSON.parseObject(s);
                            String access_token=jsonObject.get("access_token").toString();
                            String open_id=jsonObject.get("openid").toString();
                            String unionid=jsonObject.get("unionid").toString();

                            String url2 = "https://api.weixin.qq.com/sns/userinfo?access_token="+
                                    access_token+"&openid="+open_id;
                            apiAction.getWXAccessToken(url2, new BaseAction.ResultCallback<String>() {
                                @Override
                                public void onSuccess(String s) {
                                    JSONObject jsonObject=JSON.parseObject(s);
                                    String nickname=jsonObject.getString("nickname");
                                    String headimages=jsonObject.getString("headimgurl");
                                    String unionid=jsonObject.getString("unionid");
                                    String sex=jsonObject.getString("sex");
                                    String openid=jsonObject.getString("openid");

                                    apiAction.bingWeiXinLogin(openid, access_token, unionid, nickname, headimages, sex, new BaseAction.ResultCallback<String>() {
                                        @Override
                                        public void onSuccess(String s) {
                                            JSONObject jsonObject=JSON.parseObject(s);
                                            int code=jsonObject.getIntValue("code");
                                            if(code==200)
                                            {
                                                //调用登录方法
                                                LoginActivity.getIns().outLogin(jsonObject);
                                                LoginActivity.getIns().onWXLoadClose();
                                                finish();
                                            }
                                            else if(code==201)
                                            {
                                                //需要绑定账号
                                                Intent intent = new Intent(WXEntryActivity.this, RegActivity.class);
                                                intent.putExtra("wx_code",openid);
                                                intent.putExtra("openid",openid);
                                                startActivity(intent);

                                                LoginActivity.getIns().onWXLoadClose();
                                                finish();
                                            }
                                        }
                                        @Override
                                        public void onError(String errString) {
                                            finish();
                                        }
                                    });
                                }
                                @Override
                                public void onError(String errString) {
                                    finish();
                                }
                            });
                        }
                        @Override
                        public void onError(String errString) {
                            finish();
                        }
                    });

                } else if (type == RETURN_MSG_TYPE_SHARE) {
                    Toast.makeText(mContext,"微信分享成功",Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
        }
    }
}
