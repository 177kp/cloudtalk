package com.zhangwuji.im.ui.activity;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;

import com.zhangwuji.im.DB.entity.User;
import com.zhangwuji.im.R;
import com.zhangwuji.im.config.IntentConstant;
import com.zhangwuji.im.imcore.service.IMService;
import com.zhangwuji.im.imcore.service.IMServiceConnector;
import com.zhangwuji.im.server.network.BaseAction;
import com.zhangwuji.im.ui.base.TTBaseActivity;
import com.zhangwuji.im.ui.base.TTBaseFragmentActivity;
import com.zhangwuji.im.ui.helper.ApiAction;

public class PrivacySettingsActivity extends TTBaseActivity implements View.OnClickListener  {

	private IMService imService;
	private User currentUser;
	private int currentUserId;
	private LinearLayout ll_ask;
	private CheckBox cb_add_friend_ask;

	private IMServiceConnector imServiceConnector = new IMServiceConnector(){
		@Override
		public void onIMServiceConnected() {
			logger.d("detail#onIMServiceConnected");

			imService = imServiceConnector.getIMService();
			if (imService == null) {
				logger.e("detail#imService is null");
				return;
			}

			currentUserId = getIntent().getIntExtra(IntentConstant.KEY_PEERID,0);
			if(currentUserId == 0){
				logger.e("detail#intent params error!!");
				return;
			}
			currentUser = imService.getContactManager().findContact(currentUserId);
		}
		@Override
		public void onServiceDisconnected() {}
	};
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		LayoutInflater.from(this).inflate(R.layout.tt_privacy_setting, topContentView);
		imServiceConnector.connect(this);
		//TOP_CONTENT_VIEW
		setLeftButton(R.drawable.ac_back_icon);
		setLeftText("返回");
		topLeftBtn.setOnClickListener(this);
		letTitleTxt.setOnClickListener(this);
		setTitle("隐私设置");

		ll_ask=findViewById(R.id.ll_ask);
		cb_add_friend_ask=findViewById(R.id.cb_add_friend_ask);

		cb_add_friend_ask.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
				if(b)
				{
					ll_ask.setVisibility(View.VISIBLE);
				}
				else
				{
					ll_ask.setVisibility(View.GONE);
				}
			}
		});

	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}

	@Override
	public void onClick(View view) {
		final int id = view.getId();
		switch (id) {
			case R.id.left_btn:
			case R.id.left_txt:
				finish();
				break;
		}

	}
}
