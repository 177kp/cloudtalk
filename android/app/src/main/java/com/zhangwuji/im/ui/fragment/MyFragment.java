package com.zhangwuji.im.ui.fragment;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.view.ContextThemeWrapper;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.qmuiteam.qmui.widget.dialog.QMUIDialog;
import com.qmuiteam.qmui.widget.dialog.QMUIDialogAction;
import com.qmuiteam.qmui.widget.dialog.QMUITipDialog;
import com.zhangwuji.im.DB.entity.User;
import com.zhangwuji.im.R;
import com.zhangwuji.im.config.IntentConstant;
import com.zhangwuji.im.imcore.event.UserInfoEvent;
import com.zhangwuji.im.imcore.manager.IMLoginManager;
import com.zhangwuji.im.imcore.service.IMService;
import com.zhangwuji.im.imcore.service.IMServiceConnector;
import com.zhangwuji.im.server.network.BaseAction;
import com.zhangwuji.im.server.network.IMAction;
import com.zhangwuji.im.ui.activity.MyUserActivity;
import com.zhangwuji.im.ui.activity.MyWalletActivity;
import com.zhangwuji.im.ui.activity.PrivacySettingsActivity;
import com.zhangwuji.im.ui.activity.SendRedPacketActivity;
import com.zhangwuji.im.ui.activity.SettingActivity;
import com.zhangwuji.im.ui.activity.UserInfoActivity;
import com.zhangwuji.im.ui.activity.WalletActivity;
import com.zhangwuji.im.ui.helper.ApiAction;
import com.zhangwuji.im.ui.helper.IMUIHelper;
import com.zhangwuji.im.ui.helper.LoginInfoSp;
import com.zhangwuji.im.ui.helper.UpdateManager;
import com.zhangwuji.im.ui.widget.IMBaseImageView;
import com.zhangwuji.im.ui.widget.SecurityPasswordEditText;
import com.zhangwuji.im.utils.CommonUtil;
import com.zhangwuji.im.utils.FileUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

public class MyFragment extends MainFragment {
    private View curView = null;
    private View contentView;
    private View exitView;
    private View clearView;
    private View settingView;
    private View wallet;
    private RelativeLayout checkupdata;
    private SecurityPasswordEditText editText_Pwd;
    private String payPwd1,payPwd2;
    private  QMUITipDialog tipDialog;
    private   IMService imService;
    private View rl_privacy;
    private TextView tv_tuijiancode;

    private IMServiceConnector imServiceConnector = new IMServiceConnector() {
        @Override
        public void onServiceDisconnected() {
        }

        @Override
        public void onIMServiceConnected() {
            if (curView == null) {
                return;
            }
            imService = imServiceConnector.getIMService();
            if (imService == null) {
                return;
            }
            init(imService);
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        imServiceConnector.connect(getActivity());
        EventBus.getDefault().register(this);



        tipDialog = new QMUITipDialog.Builder(getActivity())
                .setIconType(QMUITipDialog.Builder.ICON_TYPE_LOADING)
                .setTipWord("正在提交")
                .create();

        if (null != curView) {
            ((ViewGroup) curView.getParent()).removeView(curView);
            return curView;
        }
        curView = inflater.inflate(R.layout.tt_fragment_my, topContentView);

        initRes();

        return curView;
    }

    /**
     * @Description 初始化资源
     */
    private void initRes() {
        super.init(curView);

        contentView = curView.findViewById(R.id.content);
        exitView = curView.findViewById(R.id.exitPage);
        clearView = curView.findViewById(R.id.clearPage);
        settingView = curView.findViewById(R.id.settingPage);
        checkupdata = curView.findViewById(R.id.checkupdata);
        rl_privacy=curView.findViewById(R.id.rl_privacy);

        rl_privacy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getActivity(), PrivacySettingsActivity.class));

            }
        });

        checkupdata.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UpdateManager.getUpdateManager().checkAppUpdate(getActivity(), true, true, new UpdateManager.OnUpdateAppListenner() {
                    @Override
                    public void hasUpdate(boolean isAdvise) {

                    }

                    @Override
                    public void noUpdate() {
                        QMUIDialog.MessageDialogBuilder builder = new QMUIDialog.MessageDialogBuilder(getContext());
                        builder.setMessage("您当前已经是最新版本")
                                .setTitle("系统提示")
                                .addAction("确定", new QMUIDialogAction.ActionListener() {
                                    @Override
                                    public void onClick(QMUIDialog qmuiDialog, int i) {
                                        qmuiDialog.dismiss();
                                    }
                                })
                                .setCancelable(true)
                                .create();
                        builder.show();

                    }

                    @Override
                    public void error() {

                        final QMUIDialog.MessageDialogBuilder builder = new QMUIDialog.MessageDialogBuilder(getContext());
                        builder.setMessage("无法获取版本更新信息")
                                .setTitle("系统提示")
                                .addAction("确定", new QMUIDialogAction.ActionListener() {
                                    @Override
                                    public void onClick(QMUIDialog qmuiDialog, int i) {
                                        qmuiDialog.dismiss();
                                    }
                                })
                                .setCancelable(true)
                                .create();
                        builder.show();
                    }
                });
            }
        });
        wallet = curView.findViewById(R.id.wallet);
        wallet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MyFragment.this.getActivity(), WalletActivity.class));
            }
        });


        clearView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(getActivity(), android.R.style.Theme_Holo_Light_Dialog));
                LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                View dialog_view = inflater.inflate(R.layout.tt_custom_dialog, null);
                final EditText editText = (EditText) dialog_view.findViewById(R.id.dialog_edit_content);
                editText.setVisibility(View.GONE);
                TextView textText = (TextView) dialog_view.findViewById(R.id.dialog_title);
                textText.setText(R.string.clear_cache_tip);
                builder.setView(dialog_view);

                builder.setPositiveButton(getString(R.string.tt_ok), new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ImageLoader.getInstance().clearMemoryCache();
                        ImageLoader.getInstance().clearDiskCache();
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                FileUtil.deleteHistoryFiles(new File(Environment.getExternalStorageDirectory().toString()
                                        + File.separator + "MGJ-IM" + File.separator), System.currentTimeMillis());
                                Toast toast = Toast.makeText(getActivity(), R.string.thumb_remove_finish, Toast.LENGTH_LONG);
                                toast.setGravity(Gravity.CENTER, 0, 0);
                                toast.show();
                            }
                        }, 500);

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
        exitView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(getActivity(), android.R.style.Theme_Holo_Light_Dialog));
                LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                View dialog_view = inflater.inflate(R.layout.tt_custom_dialog, null);
                final EditText editText = (EditText) dialog_view.findViewById(R.id.dialog_edit_content);
                editText.setVisibility(View.GONE);
                TextView textText = (TextView) dialog_view.findViewById(R.id.dialog_title);
                textText.setText(R.string.exit_CloudTalk);
                builder.setView(dialog_view);
                builder.setPositiveButton(getString(R.string.tt_ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        IMLoginManager.instance().setKickout(false);
                        IMLoginManager.instance().logOut();

                        LoginInfoSp loginsp = LoginInfoSp.instance();
                        loginsp.init(getActivity());
                        loginsp.clearLoginInfo();

                        getActivity().finish();
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

        settingView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 跳转到配置页面
                startActivity(new Intent(MyFragment.this.getActivity(), SettingActivity.class));
            }
        });
        hideContent();

        // 设置顶部标题栏
        setTopTitle(getActivity().getString(R.string.page_me));
        // 设置页面其它控件

        ApiAction apiAction=new ApiAction(getActivity());
        apiAction.checkPayPWD(new BaseAction.ResultCallback<String>() {
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

                if(code!=200) //需要设置支付密码
                {
                    QMUIDialog.MessageDialogBuilder builder = new QMUIDialog.MessageDialogBuilder(getContext());
                    builder.setMessage("需要设置支付密码!")
                            .setTitle("系统提示")
                            .addAction("确定", new QMUIDialogAction.ActionListener() {
                                @Override
                                public void onClick(QMUIDialog qmuiDialog, int i) {
                                    qmuiDialog.dismiss();

                                    showPayPWD1();
                                }
                            })
                            .setCancelable(true)
                            .create();
                    builder.show();
                }
            }

            @Override
            public void onError(String errString) {

            }
        });

    }

    private void showPayPWD1()
    {
        final AlertDialog dlg = new AlertDialog.Builder(getActivity()).create();
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
                payPwd1 = editText_Pwd.getInputnumber();
                if (payPwd1 == null || payPwd1.equals("") || payPwd1.length() < 6) {

                    Toast.makeText(getActivity(),
                            getString(R.string.pay_paypasswordtip1), Toast.LENGTH_SHORT).show();

                } else {
                    dlg.dismiss();

                    showPayPWD2();

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
       getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
    }

    private void showPayPWD2()
    {
        final AlertDialog dlg = new AlertDialog.Builder(getActivity()).create();
        dlg.setCanceledOnTouchOutside(false);
        dlg.show();
        Window window = dlg.getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                | WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        window.setContentView(R.layout.starcard_query_pwdvalidate_layout);
        final Button ok = (Button) window.findViewById(R.id.button_Query);
        Button cancel = (Button) window.findViewById(R.id.button1_Cancel);
        ok.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                payPwd2 = editText_Pwd.getInputnumber();
                if (payPwd2 == null || payPwd2.equals("") || payPwd2.length() < 6) {

                    Toast.makeText(getActivity(),
                            getString(R.string.pay_paypasswordtip1), Toast.LENGTH_SHORT).show();

                } else {
                    dlg.dismiss();

                    if(!payPwd1.equals(payPwd2))
                    {
                        Toast.makeText(getActivity(),"两次密码输入不一致!", Toast.LENGTH_SHORT).show();
                        payPwd1="";
                        payPwd2="";
                        showPayPWD1();
                    }
                    else
                    {

                        ApiAction apiAction=new ApiAction(getActivity());
                        String payPwd= CommonUtil.md5(payPwd2).toLowerCase();
                        apiAction.setPayPWD(payPwd,new BaseAction.ResultCallback<String>() {
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
                                    tipDialog = new QMUITipDialog.Builder(getActivity())
                                            .setIconType(QMUITipDialog.Builder.ICON_TYPE_SUCCESS)
                                            .setTipWord("支付密码设置成功!")
                                            .create();
                                    tipDialog.show();

                                    contentView.postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            tipDialog.dismiss();
                                        }
                                    }, 2000);
                                }
                                else
                                {
                                    tipDialog = new QMUITipDialog.Builder(getActivity())
                                            .setIconType(QMUITipDialog.Builder.ICON_TYPE_FAIL)
                                            .setTipWord("支付密码设置失败!")
                                            .create();
                                    tipDialog.show();

                                    contentView.postDelayed(new Runnable() {
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
                .setText("确认支付密码");
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
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
    }

    private void hideContent() {
        if (contentView != null) {
            contentView.setVisibility(View.GONE);
        }
    }

    private void showContent() {
        if (contentView != null) {
            contentView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // 应该放在这里嘛??
        imServiceConnector.disconnect(getActivity());
        EventBus.getDefault().unregister(this);
    }

    @Override
    protected void initHandler() {
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(UserInfoEvent event) {
        switch (event) {
            case USER_INFO_OK:
                init(imServiceConnector.getIMService());
        }
    }


    private void init(IMService imService) {
        showContent();
        hideProgressBar();

        if (imService == null) {
            return;
        }

        final User loginContact = imService.getLoginManager().getLoginInfo();
        if (loginContact == null) {
            return;
        }
        TextView nickNameView = (TextView) curView.findViewById(R.id.nickName);
        TextView userNameView = (TextView) curView.findViewById(R.id.userName);
        tv_tuijiancode = (TextView) curView.findViewById(R.id.tv_tuijiancode);

        IMBaseImageView portraitImageView = (IMBaseImageView) curView.findViewById(R.id.user_portrait);

        nickNameView.setText(loginContact.getMainName());
        userNameView.setText("ID:" + loginContact.getPeerId());

        IMAction apiAction=new IMAction(getActivity());
        apiAction.getUserInfo(loginContact.getPeerId()+"", new BaseAction.ResultCallback<String>() {
            @Override
            public void onSuccess(String s) {
                com.alibaba.fastjson.JSONObject object=JSON.parseObject(s);
                com.alibaba.fastjson.JSONObject data=object.getJSONObject("data");
                com.alibaba.fastjson.JSONObject userinfo= (com.alibaba.fastjson.JSONObject)data.getJSONArray("userinfo").get(0);
                tv_tuijiancode.setText("推荐码:"+userinfo.getString("code"));
            }

            @Override
            public void onError(String errString) {

            }
        });



        //头像设置
        portraitImageView.setDefaultImageRes(R.drawable.tt_default_user_portrait_corner);
        portraitImageView.setCorner(15);
        portraitImageView.setImageResource(R.drawable.tt_default_user_portrait_corner);
        portraitImageView.setImageUrl(loginContact.getAvatar());

        RelativeLayout userContainer = (RelativeLayout) curView.findViewById(R.id.user_container);
        userContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {

                Intent intent = new Intent(getActivity(), MyUserActivity.class);
                intent.putExtra(IntentConstant.KEY_PEERID, loginContact.getPeerId());
                getActivity().startActivity(intent);
            }
        });

        (curView.findViewById(R.id.iv_qrcode)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                IMUIHelper.openQRCodeActivity(getActivity(), LoginInfoSp.instance().getLoginInfoIdentity().getLoginName());
            }
        });


    }

    private void deleteFilesByDirectory(File directory) {
        if (directory != null && directory.exists() && directory.isDirectory()) {
            for (File item : directory.listFiles()) {
                item.delete();
            }
        } else {
            logger.e("fragment#deleteFilesByDirectory, failed");
        }
    }
}
