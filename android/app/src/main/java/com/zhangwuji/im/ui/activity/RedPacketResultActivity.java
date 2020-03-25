package com.zhangwuji.im.ui.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.qmuiteam.qmui.widget.dialog.QMUITipDialog;
import com.zhangwuji.im.DB.entity.Message;
import com.zhangwuji.im.DB.entity.User;
import com.zhangwuji.im.R;
import com.zhangwuji.im.imcore.service.IMService;
import com.zhangwuji.im.imcore.service.IMServiceConnector;
import com.zhangwuji.im.protobuf.helper.EntityChangeEngine;
import com.zhangwuji.im.server.network.BaseAction;
import com.zhangwuji.im.server.network.IMAction;
import com.zhangwuji.im.ui.adapter.RedPacketdetailsAdapter;
import com.zhangwuji.im.ui.adapter.RedPacketdetailsEntity;
import com.zhangwuji.im.ui.entity.NearByUser;
import com.zhangwuji.im.ui.entity.RedPacketLogBean;
import com.zhangwuji.im.ui.helper.ApiAction;
import com.zhangwuji.im.ui.helper.IMUIHelper;
import com.zhangwuji.im.ui.plugin.message.entity.RedPacketMessage;
import com.zhangwuji.im.ui.widget.IMBaseImageView;
import com.zhangwuji.im.ui.widget.SecurityPasswordEditText;
import com.zhangwuji.im.ui.widget.xlistview.PullDownView;
import com.zhangwuji.im.utils.AvatarGenerate;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by AMing on 16/3/8.
 * YuChen
 */
public class RedPacketResultActivity extends Activity implements View.OnClickListener{

    private  QMUITipDialog tipDialog;
    private ApiAction apiAction=null;
    private IMBaseImageView iv_touxiang;
    private LinearLayout bt_fanhui;
    private TextView tv_money,tv_name,tv_detail,tv_two,tv_pin,tv_hongbaocount,tv_lookrecord;
    private boolean isoneself,isshowshouqi=false;
    private String money = "" ,type = "" ,type2 = "";
    private PullDownView mPullDownView;
    private RedPacketdetailsAdapter mAdpater;
    private int page = 1;
    private List<RedPacketLogBean> mlList = new ArrayList<RedPacketLogBean>();
    private View v_one;
    private LinearLayout ll_hongbaocount;
    private RedPacketMessage message;
    private User user;
    private IMService imService;

    private IMServiceConnector imServiceConnector = new IMServiceConnector() {
        @Override
        public void onIMServiceConnected() {
            logger.d("contactUI#onIMServiceConnected");

            imService = imServiceConnector.getIMService();
            if (imService == null) {
                logger.e("ContactFragment#onIMServiceConnected# imservice is null!!");
                return;
            }
            gethongbaoinfo();
        }

        @Override
        public void onServiceDisconnected() {
            if (EventBus.getDefault().isRegistered(RedPacketResultActivity.this)) {
                EventBus.getDefault().unregister(RedPacketResultActivity.this);
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.redpacketresultlayout);
        Intent intent = getIntent();

        isoneself = intent.getBooleanExtra("isoneself", true);
        money = intent.getStringExtra("getmoney");
        message=(RedPacketMessage)intent.getSerializableExtra("messsage");
        String userJson = intent.getStringExtra("user");
        user=JSON.parseObject(userJson,User.class);

        tipDialog = new QMUITipDialog.Builder(this)
                .setIconType(QMUITipDialog.Builder.ICON_TYPE_LOADING)
                .setTipWord("加载中...")
                .create();
        tipDialog.show();

        apiAction=new ApiAction(this);
        imServiceConnector.connect(this);

        tv_name = (TextView) findViewById(R.id.tv_name);
        tv_pin = (TextView) findViewById(R.id.tv_pin);
        bt_fanhui = (LinearLayout) findViewById(R.id.bt_fanhui);
        tv_money = (TextView) findViewById(R.id.tv_money);
        tv_detail = (TextView) findViewById(R.id.tv_detail);
        tv_two = (TextView) findViewById(R.id.tv_two);
        tv_lookrecord = (TextView) findViewById(R.id.tv_lookrecord);
        v_one = findViewById(R.id.v_one);
        ll_hongbaocount = (LinearLayout) findViewById(R.id.ll_hongbaocount);
        tv_hongbaocount = (TextView) findViewById(R.id.tv_hongbaocount);


        if(money.equals("")){
            tv_money.setVisibility(View.GONE);
            tv_two.setVisibility(View.GONE);

        }else{
            tv_money.setText(money);
            tv_two.setVisibility(View.VISIBLE);
        }

        if(isoneself){
            tv_money.setVisibility(View.GONE);
            tv_two.setVisibility(View.GONE);
        }

        iv_touxiang = (IMBaseImageView) findViewById(R.id.iv_touxiang);
        //头像设置
        iv_touxiang.setCorner(8);
        iv_touxiang.setImageUrl(AvatarGenerate.generateAvatar(user.getAvatar(),user.getMainName(),user.getPeerId()+""));


        tv_detail.setText(message.getInfo());
        tv_name.setText(user.getMainName()+getString(R.string.hongbao_reshongbao_txt1));

        mPullDownView = (PullDownView) findViewById(R.id.pull_down_view);
        mPullDownView.initBottomView();
        mPullDownView.setFocusable(false);


        mAdpater = new RedPacketdetailsAdapter(this, R.layout.redpacket_item_layout, mlList,isshowshouqi);
        mPullDownView.setAdapter(mAdpater);
        mPullDownView.setMyPullUpListViewCallBack(new PullDownView.MyPullUpListViewCallBack() {
            @Override
            public void scrollBottomState() {
                // TODO Auto-generated method stub
                page++;
                //getdata();
            }
        });

        bt_fanhui.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                finish();
            }
        });

        tv_lookrecord.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                //Intent intent = new Intent(RedPacketResultActivity.this, RedPacketRecordActivity.class);
                //startActivity(intent);
            }
        });

        try
        {
            IMUIHelper.setListViewHeightBasedOnChildren(mPullDownView);
        }catch(Exception e){}

    }

    public void gethongbaoinfo(){
        try {

             apiAction.getRedPacketInfo(message.getId() + "", new BaseAction.ResultCallback<String>() {
                @Override
                public void onSuccess(String s) {

                    JSONObject dataJson = JSON.parseObject(s);
                    int code = dataJson.getIntValue("code");
                    if(code==200){
                        JSONObject data = dataJson.getJSONObject("data").getJSONObject("redpacketinfo");
                        type2 = data.getString("type2");
                        type = data.getString("type");
                        String allnum = data.getString("allnum");
                        String usenum = data.getString("usenum");
                        String status=data.getString("status");
                        String senduid=data.getString("senduid");

                        String allmoney = data.getString("allmoney");
                        String usemoney = data.getString("usemoney");

                        if(allnum==usenum)
                        {
                            isshowshouqi=true;
                        }
                        else
                        {
                            isshowshouqi=false;

                            if(type2.equals("1")){
                                tv_money.setVisibility(View.VISIBLE);
                                tv_money.setText(allmoney+"元");
                            }
                        }

                        if(type2.equals("2")){
                            tv_pin.setVisibility(View.VISIBLE);
                        }
                        if(type.equals("0")){
                            if(allnum.equals(usenum)){
                                ll_hongbaocount.setVisibility(View.VISIBLE);
                                tv_hongbaocount.setText(allnum+getString(R.string.hongbao_reshongbao_txt3));
                                if(senduid.equals(imService.getLoginManager().getLoginId()))//自已发的红包
                                {
                                    tv_hongbaocount.setText(allnum+getString(R.string.hongbao_reshongbao_ge)+"红包, 共"+allmoney+"元,已被抢光。");
                                }
                            }else{
                                ll_hongbaocount.setVisibility(View.VISIBLE);
                                if(senduid.equals(imService.getLoginManager().getLoginId()))//自已发的红包
                                {
                                    tv_hongbaocount.setText(getString(R.string.hongbao_reshongbao_txt4)+usenum+"/"+allnum+getString(R.string.hongbao_reshongbao_ge)+", 共"+usemoney+"/"+allmoney+"元。");
                                }
                                else
                                {
                                    tv_hongbaocount.setText(getString(R.string.hongbao_reshongbao_txt4)+usenum+"/"+allnum+getString(R.string.hongbao_reshongbao_ge));
                                }
                            }
                        }
                        else
                        {
                            ll_hongbaocount.setVisibility(View.GONE);

                        }
                        getdata();
                    }else{
                        Toast.makeText(RedPacketResultActivity.this,
                                getString(R.string.hongbao_sendhongbao_txt9), Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onError(String errString) {
                    tipDialog.dismiss();
                }
            });



        } catch (Exception e) {
            tipDialog.dismiss();
            // TODO: handle exception
            Toast.makeText(RedPacketResultActivity.this,
                    getString(R.string.hongbao_sendhongbao_txt9), Toast.LENGTH_SHORT).show();
        }
    }


    public void getdata()
    {
       apiAction.getRedPacketLog(message.getId()+"", new BaseAction.ResultCallback<String>() {
            @Override
            public void onSuccess(String requestcode) {
                try {
                    tipDialog.dismiss();
                    if(requestcode != null){
                        RedPacketLogBean dataEntity;
                        JSONObject dataJson = JSON.parseObject(requestcode);
                        int  code = dataJson.getIntValue("code");
                        if(code==200){
                            JSONArray data = dataJson.getJSONArray("data");
                            if(data != null){
                                for(int i=0;i<data.size();i++){

                                    dataEntity=(RedPacketLogBean)JSON.parseObject(data.get(i).toString(),RedPacketLogBean.class);
                                    mlList.add(dataEntity);
                                    v_one.setBackgroundResource(android.R.color.white);
                                }

                                if(data.size()<=0)
                                {
                                    tv_two.setText(R.string.hongbao_reshongbao_txt2);
                                    tv_two.setVisibility(View.VISIBLE);
                                    tv_hongbaocount.setVisibility(View.GONE);
                                }
                                else
                                {
                                    mAdpater = new RedPacketdetailsAdapter(RedPacketResultActivity.this, R.layout.redpacket_item_layout, mlList,isshowshouqi);
                                    mPullDownView.setAdapter(mAdpater);
                                    mAdpater.setShowShouQi(isshowshouqi);
                                    mAdpater.notifyDataSetChanged();
                                    try
                                    {
                                        IMUIHelper.setListViewHeightBasedOnChildren(mPullDownView);
                                    }catch(Exception e){}
                                }
                            }else{
                                tv_two.setText(R.string.hongbao_reshongbao_txt2);
                                tv_two.setVisibility(View.VISIBLE);
                            }
                        }else{
                        }
                    }else{
                        page = 1;
                    }
                } catch (Exception e) {}
            }
            @Override
            public void onError(String errString) {
                tipDialog.dismiss();
            }
        });
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
                finish();
            }break;
        }
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
