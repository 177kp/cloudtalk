package com.zhangwuji.im.ui.activity;

import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.qmuiteam.qmui.widget.dialog.QMUITipDialog;
import com.zhangwuji.im.DB.sp.SystemConfigSp;
import com.zhangwuji.im.R;
import com.zhangwuji.im.UrlConstant;
import com.zhangwuji.im.server.network.BaseAction;
import com.zhangwuji.im.ui.base.TTBaseActivity;
import com.zhangwuji.im.ui.helper.ApiAction;
import com.zhangwuji.im.ui.helper.LoginInfoSp;
import com.zhangwuji.im.utils.CommonUtil;
import com.zhangwuji.im.utils.Logger;

import org.json.JSONObject;

public class RegActivity extends TTBaseActivity implements View.OnClickListener {

    private Logger logger = Logger.getLogger(RegActivity.class);
    private Handler uiHandler = new Handler();
    private EditText reg_username, reg_phone, reg_code, reg_password,reg_tuijiancode;
    private EditText mPasswordView;
    private TextView reg_login, reg_forget;
    private Button reg_button, reg_getcode;
    LoginInfoSp loginInfoSp = LoginInfoSp.instance();
    LoginInfoSp.LoginInfoSpIdentity loginInfoIdentity;
    private QMUITipDialog tipDialog;
    private String wx_code="";
    private boolean isWXLogin=false;
    private String openid="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_register);

        wx_code=this.getIntent().getStringExtra("wx_code");
        openid=this.getIntent().getStringExtra("openid");

        if(wx_code!=null && !wx_code.equals(""))
        {
            isWXLogin=true;
        }

        loginInfoSp.init(this);
        loginInfoIdentity = loginInfoSp.getLoginInfoIdentity();

        SystemConfigSp.instance().init(getApplicationContext());
        if (TextUtils.isEmpty(SystemConfigSp.instance().getStrConfig(SystemConfigSp.SysCfgDimension.LOGINSERVER))) {
            SystemConfigSp.instance().setStrConfig(SystemConfigSp.SysCfgDimension.LOGINSERVER, UrlConstant.ACCESS_MSG_ADDRESS);
        }
        if (TextUtils.isEmpty(SystemConfigSp.instance().getStrConfig(SystemConfigSp.SysCfgDimension.APPID))) {
            SystemConfigSp.instance().setStrConfig(SystemConfigSp.SysCfgDimension.APPID, UrlConstant.appid);
        }

        reg_username = (EditText) findViewById(R.id.reg_username);
        reg_phone = (EditText) findViewById(R.id.reg_phone);
        reg_code = (EditText) findViewById(R.id.reg_code);
        reg_password = (EditText) findViewById(R.id.reg_password);

        reg_login = (TextView) findViewById(R.id.reg_login);
        reg_forget = (TextView) findViewById(R.id.reg_forget);

        reg_button = (Button) findViewById(R.id.reg_button);
        reg_getcode = (Button) findViewById(R.id.reg_getcode);

        reg_tuijiancode=(EditText) findViewById(R.id.reg_tuijiancode);


        reg_login.setOnClickListener(this);
        reg_forget.setOnClickListener(this);
        reg_button.setOnClickListener(this);
        reg_getcode.setOnClickListener(this);
        reg_tuijiancode.setVisibility(View.VISIBLE);

        if(isWXLogin)
        {
            //reg_password.setVisibility(View.GONE);
            reg_username.setVisibility(View.GONE);
        }

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onClick(final View view) {
        switch (view.getId()) {
            case R.id.reg_login: {
                finish();
            }
            break;
            case R.id.reg_button: {

                String password = reg_password.getText().toString();
                String nickname = reg_username.getText().toString();
                String phone = reg_phone.getText().toString();
                String code = reg_code.getText().toString();

//                if (phone.length() != 11) {
//                    tipDialog = new QMUITipDialog.Builder(RegActivity.this)
//                            .setIconType(QMUITipDialog.Builder.ICON_TYPE_FAIL)
//                            .setTipWord("请输入正确的手机号码!")
//                            .create();
//                    tipDialog.show();
//
//                    view.postDelayed(new Runnable() {
//                        @Override
//                        public void run() {
//                            tipDialog.dismiss();
//                        }
//                    }, 2000);
//                    return;
//
//                } else

                  if (password.length() < 6) {
                    tipDialog = new QMUITipDialog.Builder(RegActivity.this)
                            .setIconType(QMUITipDialog.Builder.ICON_TYPE_FAIL)
                            .setTipWord("请输入长度大于6的密码!")
                            .create();
                    tipDialog.show();

                    view.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            tipDialog.dismiss();
                        }
                    }, 2000);
                    return;
                } else if (!isWXLogin && nickname.length() <= 0) {
                    tipDialog = new QMUITipDialog.Builder(RegActivity.this)
                            .setIconType(QMUITipDialog.Builder.ICON_TYPE_FAIL)
                            .setTipWord("请填写一个昵称!")
                            .create();
                    tipDialog.show();

                    view.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            tipDialog.dismiss();
                        }
                    }, 2000);
                    return;
                } else if (reg_tuijiancode.length() <= 0) {

                    tipDialog = new QMUITipDialog.Builder(RegActivity.this)
                        .setIconType(QMUITipDialog.Builder.ICON_TYPE_FAIL)
                        .setTipWord("请填写推荐码!")
                        .create();
                    tipDialog.show();

                    view.postDelayed(new Runnable() {
                        @Override
                       public void run() {
                        tipDialog.dismiss();
                        }
                    }, 2000);
                    return;

               }else {
                    tipDialog = new QMUITipDialog.Builder(this)
                            .setIconType(QMUITipDialog.Builder.ICON_TYPE_LOADING)
                            .setTipWord("正在提交")
                            .create();
                    tipDialog.show();
                }

                if(isWXLogin)
                {
                    password = CommonUtil.md5(password).toLowerCase();
                    ApiAction apiAction = new ApiAction(this);
                    apiAction.bingWeiXinAccount(openid, phone, code, reg_tuijiancode.getText().toString(),password,new BaseAction.ResultCallback<String>() {
                        @Override
                        public void onSuccess(String s) {

                            com.alibaba.fastjson.JSONObject jsonObject= JSON.parseObject(s);
                            int code=jsonObject.getIntValue("code");
                            if(code==200)
                            {
                                //调用登录方法
                                LoginActivity.getIns().outLogin(jsonObject);
                                LoginActivity.getIns().onWXLoadClose();
                                finish();
                            }
                            else if(code==100)
                            {
                                tipDialog = new QMUITipDialog.Builder(RegActivity.this)
                                        .setIconType(QMUITipDialog.Builder.ICON_TYPE_FAIL)
                                        .setTipWord("绑定失败!")
                                        .create();
                                tipDialog.show();

                                view.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        tipDialog.dismiss();
                                    }
                                }, 2000);
                            }
                        }
                        @Override
                        public void onError(String errString) {
                        }
                    });


                }
                else {
                    password = CommonUtil.md5(password).toLowerCase();
                    ApiAction apiAction = new ApiAction(this);
                    apiAction.UserReg(nickname, phone, password, reg_code.getText().toString(),reg_tuijiancode.getText().toString(), new BaseAction.ResultCallback<String>() {
                        @Override
                        public void onSuccess(String s) {
                            try {
                                JSONObject jsonObject = new JSONObject(s);
                                int code = jsonObject.getInt("code");
                                String msg = jsonObject.getString("message");
                                if (code == 200) {
                                    tipDialog.dismiss();
                                    tipDialog = new QMUITipDialog.Builder(RegActivity.this)
                                            .setIconType(QMUITipDialog.Builder.ICON_TYPE_SUCCESS)
                                            .setTipWord("注册成功!")
                                            .create();
                                    tipDialog.show();

                                    view.postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            tipDialog.dismiss();
                                            finish();
                                        }
                                    }, 2000);

                                } else if (code == 201) {
                                    tipDialog.dismiss();
                                    tipDialog = new QMUITipDialog.Builder(RegActivity.this)
                                            .setIconType(QMUITipDialog.Builder.ICON_TYPE_FAIL)
                                            .setTipWord(msg)
                                            .create();
                                    tipDialog.show();

                                    view.postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            tipDialog.dismiss();
                                        }
                                    }, 2000);
                                } else {
                                    tipDialog.dismiss();
                                    tipDialog = new QMUITipDialog.Builder(RegActivity.this)
                                            .setIconType(QMUITipDialog.Builder.ICON_TYPE_FAIL)
                                            .setTipWord(msg)
                                            .create();
                                    tipDialog.show();

                                    view.postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            tipDialog.dismiss();
                                        }
                                    }, 2000);
                                }
                            } catch (Exception ee) {
                            }
                        }

                        @Override
                        public void onError(String errString) {

                        }
                    });
                }
            }
            break;
        }

    }
}
