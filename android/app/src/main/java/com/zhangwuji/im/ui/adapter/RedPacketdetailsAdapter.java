package com.zhangwuji.im.ui.adapter;

import java.util.List;
import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.zhangwuji.im.R;
import com.zhangwuji.im.ui.entity.RedPacketLogBean;
import com.zhangwuji.im.ui.widget.IMBaseImageView;
import com.zhangwuji.im.utils.AvatarGenerate;


public class RedPacketdetailsAdapter extends ArrayAdapter<RedPacketLogBean>{

	private int resoureId;
    private boolean isshowshouqi;
	public RedPacketdetailsAdapter(Context context, int resource,
								   List<RedPacketLogBean> objects, boolean isshow) {
		super(context, resource, objects);
		this.resoureId = resource;
		this.isshowshouqi=isshow;
		// TODO Auto-generated constructor stub
	}

	public void setShowShouQi(boolean show)
	{
		this.isshowshouqi=show;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		RedPacketLogBean packetdetailsEntity = getItem(position);
		View view ;
		ViewHolder viewHolder;
		if(convertView == null){
			view = LayoutInflater.from(getContext()).inflate(resoureId, null);
			viewHolder = new ViewHolder();
			viewHolder.tv_details = (TextView) view.findViewById(R.id.tv_details);
			viewHolder.tv_time = (TextView) view.findViewById(R.id.tv_time);
			viewHolder.tv_money = (TextView) view.findViewById(R.id.tv_money);
			viewHolder.iv_touxiang = (IMBaseImageView) view.findViewById(R.id.iv_touxiang);
			viewHolder.ll_shouqi = (LinearLayout) view.findViewById(R.id.ll_shouqi);
			view.setTag(viewHolder);
		}else{
			view = convertView;
			viewHolder = (ViewHolder) view.getTag();
		}
		viewHolder.tv_details.setText(packetdetailsEntity.getNickname());
		viewHolder.tv_time.setText(packetdetailsEntity.getAddtime());
		viewHolder.tv_money.setText(packetdetailsEntity.getMoney()+"元");


		
		if(isshowshouqi)
		{
			if(packetdetailsEntity.getLv()==1)
			{
			  viewHolder.ll_shouqi.setVisibility(View.VISIBLE);
			}
			else
			{
			  viewHolder.ll_shouqi.setVisibility(View.GONE);
			}
		}

		viewHolder.iv_touxiang.setCorner(8);
		viewHolder.iv_touxiang.setImageUrl(AvatarGenerate.generateAvatar(packetdetailsEntity.getAvatar(),packetdetailsEntity.getNickname(),packetdetailsEntity.getUid()+""));

		return view;
	}
	public final static class ViewHolder {
		TextView tv_details;
		TextView tv_time;
		TextView tv_money;
		IMBaseImageView iv_touxiang;
	    LinearLayout ll_shouqi;
	}
}