package com.zhangwuji.im.ui.activity;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.zhangwuji.im.DB.DBInterface;
import com.zhangwuji.im.DB.entity.User;
import com.zhangwuji.im.R;
import com.zhangwuji.im.config.IntentConstant;
import com.zhangwuji.im.imcore.service.IMService;
import com.zhangwuji.im.imcore.service.IMServiceConnector;
import com.zhangwuji.im.server.network.BaseAction;
import com.zhangwuji.im.server.network.IMAction;
import com.zhangwuji.im.ui.base.TTBaseActivity;
import com.zhangwuji.im.ui.helper.LoginInfoSp;
import com.zhangwuji.im.ui.helper.ZxingUtil;
import com.zhangwuji.im.utils.ImageLoaderUtil;
import com.zhangwuji.im.utils.ScreenUtil;

public class QRCodeActivity extends TTBaseActivity {

    private String key_qrcode;
    private TextView my_tuijian_code;
    private IMService imService;
    private User currentUser;

    private IMServiceConnector imServiceConnector = new IMServiceConnector(){
        @Override
        public void onIMServiceConnected() {
            logger.d("detail#onIMServiceConnected");

            imService = imServiceConnector.getIMService();
            if (imService == null) {
                logger.e("detail#imService is null");
                return;
            }
            initView();
        }
        @Override
        public void onServiceDisconnected() {}
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        imServiceConnector.connect(this);
        //设置最亮
        ScreenUtil.setBrightness(this, WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_FULL);
        LayoutInflater.from(this).inflate(R.layout.activity_qrcode, topContentView);
        key_qrcode = getIntent().getStringExtra(IntentConstant.KEY_QRCODE);
    }

    private void initView() {
        ImageView iv_qrcode = (ImageView) findViewById(R.id.iv_qrcode);
        TextView tv_nickname = (TextView) findViewById(R.id.tv_nickname);
        ImageView iv_person = (ImageView) findViewById(R.id.iv_person);
        my_tuijian_code=findViewById(R.id.my_tuijian_code);


        if (key_qrcode != null && key_qrcode.length() > 0) {
            Bitmap qRcodeImage = ZxingUtil.createQRcodeImage("QRCode:" + key_qrcode, 200, 200);

            iv_qrcode.setImageBitmap(qRcodeImage);
        }
        // 初始化数据库
        User loginEntity =imService.getLoginManager().getLoginInfo();
        tv_nickname.setText(loginEntity.getMainName());

        ImageLoaderUtil.getImageLoaderInstance().displayImage(loginEntity.getAvatar(), iv_person);

        setLeftText("二维码名片");
        setLeftButton(R.drawable.ac_back_icon, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        IMAction apiAction=new IMAction(this);
        apiAction.getUserInfo(loginEntity.getPeerId()+"", new BaseAction.ResultCallback<String>() {
            @Override
            public void onSuccess(String s) {
                com.alibaba.fastjson.JSONObject object= JSON.parseObject(s);
                com.alibaba.fastjson.JSONObject data=object.getJSONObject("data");
                com.alibaba.fastjson.JSONObject userinfo= (com.alibaba.fastjson.JSONObject)data.getJSONArray("userinfo").get(0);
                my_tuijian_code.setText("我的推荐码: "+userinfo.getString("code"));
            }

            @Override
            public void onError(String errString) {

            }
        });

    }


    @Override
    protected void onDestroy() {
        ////恢复之前亮度
        ScreenUtil.setBrightness(this, WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE);
        super.onDestroy();
    }
}
