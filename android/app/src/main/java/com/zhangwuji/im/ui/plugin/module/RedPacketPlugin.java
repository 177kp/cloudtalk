package com.zhangwuji.im.ui.plugin.module;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;

import com.zhangwuji.im.R;
import com.zhangwuji.im.imcore.entity.TextMessage;
import com.zhangwuji.im.imcore.event.MessageEvent;
import com.zhangwuji.im.ui.activity.AMAPLocationActivity;
import com.zhangwuji.im.ui.activity.SendRedPacketActivity;
import com.zhangwuji.im.ui.plugin.ExtensionModule;
import com.zhangwuji.im.ui.plugin.IPluginData;
import com.zhangwuji.im.ui.plugin.IPluginModule;
import com.zhangwuji.im.ui.plugin.message.entity.LocationMessage;
import com.zhangwuji.im.ui.plugin.message.entity.RedPacketMessage;

import org.greenrobot.eventbus.EventBus;

/**
 * 发送红包功能插件
 */
public class RedPacketPlugin implements IPluginModule {

	IPluginData pluginData=null;
	public Drawable obtainDrawable(Context context) {
		return context.getResources().getDrawable(R.drawable.actionbar_redpacket_icon);
	}
	
	public String obtainTitle(Context context) {
		return "红包";
	}

	public void onClick(Activity currentActivity, IPluginData pluginData,int position) {
		this.pluginData=pluginData;
		//红包功能已屏蔽。有需要联系QQ689541
		new AlertDialog.Builder(currentActivity)
				.setMessage("红包功能不在开源项目内，有需求联系QQ:689541 ")
				.setPositiveButton("确认", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
					}
				})
				.setNegativeButton("取消", null)
				.create().show();

	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode != Activity.RESULT_OK)
			return;


	}
}