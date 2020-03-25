package com.zhangwuji.im.ui.activity;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.view.Window;
import android.widget.Toast;

import com.zhangwuji.im.R;
import com.zhangwuji.im.config.IntentConstant;
import com.zhangwuji.im.imcore.event.LoginEvent;
import com.zhangwuji.im.imcore.event.PriorityEvent;
import com.zhangwuji.im.imcore.event.UnreadEvent;
import com.zhangwuji.im.imcore.manager.IMContactManager;
import com.zhangwuji.im.imcore.service.IMService;
import com.zhangwuji.im.imcore.service.IMServiceConnector;
import com.zhangwuji.im.protobuf.IMSwitchService;
import com.zhangwuji.im.ui.entity.IMCallAction;
import com.zhangwuji.im.ui.fragment.ChatFragment;
import com.zhangwuji.im.ui.fragment.ContactFragment;
import com.zhangwuji.im.ui.helper.IMUIHelper;
import com.zhangwuji.im.ui.helper.UpdateManager;
import com.zhangwuji.im.ui.widget.NaviTabButton;
import com.zhangwuji.im.utils.Logger;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends FragmentActivity {
    private Fragment[] mFragments;
    private NaviTabButton[] mTabButtons;
    private Logger logger = Logger.getLogger(MainActivity.class);
    private IMService imService;
    SharedPreferences sharedPreferences;
    private IMServiceConnector imServiceConnector = new IMServiceConnector() {
        @Override
        public void onIMServiceConnected() {
            imService = imServiceConnector.getIMService();
        }

        @Override
        public void onServiceDisconnected() {
        }
    };
    //权限申请
    String[] allpermissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA,  Manifest.permission.RECORD_AUDIO};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedPreferences = getSharedPreferences("cloudtalk_im_data", this.MODE_PRIVATE);

        logger.d("MainActivity#savedInstanceState:%s", savedInstanceState);
        //todo eric when crash, this will be called, why?
        if (savedInstanceState != null) {
            logger.w("MainActivity#crashed and restarted, just exit");
            jumpToLoginPage();
            finish();
        }

        // 在这个地方加可能会有问题吧
        EventBus.getDefault().register(this);
        imServiceConnector.connect(this);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.tt_activity_main);

        initTab();
        initFragment();
        setFragmentIndicator(0);
        applypermission();
        loadData();
    }

    private void loadData() {
        checkUpdate();
    }

    /**
     * 判断版本更新
     */
    private void checkUpdate() {
        UpdateManager.getUpdateManager().checkAppUpdate(this, false, true, null);
    }

    @Override
    public void onBackPressed() {
        //don't let it exit
        //super.onBackPressed();

        //nonRoot	If false then this only works if the activity is the root of a task; if true it will work for any activity in a task.
        //document http://developer.android.com/reference/android/app/Activity.html

        //moveTaskToBack(true);

        Intent i = new Intent(Intent.ACTION_MAIN);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        i.addCategory(Intent.CATEGORY_HOME);
        startActivity(i);
    }


    private void initFragment() {
        mFragments = new Fragment[5];
        mFragments[0] = getSupportFragmentManager().findFragmentById(R.id.fragment_home);
        mFragments[1] = getSupportFragmentManager().findFragmentById(R.id.fragment_chat);
        mFragments[2] = getSupportFragmentManager().findFragmentById(R.id.fragment_contact);
        mFragments[3] = getSupportFragmentManager().findFragmentById(R.id.fragment_internal);
        mFragments[4] = getSupportFragmentManager().findFragmentById(R.id.fragment_my);
    }

    private void initTab() {
        mTabButtons = new NaviTabButton[5];

        mTabButtons[0] = (NaviTabButton) findViewById(R.id.tabbutton_home);
        mTabButtons[1] = (NaviTabButton) findViewById(R.id.tabbutton_chat);
        mTabButtons[2] = (NaviTabButton) findViewById(R.id.tabbutton_contact);
        mTabButtons[3] = (NaviTabButton) findViewById(R.id.tabbutton_internal);
        mTabButtons[4] = (NaviTabButton) findViewById(R.id.tabbutton_my);

        mTabButtons[0].setTitle("主页");
        mTabButtons[0].setIndex(0);
        mTabButtons[0].setSelectedImage(getResources().getDrawable(R.drawable.tt_tab_internal_select));
        mTabButtons[0].setUnselectedImage(getResources().getDrawable(R.drawable.tt_tab_internal_nor));

        mTabButtons[1].setTitle(getString(R.string.main_chat));
        mTabButtons[1].setIndex(1);
        mTabButtons[1].setSelectedImage(getResources().getDrawable(R.drawable.tt_tab_chat_sel));
        mTabButtons[1].setUnselectedImage(getResources().getDrawable(R.drawable.tt_tab_chat_nor));

        mTabButtons[2].setTitle(getString(R.string.main_contact));
        mTabButtons[2].setIndex(2);
        mTabButtons[2].setSelectedImage(getResources().getDrawable(R.drawable.tt_tab_contact_sel));
        mTabButtons[2].setUnselectedImage(getResources().getDrawable(R.drawable.tt_tab_contact_nor));

        mTabButtons[3].setTitle(getString(R.string.main_innernet));
        mTabButtons[3].setIndex(3);
        mTabButtons[3].setSelectedImage(getResources().getDrawable(R.drawable.tt_tab_internal_select));
        mTabButtons[3].setUnselectedImage(getResources().getDrawable(R.drawable.tt_tab_internal_nor));

        mTabButtons[4].setTitle(getString(R.string.main_me_tab));
        mTabButtons[4].setIndex(4);
        mTabButtons[4].setSelectedImage(getResources().getDrawable(R.drawable.tt_tab_me_sel));
        mTabButtons[4].setUnselectedImage(getResources().getDrawable(R.drawable.tt_tab_me_nor));

        //获取缓存通知计数
    }


    public void setFragmentIndicator(int which) {

        getSupportFragmentManager().beginTransaction().hide(mFragments[0]).hide(mFragments[1]).hide(mFragments[2]).hide(mFragments[3]).hide(mFragments[4]).show(mFragments[which]).commit();
        mTabButtons[0].setSelectedButton(false);
        mTabButtons[1].setSelectedButton(false);
        mTabButtons[2].setSelectedButton(false);
        mTabButtons[3].setSelectedButton(false);
        mTabButtons[4].setSelectedButton(false);

        mTabButtons[which].setSelectedButton(true);
    }

    public void setUnreadMessageCnt(int unreadCnt) {
        mTabButtons[1].setUnreadNotify(unreadCnt);
    }


    /**
     * 双击事件
     */
    public void chatDoubleListener() {
        setFragmentIndicator(1);
        ((ChatFragment) mFragments[1]).scrollToUnreadPosition();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleLocateDepratment(intent);
    }


    @Override
    protected void onResume() {
        super.onResume();
        int oldnum = IMUIHelper.getConfigNameAsInt(this, "newfriendinvite");
        if (oldnum > 0) {
            mTabButtons[2].setUnreadNotify(oldnum);
        } else {
            mTabButtons[2].setUnreadNotify(0);
        }
    }

    private void handleLocateDepratment(Intent intent) {
        int departmentIdToLocate = intent.getIntExtra(IntentConstant.KEY_LOCATE_DEPARTMENT, -1);
        if (departmentIdToLocate == -1) {
            return;
        }

        logger.d("department#got department to locate id:%d", departmentIdToLocate);
        setFragmentIndicator(1);
        ContactFragment fragment = (ContactFragment) mFragments[1];
        if (fragment == null) {
            logger.e("department#fragment is null");
            return;
        }
        fragment.locateDepartment(departmentIdToLocate);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        logger.d("mainactivity#onDestroy");
        EventBus.getDefault().unregister(this);
        imServiceConnector.disconnect(this);
        super.onDestroy();
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onUserEvent(UnreadEvent event) {
        switch (event.event) {
            case SESSION_READED_UNREAD_MSG:
            case UNREAD_MSG_LIST_OK:
            case UNREAD_MSG_RECEIVED:
                showUnreadMessageCount();
                break;
        }
    }

    private void showUnreadMessageCount() {
        //todo eric when to
        if (imService != null) {
            int unreadNum = imService.getUnReadMsgManager().getTotalUnreadCount();
            mTabButtons[1].setUnreadNotify(unreadNum);
        }

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onUserEvent(LoginEvent event) {
        switch (event) {
            case LOGIN_OUT:
                handleOnLogout();
                break;
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onUserEvent(PriorityEvent event) {
        switch (event.event) {
            case MSG_RECEIVED_VIDEO: {
                IMSwitchService.IMP2PCmdMsg p2pcmd = (IMSwitchService.IMP2PCmdMsg) event.object;
                String msgdata = p2pcmd.getCmdMsgData();
                try {
                    JSONObject jsonObject = new JSONObject(msgdata);
                    String roomid = jsonObject.getString("content");
                    int userid = p2pcmd.getFromUserId();
                    Intent intent = new Intent(this, VideoChatViewActivity2.class);
                    intent.putExtra("roomid", roomid);
                    intent.putExtra("tageruserId", p2pcmd.getFromUserId());
                    intent.putExtra("callAction", IMCallAction.ACTION_INCOMING_CALL.getName());
                    startActivity(intent);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            break;
            case MSG_FRIEND_INVITE: {
                int oldnum = IMUIHelper.getConfigNameAsInt(this, "newfriendinvite");
                oldnum++;
                IMUIHelper.setConfigName(this, "newfriendinvite", oldnum);
                mTabButtons[2].setUnreadNotify(oldnum);
            }
            break;
            case MSG_FRIEND_AGEREE: {
                IMContactManager.instance().reqGetAllUsers(0); //更新好友列表。
            }
            break;
        }
    }

    private void handleOnLogout() {
        logger.d("mainactivity#login#handleOnLogout");
        finish();
        logger.d("mainactivity#login#kill self, and start login activity");
        jumpToLoginPage();

    }

    private void jumpToLoginPage() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.putExtra(IntentConstant.KEY_LOGIN_NOT_AUTO, true);
        startActivity(intent);
    }


    public void applypermission() {
        if (Build.VERSION.SDK_INT >= 23) {
            boolean needapply = false;
            for (int i = 0; i < allpermissions.length; i++) {
                int chechpermission = ContextCompat.checkSelfPermission(getApplicationContext(),
                        allpermissions[i]);
                if (chechpermission != PackageManager.PERMISSION_GRANTED) {
                    needapply = true;
                }
            }
            if (needapply) {
                ActivityCompat.requestPermissions(MainActivity.this, allpermissions, 1);
            }
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        for (int i = 0; i < grantResults.length; i++) {
            if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
            } else {
            }
        }
    }


}