package com.zhangwuji.im.ui.widget;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.qmuiteam.qmui.widget.dialog.QMUIDialog;
import com.qmuiteam.qmui.widget.dialog.QMUIDialogAction;
import com.zhangwuji.im.R;
import com.zhangwuji.im.server.network.BaseAction;
import com.zhangwuji.im.ui.activity.NearByPeopleInfoActivity;
import com.zhangwuji.im.ui.activity.NewFriendListActivity;
import com.zhangwuji.im.ui.activity.capture.CaptureActivity;
import com.zhangwuji.im.ui.entity.NearByUser;
import com.zhangwuji.im.ui.helper.ApiAction;
import com.zhangwuji.im.ui.helper.IMUIHelper;
import com.zhangwuji.im.ui.helper.LoginInfoSp;

import org.json.JSONException;


public class MorePopWindow extends PopupWindow {

    @SuppressLint("InflateParams")
    public MorePopWindow(final Activity context) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View content = inflater.inflate(R.layout.popupwindow_add, null);

        // 设置SelectPicPopupWindow的View
        this.setContentView(content);
        // 设置SelectPicPopupWindow弹出窗体的宽
        this.setWidth(LayoutParams.WRAP_CONTENT);
        // 设置SelectPicPopupWindow弹出窗体的高
        this.setHeight(LayoutParams.WRAP_CONTENT);
        // 设置SelectPicPopupWindow弹出窗体可点击
        this.setFocusable(true);
        this.setOutsideTouchable(true);
        // 刷新状态
        this.update();
        // 实例化一个ColorDrawable颜色为半透明
        ColorDrawable dw = new ColorDrawable(0000000000);
        // 点back键和其他地方使其消失,设置了这个才能触发OnDismisslistener ，设置其他控件变化等操作
        this.setBackgroundDrawable(dw);

        // 设置SelectPicPopupWindow弹出窗体动画效果
        this.setAnimationStyle(R.style.AnimationPreview);


        RelativeLayout re_addfriends = (RelativeLayout) content.findViewById(R.id.re_addfriends);
        RelativeLayout re_creatgroup = (RelativeLayout) content.findViewById(R.id.re_creatgroup);
        RelativeLayout re_scanner = (RelativeLayout) content.findViewById(R.id.re_scanner);
        RelativeLayout re_nearby=(RelativeLayout) content.findViewById(R.id.re_nearby);

        re_addfriends.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

                MorePopWindow.this.dismiss();

                final QMUIDialog.EditTextDialogBuilder builder = new QMUIDialog.EditTextDialogBuilder(context);
                builder.setTitle("请输入好友ID")
                        .setPlaceholder("请在此输入好友ID/用户名")
                        .setInputType(InputType.TYPE_CLASS_TEXT)
                        .addAction("取消", new QMUIDialogAction.ActionListener() {
                            @Override
                            public void onClick(QMUIDialog dialog, int index) {
                                dialog.dismiss();
                            }
                        })
                        .addAction("确定", new QMUIDialogAction.ActionListener() {
                            @Override
                            public void onClick(final QMUIDialog dialog, int index) {
                                String phone = builder.getEditText().getText().toString();
                                if (!phone.equals(LoginInfoSp.instance().getLoginInfoIdentity().getLoginName())) {

                                    ApiAction apiAction = new ApiAction(context);
                                    apiAction.getUserInfoByPhone(phone, new BaseAction.ResultCallback<String>() {
                                        @Override
                                        public void onSuccess(String s) {
                                            org.json.JSONObject jsonObject = null;
                                            int code = 0;
                                            try {
                                                jsonObject = new org.json.JSONObject(s);
                                                code = jsonObject.getInt("code");
                                            } catch (JSONException e) {
                                                e.printStackTrace();
                                            }
                                            if (code == 200) {
                                                try {
                                                    NearByUser nearByUser = JSON.parseObject(jsonObject.getJSONObject("data").getJSONArray("userinfo").get(0).toString(), NearByUser.class);
                                                    Intent intent = new Intent(context, NearByPeopleInfoActivity.class);
                                                    intent.putExtra("userinfo", nearByUser);
                                                    context.startActivity(intent);
                                                    dialog.dismiss();
                                                } catch (JSONException e) {
                                                    e.printStackTrace();
                                                }
                                            } else {
                                                Toast.makeText(context, "没有该用户信息!", Toast.LENGTH_SHORT).show();
                                            }
                                        }

                                        @Override
                                        public void onError(String errString) {
                                            Toast.makeText(context, "没有该用户信息!", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                } else {
                                    Toast.makeText(context, "切勿添加自身！", Toast.LENGTH_SHORT).show();
                                }

                            }
                        }).show();
            }

        });

        re_creatgroup.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                MorePopWindow.this.dismiss();
                IMUIHelper.openGroupMemberSelectActivity(context, null);
            }

        });
        re_scanner.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                MorePopWindow.this.dismiss();
                Intent intent2 = new Intent(context, CaptureActivity.class);
                context.startActivity(intent2);
            }
        });

        re_nearby.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                MorePopWindow.this.dismiss();
                Intent intent2 = new Intent(context, NearByPeopleInfoActivity.class);
                context.startActivity(intent2);
            }
        });

    }

    /**
     * 显示popupWindow
     *
     * @param parent
     */
    public void showPopupWindow(View parent) {
        if (!this.isShowing()) {
            // 以下拉方式显示popupwindow
            this.showAsDropDown(parent, 0, 0);
        } else {
            this.dismiss();
        }
    }
}
