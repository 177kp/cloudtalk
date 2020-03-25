package com.zhangwuji.im.ui.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.qmuiteam.qmui.widget.dialog.QMUITipDialog;
import com.zhangwuji.im.R;
import com.zhangwuji.im.imcore.manager.IMContactManager;
import com.zhangwuji.im.protobuf.helper.EntityChangeEngine;
import com.zhangwuji.im.server.network.BaseAction;
import com.zhangwuji.im.server.network.IMAction;
import com.zhangwuji.im.ui.base.TTBaseActivity;
import com.zhangwuji.im.ui.entity.NearByUser;
import com.zhangwuji.im.ui.helper.ApiAction;
import com.zhangwuji.im.ui.helper.IMUIHelper;
import com.zhangwuji.im.ui.widget.IMBaseImageView;
import com.zhangwuji.im.ui.widget.SecurityPasswordEditText;
import com.zhangwuji.im.utils.AvatarGenerate;
import com.zhangwuji.im.utils.CommonUtil;

import org.json.JSONException;

/**
 * Created by AMing on 16/3/8.
 * YuChen
 */
public class SendRedPacketActivity extends Activity implements View.OnClickListener{


    private ApiAction apiAction=null;
    private NearByUser userinfo=new NearByUser();
    private    Button bt_addfriend, bt_chats;
    private  QMUITipDialog tipDialog;


    private EditText et_money,et_detail,et_number;
    private TextView tv_money;
    private Button bt_sendmoney,bt_fanhui;
    private String payPwd = "";
    private SecurityPasswordEditText editText_Pwd;
    private boolean isgroup = false,isspellluck = true;
    private LinearLayout ll_one,ll_hongbaonumber;
    private TextView tv_allnumber,tv_one,tv_changehongbao,tv_two;
    private String type = "0",allnum = "0";
    private String uid;
    protected IMAction action;
    private  String targetId="",info;
    public static final  int SENDHONGBAOOK = 1;
    private int allmembercount=0;
    private int type2=1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.sendredpackage_layout);

        action = new IMAction(this);

        Intent intent = getIntent();
        isgroup = intent.getBooleanExtra("isgroup", false);
        targetId=intent.getStringExtra("targetId");

        et_money = (EditText) findViewById(R.id.et_money);
        et_detail = (EditText) findViewById(R.id.et_detail);
        tv_money = (TextView) findViewById(R.id.tv_money);
        bt_fanhui = (Button) findViewById(R.id.bt_fanhui);

        bt_fanhui.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                finish();
            }
        });
        bt_sendmoney = (Button) findViewById(R.id.bt_sendmoney);
        if(isgroup){
            type2=2;
            ll_one = (LinearLayout) findViewById(R.id.ll_one);
            ll_one.setVisibility(View.VISIBLE);
            ll_hongbaonumber = (LinearLayout) findViewById(R.id.ll_hongbaonumber);
            ll_hongbaonumber.setVisibility(View.VISIBLE);
            tv_allnumber = (TextView) findViewById(R.id.tv_allnumber);
            tv_allnumber.setVisibility(View.VISIBLE);
            tv_one = (TextView) findViewById(R.id.tv_one);
            tv_one.setVisibility(View.VISIBLE);
            tv_changehongbao = (TextView) findViewById(R.id.tv_changehongbao);
            tv_changehongbao.setVisibility(View.GONE);
            tv_two = (TextView) findViewById(R.id.tv_two);
            tv_two.setText(R.string.hongbao_allmoney);
            et_number = (EditText) findViewById(R.id.et_number);
            tv_changehongbao.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    // TODO Auto-generated method stub
                    if(isspellluck){
                        tv_two.setText(R.string.hongbao_sendhongbao_txt1);
                        tv_one.setText(getString(R.string.hongbao_sendhongbao_txt2)+"，");
                        tv_changehongbao.setText(R.string.hongbao_sendhongbao_txt3);
                        isspellluck = false;
                    }else{
                        tv_two.setText(R.string.hongbao_allmoney);
                        tv_one.setText(getString(R.string.hongbao_sendhongbao_txt4)+"，");
                        tv_changehongbao.setText(R.string.hongbao_sendhongbao_txt5);
                        isspellluck = true;
                    }
                }
            });

            if(isgroup)
            {
               // getGroups();
            }



            et_number.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    et_number.setFocusable(true);
                    et_number.setFocusableInTouchMode(true);
                    et_number.setSelection(et_number.getText().length());
                    et_number.setSelectAllOnFocus(true);
                }
            });



            et_number.addTextChangedListener(new TextWatcher() {
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    // TODO Auto-generated method stub
                    if (!s.toString().equals("")) {

                        try
                        {
                            int sendnum=Integer.parseInt(s.toString());
                            if(allmembercount>0 && sendnum>allmembercount)
                            {
                                Toast.makeText(SendRedPacketActivity.this,"本群最多可发"+allmembercount+"个红包!", Toast.LENGTH_SHORT).show();
                                return;
                            }
                        }catch(Exception e){}

                    }
                }
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count,
                                              int after) {
                    // TODO Auto-generated method stub

                }

                @Override
                public void afterTextChanged(Editable s) {
                    // TODO Auto-generated method stub

                }
            });
        }

        tipDialog = new QMUITipDialog.Builder(this)
                .setIconType(QMUITipDialog.Builder.ICON_TYPE_LOADING)
                .setTipWord("正在提交")
                .create();

        et_money.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                et_money.setFocusable(true);
                et_money.setFocusableInTouchMode(true);
                et_money.setSelection(et_money.getText().length());
                et_money.setSelectAllOnFocus(true);

            }
        });

        et_money.setOnFocusChangeListener(new View.OnFocusChangeListener()
        {
            //v 发生变化的视图    hasFocus:用来判断视图是否获得了焦点
            public void onFocusChange(View v,boolean hasFocus)
            {
                EditText _v = (EditText)v;
                if(hasFocus)
                {
                    et_money.setFocusable(true);
                    et_money.setFocusableInTouchMode(true);
                    et_money.setSelection(et_money.getText().length());
                    et_money.setSelectAllOnFocus(true);
                }
            }
        });


        bt_sendmoney.setEnabled(false);

        et_money.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // TODO Auto-generated method stub
                if (s.toString().contains(".")) {
                    if (s.length() - 1 - s.toString().indexOf(".") > 2) {
                        s = s.toString().subSequence(0,
                                s.toString().indexOf(".") + 3);
                        et_money.setText(s);
                        et_money.setSelection(s.length());
                    }
                }
                if (s.toString().trim().substring(0).equals(".")) {
                    s = "0" + s;
                    et_money.setText(s);
                    et_money.setSelection(2);
                }
                if (s.toString().startsWith("0") && s.toString().trim().length() > 1) {
                    if (!s.toString().substring(1, 2).equals(".")) {
                        et_money.setText(s.subSequence(0, 1));
                        et_money.setSelection(1);
                        return;
                    }
                }

                if(!et_money.getText().toString().equals("")){
//					if(!isspellluck && isgroup) {
//						int summoney = Integer.parseInt(et_money.getText().toString()) * Integer.parseInt(et_number.getText().toString());
//						tv_money.setText("￥ " + summoney);
//					}else{
                    tv_money.setText("￥ " + et_money.getText().toString());

                    Float theMoney=Float.parseFloat(et_money.getText().toString());

                    if(theMoney>0 && theMoney<=200)
                    {
                        bt_sendmoney.setBackgroundResource(R.drawable.tt_red_round_button);
                        bt_sendmoney.setEnabled(true);
                    }
                    else if(theMoney>200 && !isgroup)
                    {
                        Toast toast= Toast.makeText(getApplicationContext(),
                                getString(R.string.hongbao_sendhongbao_txt6), Toast.LENGTH_SHORT);
                        toast.setGravity(Gravity.CENTER, 0, 0);
                        toast.show();

                        bt_sendmoney.setBackgroundResource(R.drawable.tt_red_round_button2);
                        bt_sendmoney.setEnabled(false);
                    }
                    else
                    {
                        bt_sendmoney.setBackgroundResource(R.drawable.tt_red_round_button);
                        bt_sendmoney.setEnabled(true);
                    }

                }else{
                    tv_money.setText("￥ 0.00");
                }

            }
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {
                // TODO Auto-generated method stub

            }

            @Override
            public void afterTextChanged(Editable s) {
                // TODO Auto-generated method stub

            }
        });



        bt_sendmoney.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                if(et_money.getText().toString().equals("")||et_money.getText().toString().equals("0.00")||et_money.getText().toString().equals("0.0")||et_money.getText().toString().equals("0.")||et_money.getText().toString().equals("0")){
                    Toast.makeText(SendRedPacketActivity.this,
                            getString(R.string.hongbao_sendhongbao_txt7), Toast.LENGTH_SHORT).show();
                }else{
                    if(isgroup){
                        if(et_number.getText().toString().equals("")){
                            Toast.makeText(SendRedPacketActivity.this,
                                    getString(R.string.hongbao_sendhongbao_txt8), Toast.LENGTH_SHORT).show();
                            return;
                        }
                        else
                        {
                            try
                            {
                                int sendnum=Integer.parseInt(et_number.getText().toString());
                                if(allmembercount>0 && sendnum>allmembercount)
                                {
                                    Toast.makeText(SendRedPacketActivity.this,"本群最多可发"+allmembercount+"个红包!", Toast.LENGTH_SHORT).show();
                                    return;
                                }
                            }catch(Exception e){}
                        }
                    }
                    else
                    {
                        Float theMoney=Float.parseFloat(et_money.getText().toString());
                        if(theMoney>200)
                        {
                            Toast toast= Toast.makeText(getApplicationContext(),
                                    getString(R.string.hongbao_sendhongbao_txt6), Toast.LENGTH_SHORT);
                            toast.setGravity(Gravity.CENTER, 0, 0);
                            toast.show();
                            return;
                        }
                    }
                    showPwdAlert();
                }
            }
        });

    }

    private void initData() {

    }

    @Override
    public void onClick(final View view) {
        final int id = view.getId();
        switch (id) {
            case R.id.left_btn:
            case R.id.left_txt:
                finish();
                break;
            case R.id.bt_chats:
            {
                IMUIHelper.openChatActivity(this, EntityChangeEngine.getSessionKey(userinfo.getId(),1));
                finish();
            }break;
        }
    }

    private void showPwdAlert() {
        final AlertDialog dlg = new AlertDialog.Builder(this).create();
        dlg.setCanceledOnTouchOutside(false);
        dlg.show();
        Window window = dlg.getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                | WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        window.setContentView(R.layout.starcard_query_pwdvalidate_layout);
        Button ok = (Button) window.findViewById(R.id.button_Query);
        Button cancel = (Button) window.findViewById(R.id.button1_Cancel);
        ok.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                payPwd = editText_Pwd.getInputnumber();
                if (payPwd == null || payPwd.equals("") || payPwd.length() < 6) {

                    Toast.makeText(SendRedPacketActivity.this,
                            getString(R.string.pay_paypasswordtip1), Toast.LENGTH_SHORT).show();

                } else {
                    dlg.dismiss();
                    sendhongbao(payPwd);
                }
            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                dlg.dismiss();
            }
        });

        editText_Pwd = (SecurityPasswordEditText) window
                .findViewById(R.id.password);
        ((TextView) window.findViewById(R.id.textView_TopTitle))
                .setText(R.string.pay_paypassword);
        editText_Pwd.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    dlg.getWindow()
                            .setSoftInputMode(
                                    WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
                }
            }
        });
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);

    }

    public void sendhongbao(String payPwd)
    {


        int sendnum=1;

        if(isgroup) {
            sendnum=Integer.parseInt(et_number.getText().toString());
        }
        Float theMoney=Float.parseFloat(et_money.getText().toString());

        info=et_detail.getText().toString();
        if(info.equals(""))
        {
            info="恭喜发财，大吉大利！";
        }

        tipDialog = new QMUITipDialog.Builder(this)
                .setIconType(QMUITipDialog.Builder.ICON_TYPE_LOADING)
                .setTipWord("正在提交")
                .create();
        tipDialog.show();

        apiAction=new ApiAction(this);
        payPwd= CommonUtil.md5(payPwd).toLowerCase();
        apiAction.sendRedPacket(payPwd, type2==1?1:0, type2, theMoney, sendnum, targetId, info, new BaseAction.ResultCallback<String>() {
            @Override
            public void onSuccess(String s) {

                org.json.JSONObject jsonObject=null;
                int code= 0;
                try {
                    jsonObject = new org.json.JSONObject(s);
                    code = jsonObject.getInt("code");
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                if(code==200)
                {

                    int pid=0;
                    try {
                        org.json.JSONObject data=jsonObject.getJSONObject("data");
                        pid=data.getInt("pid");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    Intent intent2 = new Intent();
                    intent2.putExtra("pid", pid);
                    intent2.putExtra("msg", info);
                    intent2.putExtra("groupid", targetId);

                    setResult(Activity.RESULT_OK , intent2);
                    finish();

                }
                else if(code==201)
                {
                    tipDialog.dismiss();
                    tipDialog = new QMUITipDialog.Builder(SendRedPacketActivity.this)
                            .setIconType(QMUITipDialog.Builder.ICON_TYPE_FAIL)
                            .setTipWord("没有设置支付密码!")
                            .create();
                    tipDialog.show();
                    bt_sendmoney.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            tipDialog.dismiss();
                        }
                    }, 2000);
                }
                else if(code==202)
                {
                    tipDialog.dismiss();
                    tipDialog = new QMUITipDialog.Builder(SendRedPacketActivity.this)
                            .setIconType(QMUITipDialog.Builder.ICON_TYPE_FAIL)
                            .setTipWord("支付密码校验失败!")
                            .create();
                    tipDialog.show();
                    bt_sendmoney.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            tipDialog.dismiss();
                        }
                    }, 2000);
                }
                else if(code==203)
                {
                    tipDialog.dismiss();
                    tipDialog = new QMUITipDialog.Builder(SendRedPacketActivity.this)
                            .setIconType(QMUITipDialog.Builder.ICON_TYPE_FAIL)
                            .setTipWord("余额不足!")
                            .create();
                    tipDialog.show();
                    bt_sendmoney.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            tipDialog.dismiss();
                        }
                    }, 2000);
                }
                else
                {
                    tipDialog.dismiss();
                    tipDialog = new QMUITipDialog.Builder(SendRedPacketActivity.this)
                            .setIconType(QMUITipDialog.Builder.ICON_TYPE_FAIL)
                            .setTipWord("红包发送失败!!")
                            .create();
                    tipDialog.show();
                    bt_sendmoney.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            tipDialog.dismiss();
                        }
                    }, 2000);
                }

            }
            @Override
            public void onError(String errString) {
                tipDialog.dismiss();
                tipDialog = new QMUITipDialog.Builder(SendRedPacketActivity.this)
                        .setIconType(QMUITipDialog.Builder.ICON_TYPE_FAIL)
                        .setTipWord("红包发送失败!!")
                        .create();
                tipDialog.show();

                bt_sendmoney.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        tipDialog.dismiss();
                    }
                }, 2000);
            }
        });

    }


    @Override
    public void onBackPressed() {
        finish();
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }


}
