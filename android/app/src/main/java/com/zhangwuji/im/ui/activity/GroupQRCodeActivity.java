package com.zhangwuji.im.ui.activity;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.zhangwuji.im.DB.DBInterface;
import com.zhangwuji.im.DB.entity.Group;
import com.zhangwuji.im.DB.entity.PeerEntity;
import com.zhangwuji.im.DB.entity.User;
import com.zhangwuji.im.R;
import com.zhangwuji.im.config.IntentConstant;
import com.zhangwuji.im.config.SysConstant;
import com.zhangwuji.im.imcore.service.IMService;
import com.zhangwuji.im.imcore.service.IMServiceConnector;
import com.zhangwuji.im.ui.base.TTBaseActivity;
import com.zhangwuji.im.ui.fragment.GroupManagerFragment;
import com.zhangwuji.im.ui.helper.LoginInfoSp;
import com.zhangwuji.im.ui.helper.ZxingUtil;
import com.zhangwuji.im.ui.widget.IMGroupAvatar;
import com.zhangwuji.im.utils.AvatarGenerate;
import com.zhangwuji.im.utils.ImageLoaderUtil;
import com.zhangwuji.im.utils.ScreenUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class GroupQRCodeActivity extends TTBaseActivity {

    private String key_qrcode;
    private String curSessionKey;
    private IMService imService;
    private PeerEntity peerEntity;

    private IMServiceConnector imServiceConnector = new IMServiceConnector(){
        @Override
        public void onServiceDisconnected() {
        }

        @Override
        public void onIMServiceConnected() {
            logger.d("groupmgr#onIMServiceConnected");
            imService = imServiceConnector.getIMService();
            if(imService == null){
                Toast.makeText(GroupQRCodeActivity.this,
                        getResources().getString(R.string.im_service_disconnected), Toast.LENGTH_SHORT).show();
                return;
            }
            initView();
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        imServiceConnector.connect(this);
        //设置最亮
        ScreenUtil.setBrightness(this, WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_FULL);
        LayoutInflater.from(this).inflate(R.layout.activity_group_qrcode, topContentView);
    }

    private void initView() {

        curSessionKey =  getIntent().getStringExtra(IntentConstant.KEY_SESSION_KEY);
        peerEntity = imService.getSessionManager().findPeerEntity(curSessionKey);
        Group groupEntity = (Group) peerEntity;

        key_qrcode = "cloudtalk-groupid:"+groupEntity.getId();

        ImageView iv_qrcode = (ImageView) findViewById(R.id.iv_qrcode);
        TextView tv_groupname = (TextView) findViewById(R.id.tv_groupname);
        IMGroupAvatar iv_groupimg = (IMGroupAvatar) findViewById(R.id.iv_groupimg);
        TextView tv_groupcount = (TextView) findViewById(R.id.tv_groupcount);

        if (key_qrcode != null && key_qrcode.length() > 0) {
            Bitmap qRcodeImage = ZxingUtil.createQRcodeImage("QRCode:" + key_qrcode, 200, 200);

            iv_qrcode.setImageBitmap(qRcodeImage);
        }
        // 初始化数据库

        tv_groupname.setText(groupEntity.getMainName());
        tv_groupcount.setText("("+groupEntity.getUserCnt()+")");

       // ImageLoaderUtil.getImageLoaderInstance().displayImage(groupEntity.getAvatar(), iv_groupimg);

        List<String> avatarUrlList = new ArrayList<>();
        Set<Integer> userIds = groupEntity.getlistGroupMemberIds();
        int i = 0;
        ArrayList<Integer> userId = new ArrayList<>();
        for(Integer buddyId:userIds){
            User entity = imService.getContactManager().findContact(buddyId);
            if (entity == null) {
                userId.add(buddyId);
                continue;
            }
            avatarUrlList.add(AvatarGenerate.generateAvatar(entity.getAvatar(),entity.getMainName(),entity.getPeerId()+""));
            if (i >= 3) {
                break;
            }
            i++;
        }
        setGroupAvatar(iv_groupimg,avatarUrlList);

        setTitle("群二维码");
        setLeftText("返回");
        setLeftButton(R.drawable.ac_back_icon, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
    private void setGroupAvatar(IMGroupAvatar avatar, List<String> avatarUrlList){
        try {
            avatar.setViewSize(ScreenUtil.instance(this).dip2px(38));
            avatar.setChildCorner(2);
            avatar.setAvatarUrlAppend(SysConstant.AVATAR_APPEND_32);
            avatar.setParentPadding(3);
            avatar.setAvatarUrls((ArrayList<String>) avatarUrlList);
        }catch (Exception e){
        }
    }

    @Override
    protected void onDestroy() {
        ////恢复之前亮度
        ScreenUtil.setBrightness(this, WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE);
        super.onDestroy();
    }
}
