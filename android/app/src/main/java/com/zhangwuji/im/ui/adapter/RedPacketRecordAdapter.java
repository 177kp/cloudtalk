package com.zhangwuji.im.ui.adapter;

import java.util.List;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.zhangwuji.im.R;
import com.zhangwuji.im.ui.adapter.RedPacketdetailsEntity;
import com.zhangwuji.im.ui.widget.IMBaseImageView;


public class RedPacketRecordAdapter extends ArrayAdapter<RedPacketdetailsEntity>{

	private int resoureId;
	private String allmoney = "0";
	private String allcount = "0";
	private String bestcount="0";
	private String username="";
	private String faceurl="";
	private String uid="0";
	
	public RedPacketRecordAdapter(Context context, int resource,
			List<RedPacketdetailsEntity> objects,String allmoney,String allcount,String bestcount,String username,String faceurl,String uid) {
		super(context, resource, objects);
		resoureId = resource;
		// TODO Auto-generated constructor stub
		
		this.allmoney=allmoney;
		this.allcount=allcount;
		this.bestcount=bestcount;
		this.username=username;
		this.faceurl=faceurl;
		this.uid=uid;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		View view ;

		if(position==0)
		{
			view = LayoutInflater.from(getContext()).inflate(R.layout.item_hongbaorecode_top, null);
			ViewHolder2 viewHolder2=new ViewHolder2();
			viewHolder2.tv_money = (TextView) view.findViewById(R.id.tv_money);
			viewHolder2.tv_name = (TextView) view.findViewById(R.id.tv_name);
			viewHolder2.tv_shouqizuijia = (TextView) view.findViewById(R.id.tv_shouqizuijia);
			viewHolder2.tv_allhongbao = (TextView) view.findViewById(R.id.tv_allhongbao);
			viewHolder2.iv_touxiang = (IMBaseImageView) view.findViewById(R.id.iv_touxiang);
			
			viewHolder2.tv_allhongbao.setText(this.allcount);
			viewHolder2.tv_shouqizuijia.setText(this.bestcount);
			viewHolder2.tv_money.setText(this.allmoney);
			
			//ImageLoader.getInstance().displayImage(TextUtils.isEmpty(this.username) ? RongGenerate.generateDefaultAvatar(this.username, uid) : this.faceurl, viewHolder2.iv_touxiang, App.getOptions());
			viewHolder2.tv_name.setText(this.username+getContext().getString(R.string.hongbao_reshongbao_gongshoudao));
			return view;
		}
		else
		{
		  RedPacketdetailsEntity packetdetailsEntity = getItem(position);
		  ViewHolder viewHolder;
		  //if(convertView == null){
			view = LayoutInflater.from(getContext()).inflate(resoureId, null);
			viewHolder = new ViewHolder();
			viewHolder.tv_details = (TextView) view.findViewById(R.id.tv_details);
			viewHolder.tv_time = (TextView) view.findViewById(R.id.tv_time);
			viewHolder.tv_pin = (TextView) view.findViewById(R.id.tv_pin);
			viewHolder.tv_money = (TextView) view.findViewById(R.id.tv_money);
			view.setTag(viewHolder);
//		}else{
//			view = convertView;
//			viewHolder = (ViewHolder) view.getTag();
//		}
		if(packetdetailsEntity.getType2().equals("2")){
			viewHolder.tv_pin.setVisibility(View.VISIBLE);
		}else{
			viewHolder.tv_pin.setVisibility(View.GONE);
		}
		viewHolder.tv_details.setText(packetdetailsEntity.getName());
		viewHolder.tv_time.setText(packetdetailsEntity.getAddtime());
		viewHolder.tv_money.setText(packetdetailsEntity.getMoney()+"元");


		return view;
		}
	}
	
	public final static class ViewHolder {
		TextView tv_details;
		TextView tv_time;
		TextView tv_money;
		TextView tv_pin;

	}
	public final static class ViewHolder2 {
		TextView tv_money;
		TextView tv_allhongbao;
		TextView tv_shouqizuijia;
		TextView tv_name;
		IMBaseImageView iv_touxiang;

	}
	
}
