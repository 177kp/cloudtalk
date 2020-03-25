package com.zhangwuji.im.ui.widget.xlistview;

import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ListView;

import com.zhangwuji.im.R;


public class PullDownView extends ListView implements OnScrollListener {

	/** 底部显示正在加载的页面 */
	private View footerView = null;
	/** 存储上下文 */
	private Context context;
	/** 上拉刷新的ListView的回调监听 */
	private MyPullUpListViewCallBack myPullUpListViewCallBack;
	/** 记录第一行Item的数值 */
	private int firstVisibleItem;

	public PullDownView(Context context) {
		super(context);
		this.context = context;
		initListView();
	}

	public PullDownView(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.context = context;
		initListView();
	}

	/**
	 * 初始化ListView
	 */
	private void initListView() {

		// 为ListView设置滑动监听
		setOnScrollListener(this);
		// 去掉底部分割线
		setFooterDividersEnabled(false);
	}

	/**
	 * 初始化话底部页面
	 */
	public void initBottomView() {

		if (footerView == null) {
			footerView = LayoutInflater.from(this.context).inflate(
					R.layout.pulldown_footer, null);
		}
		addFooterView(footerView);
	}

	public void onScrollStateChanged(AbsListView view, int scrollState) {

		//当滑动到底部时
		if (scrollState == OnScrollListener.SCROLL_STATE_IDLE
				&& firstVisibleItem != 0) {
			new Handler().postDelayed(new Runnable() {  

				@Override  
				public void run() {  
					myPullUpListViewCallBack.scrollBottomState(); 
				}  
			}, 2000);  
		}
	}

	public void onScroll(AbsListView view, int firstVisibleItem,
			int visibleItemCount, int totalItemCount) {
		this.firstVisibleItem = firstVisibleItem;

		if (footerView != null) {
			//判断可视Item是否能在当前页面完全显示
			if (visibleItemCount == totalItemCount) {
				// removeFooterView(footerView);
				footerView.setVisibility(View.GONE);//隐藏底部布局
			} else {
				// addFooterView(footerView);
				footerView.setVisibility(View.VISIBLE);//显示底部布局
			}
		}

	}

	public void setMyPullUpListViewCallBack(
			MyPullUpListViewCallBack myPullUpListViewCallBack) {
		this.myPullUpListViewCallBack = myPullUpListViewCallBack;
	}

	/**
	 * 上拉加载的ListView的回调监听
	 * 
	 */
	public interface MyPullUpListViewCallBack {

		void scrollBottomState();
	}

}