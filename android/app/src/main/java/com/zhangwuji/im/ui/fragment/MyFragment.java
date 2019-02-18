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
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.zhangwuji.im.DB.entity.User;
import com.zhangwuji.im.R;
import com.zhangwuji.im.ui.helper.IMUIHelper;
import com.zhangwuji.im.imcore.event.UserInfoEvent;
import com.zhangwuji.im.imcore.manager.IMLoginManager;
import com.zhangwuji.im.imcore.service.IMService;
import com.zhangwuji.im.ui.activity.SettingActivity;
import com.zhangwuji.im.imcore.service.IMServiceConnector;
import com.zhangwuji.im.ui.helper.LoginInfoSp;
import com.zhangwuji.im.utils.FileUtil;
import com.zhangwuji.im.ui.widget.IMBaseImageView;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.io.File;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class MyFragment extends MainFragment {
	private View curView = null;
	private View contentView;
	private View exitView;
    private View clearView;
    private View settingView;

    private IMServiceConnector imServiceConnector = new IMServiceConnector(){
        @Override
        public void onServiceDisconnected() {}

        @Override
        public void onIMServiceConnected() {
            if (curView == null) {
                return;
            }
            IMService imService = imServiceConnector.getIMService();
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

        clearView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(getActivity(), android.R.style.Theme_Holo_Light_Dialog));
                LayoutInflater inflater = (LayoutInflater)getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                View dialog_view = inflater.inflate(R.layout.tt_custom_dialog, null);
                final EditText editText = (EditText)dialog_view.findViewById(R.id.dialog_edit_content);
                editText.setVisibility(View.GONE);
                TextView textText = (TextView)dialog_view.findViewById(R.id.dialog_title);
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
                                        + File.separator + "MGJ-IM"+File.separator),System.currentTimeMillis());
                                Toast toast = Toast.makeText(getActivity(),R.string.thumb_remove_finish,Toast.LENGTH_LONG);
                                toast.setGravity(Gravity.CENTER,0,0);
                                toast.show();
                            }
                        },500);

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
                LayoutInflater inflater = (LayoutInflater)getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                View dialog_view = inflater.inflate(R.layout.tt_custom_dialog, null);
                final EditText editText = (EditText)dialog_view.findViewById(R.id.dialog_edit_content);
                editText.setVisibility(View.GONE);
                TextView textText = (TextView)dialog_view.findViewById(R.id.dialog_title);
                textText.setText(R.string.exit_CloudTalk);
                builder.setView(dialog_view);
                builder.setPositiveButton(getString(R.string.tt_ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        IMLoginManager.instance().setKickout(false);
                        IMLoginManager.instance().logOut();

                        LoginInfoSp loginsp=LoginInfoSp.instance();
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
    public void onEventMainThread(UserInfoEvent event){
        switch (event){
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
        IMBaseImageView portraitImageView = (IMBaseImageView) curView.findViewById(R.id.user_portrait);

		nickNameView.setText(loginContact.getMainName());
		userNameView.setText(loginContact.getRealName());

        //头像设置
        portraitImageView.setDefaultImageRes(R.drawable.tt_default_user_portrait_corner);
        portraitImageView.setCorner(15);
        portraitImageView.setImageResource(R.drawable.tt_default_user_portrait_corner);
        portraitImageView.setImageUrl(loginContact.getAvatar());

        RelativeLayout userContainer = (RelativeLayout) curView.findViewById(R.id.user_container);
		userContainer.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				IMUIHelper.openUserProfileActivity(getActivity(), loginContact.getPeerId());
			}
		});
	}

    private void deleteFilesByDirectory(File directory) {
        if (directory != null && directory.exists() && directory.isDirectory()) {
            for (File item : directory.listFiles()) {
                item.delete();
            }
        }
        else {
            logger.e("fragment#deleteFilesByDirectory, failed");
        }
    }
}
