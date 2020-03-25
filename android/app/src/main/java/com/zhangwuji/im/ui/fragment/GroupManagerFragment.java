package com.zhangwuji.im.ui.fragment;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.zhangwuji.im.DB.entity.Group;
import com.zhangwuji.im.DB.entity.PeerEntity;
import com.zhangwuji.im.DB.entity.User;
import com.zhangwuji.im.config.DBConstant;
import com.zhangwuji.im.DB.sp.ConfigurationSp;
import com.zhangwuji.im.R;
import com.zhangwuji.im.config.IntentConstant;
import com.zhangwuji.im.imcore.manager.IMLoginManager;
import com.zhangwuji.im.server.network.BaseAction;
import com.zhangwuji.im.server.network.IMAction;
import com.zhangwuji.im.ui.activity.GroupQRCodeActivity;
import com.zhangwuji.im.ui.adapter.GroupManagerAdapter;
import com.zhangwuji.im.ui.helper.ApiAction;
import com.zhangwuji.im.ui.helper.CheckboxConfigHelper;
import com.zhangwuji.im.imcore.event.GroupEvent;
import com.zhangwuji.im.imcore.service.IMService;
import com.zhangwuji.im.ui.base.TTBaseFragment;
import com.zhangwuji.im.imcore.service.IMServiceConnector;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.listener.PauseOnScrollListener;
import com.zhangwuji.im.ui.helper.LoginInfoSp;

import java.util.ArrayList;
import java.util.List;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;


/**
 * @YM
 * 个人与群组的聊天详情都会来到这个页面
 * single: 这有sessionId的头像，以及加号"+" ， 创建群成功之后，跳到聊天的页面
 * group:  群成员，加减号 ， 修改成功之后，跳到群管理页面
 * 临时群任何人都可以加人，但是只有群主可以踢人”这个逻辑修改下，正式群暂时只给createId开放
 */
public class GroupManagerFragment extends TTBaseFragment {
    private View curView = null;
    /**adapter配置*/
    private GridView gridView;
    private GroupManagerAdapter adapter;


    /**详情的配置  勿扰以及指定聊天*/
    CheckboxConfigHelper checkBoxConfiger = new CheckboxConfigHelper();
    CheckBox noDisturbCheckbox;
    CheckBox topSessionCheckBox;
    CheckBox cb_dis_send_msg;

    /**需要的状态参数*/
    private IMService imService;
    private String curSessionKey;
    private PeerEntity peerEntity;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        imServiceConnector.connect(getActivity());
        EventBus.getDefault().register(this);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        if (null != curView) {
            ((ViewGroup) curView.getParent()).removeView(curView);
            return curView;
        }
        curView = inflater.inflate(R.layout.tt_fragment_group_manage, topContentView);
        noDisturbCheckbox = (CheckBox) curView.findViewById(R.id.NotificationNoDisturbCheckbox);
        topSessionCheckBox = (CheckBox) curView.findViewById(R.id.NotificationTopMessageCheckbox);
        initRes();
        return curView;
    }

    private void initRes() {
        // 设置标题栏
        setTopLeftButton(R.drawable.tt_top_back);
        setTopLeftText(getActivity().getString(R.string.top_left_back));
        topLeftContainerLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().finish();
            }
        });
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    /**
     * Called when the fragment is no longer in use.  This is called
     * after {@link #onStop()} and before {@link #onDetach()}.
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
        imServiceConnector.disconnect(getActivity());
    }

    @Override
    protected void initHandler() {
    }


    private IMServiceConnector imServiceConnector = new IMServiceConnector(){
        @Override
        public void onServiceDisconnected() {
        }

        @Override
        public void onIMServiceConnected() {
            logger.d("groupmgr#onIMServiceConnected");
            imService = imServiceConnector.getIMService();
            if(imService == null){
              Toast.makeText(GroupManagerFragment.this.getActivity(),
                        getResources().getString(R.string.im_service_disconnected), Toast.LENGTH_SHORT).show();
               return;
            }
            checkBoxConfiger.init(imService.getConfigSp());
            initView();
            initAdapter();
        }
    };


    private void initView() {
        setTopTitle(getString(R.string.chat_detail));
        if (null == imService || null == curView ) {
            logger.e("groupmgr#init failed,cause by imService or curView is null");
            return;
        }

        curSessionKey =  getActivity().getIntent().getStringExtra(IntentConstant.KEY_SESSION_KEY);
        if (TextUtils.isEmpty(curSessionKey)) {
            logger.e("groupmgr#getSessionInfoFromIntent failed");
            return;
        }
        peerEntity = imService.getSessionManager().findPeerEntity(curSessionKey);
        if(peerEntity == null){
            logger.e("groupmgr#findPeerEntity failed,sessionKey:%s",curSessionKey);
            return;
        }
        switch (peerEntity.getType()){
            case DBConstant.SESSION_TYPE_GROUP:{
               final  Group groupEntity = (Group) peerEntity;
                // 群组名称的展示
                TextView groupNameView = (TextView) curView.findViewById(R.id.group_manager_title);
                groupNameView.setText(groupEntity.getMainName());
                cb_dis_send_msg=curView.findViewById(R.id.cb_dis_send_msg);
                RelativeLayout group_manager_dis_msg=curView.findViewById(R.id.group_manager_dis_msg);
                //如果是管理员
                if(groupEntity.getCreatorId()==imService.getLoginManager().getLoginId())
                {

                    group_manager_dis_msg.setVisibility(View.VISIBLE);

                    RelativeLayout delete_group=curView.findViewById(R.id.delete_group);
                    delete_group.setVisibility(View.VISIBLE);

                    delete_group.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                            AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(getActivity(), android.R.style.Theme_Holo_Light_Dialog));
                            LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                            View dialog_view = inflater.inflate(R.layout.tt_custom_dialog, null);
                            final EditText editText = (EditText) dialog_view.findViewById(R.id.dialog_edit_content);
                            editText.setVisibility(View.GONE);
                            TextView textText = (TextView) dialog_view.findViewById(R.id.dialog_title);
                            textText.setText("确定解散该群组?");
                            builder.setView(dialog_view);
                            builder.setPositiveButton(getString(R.string.tt_ok), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            });

                            builder.setNegativeButton(getString(R.string.tt_cancel), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    dialogInterface.dismiss();
                                }
                            });
                            builder.show();
                        }
                    });


                    IMAction apiAction=new IMAction(getActivity());
                    apiAction.getGroupInfo(groupEntity.getId() + "", new BaseAction.ResultCallback<String>() {
                        @Override
                        public void onSuccess(String s) {

                            JSONObject jsonObject= JSON.parseObject(s);
                            JSONObject jData=jsonObject.getJSONObject("data");
                            JSONObject gdata= (JSONObject)jData.getJSONArray("grouplist").get(0);

                            if(gdata.getIntValue("disable_send_msg")==1)
                            {
                                cb_dis_send_msg.setChecked(true);
                            }
                            else
                            {
                                cb_dis_send_msg.setChecked(false);
                            }
                        }

                        @Override
                        public void onError(String errString) {

                        }
                    });


                    cb_dis_send_msg.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                            ApiAction apiAction1=new ApiAction(getActivity());
                            apiAction1.set_dis_send_msg(groupEntity.getId(), "", b == true ? 1 : 0, new BaseAction.ResultCallback<String>() {
                                @Override
                                public void onSuccess(String s) {
                                }
                                @Override
                                public void onError(String errString) {
                                }
                            });
                        }
                    });

                }
                else
                {
                    group_manager_dis_msg.setVisibility(View.GONE);
                }

            }break;

            case DBConstant.SESSION_TYPE_SINGLE:{
                // 个人不显示群聊名称
                View groupNameContainerView = curView.findViewById(R.id.group_manager_name);
                groupNameContainerView.setVisibility(View.GONE);

                RelativeLayout group_manager_vote=curView.findViewById(R.id.group_manager_vote);
                group_manager_vote.setVisibility(View.GONE);
                RelativeLayout rl_qrcode=curView.findViewById(R.id.rl_qrcode);
                rl_qrcode.setVisibility(View.GONE);

                RelativeLayout group_manager_dis_msg=curView.findViewById(R.id.group_manager_dis_msg);
                group_manager_dis_msg.setVisibility(View.GONE);



            }break;
        }

        RelativeLayout rl_qrcode= (RelativeLayout)curView.findViewById(R.id.rl_qrcode);
        rl_qrcode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getActivity(), GroupQRCodeActivity.class).putExtra(IntentConstant.KEY_SESSION_KEY,curSessionKey));
            }
        });

        // 初始化配置checkBox
        initCheckbox();
    }

    private void initAdapter(){
        logger.d("groupmgr#initAdapter");

        gridView = (GridView) curView.findViewById(R.id.group_manager_grid);
        gridView.setSelector(new ColorDrawable(Color.TRANSPARENT));// 去掉点击时的黄色背影
        gridView.setOnScrollListener(new PauseOnScrollListener(ImageLoader.getInstance(), true, true));

        adapter = new GroupManagerAdapter(getActivity(),imService,peerEntity);
        gridView.setAdapter(adapter);
    }

    /**事件驱动通知*/
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(GroupEvent event){
        switch (event.getEvent()){

            case CHANGE_GROUP_MEMBER_FAIL:
            case CHANGE_GROUP_MEMBER_TIMEOUT:{
                Toast.makeText(getActivity(), getString(R.string.change_temp_group_failed), Toast.LENGTH_SHORT).show();
                return;
            }
            case CHANGE_GROUP_MEMBER_SUCCESS:{
                onMemberChangeSuccess(event);
            }break;
        }
    }

    private void onMemberChangeSuccess(GroupEvent event){
        int groupId = event.getGroupEntity().getPeerId();
        if(groupId != peerEntity.getPeerId()){
            return;
        }
        List<Integer> changeList = event.getChangeList();
        if(changeList == null || changeList.size()<=0){
            return;
        }
        int changeType = event.getChangeType();

        switch (changeType){
            case DBConstant.GROUP_MODIFY_TYPE_ADD:
                ArrayList<User> newList = new ArrayList<>();
                for(Integer userId:changeList){
                    User userEntity =  imService.getContactManager().findContact(userId);
                    if(userEntity!=null) {
                        newList.add(userEntity);
                    }
                }
                adapter.add(newList);
                break;
            case DBConstant.GROUP_MODIFY_TYPE_DEL:
                for(Integer userId:changeList){
                    adapter.removeById(userId);
                }
                break;
        }
    }

	private void initCheckbox() {
        checkBoxConfiger.initCheckBox(noDisturbCheckbox, curSessionKey, ConfigurationSp.CfgDimension.NOTIFICATION);
        checkBoxConfiger.initTopCheckBox(topSessionCheckBox,curSessionKey);
    }
}
