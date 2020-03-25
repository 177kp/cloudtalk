package com.zhangwuji.im.ui.widget;


import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.zhangwuji.im.R;
import com.zhangwuji.im.ui.helper.StringUtils;
import com.zhangwuji.im.utils.ScreenUtil;

import java.util.List;

/**
 * 通用类似AlertDialog，实现各种对话框，各种对话框时复用同一个对话框
 * 注意：如果指定页面有几种不同该对话框显示，则需要调用reset()方法
 *
 * @author wangf
 */
public class A_CustomAlertDialog extends Dialog {

    private Context mContext;// 上下文
    private TextView tvTitle, tvMessage, tvLeftButton, tvRightButton;// 标题，正文，左下按钮，右下按钮
    private LinearLayout llContent;// 内部添加自定义布局
    private View view_line;// 底部的线
    private LinearLayout llButtons;// 底部的按钮布局
    private View view_button;// 按钮之间的线

    public A_CustomAlertDialog(Context context) {
        super(context, R.style.Dialog);// 创建一个使用指定样式的对话框
        setContentView(R.layout.a_widget_customalertdialog);
        this.mContext = context;
        tvTitle = (TextView) findViewById(R.id.tvTitle);
        tvTitle.setVisibility(View.GONE);
        tvMessage = (TextView) findViewById(R.id.tvMessage);
        tvMessage.setVisibility(View.GONE);
        tvLeftButton = (TextView) findViewById(R.id.tvLeftButton);
        tvLeftButton.setBackgroundResource(R.drawable.actionsheet_cancel_selector);
        tvLeftButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        tvRightButton = (TextView) findViewById(R.id.tvRightButton);
        llContent = (LinearLayout) findViewById(R.id.llContent);
        view_line = findViewById(R.id.view_line);
        llButtons = (LinearLayout) findViewById(R.id.llButtons);
        view_button = findViewById(R.id.view_button);
    }

    private AdapterView.OnItemClickListener mItemClickListener;

    /**
     * 充值对话框
     */
    public A_CustomAlertDialog reset() {
        tvTitle.setVisibility(View.GONE);
        tvMessage.setVisibility(View.GONE);
        llContent.setVisibility(View.VISIBLE);
        llContent.removeAllViews();
        view_line.setVisibility(View.VISIBLE);
        llButtons.setVisibility(View.VISIBLE);
        tvLeftButton.setVisibility(View.VISIBLE);
        view_button.setVisibility(View.VISIBLE);
        tvRightButton.setVisibility(View.VISIBLE);
        return this;
    }

    /**
     * 设置单选
     *
     * @param list     泛型集合
     * @param listener 每一项点击事件
     */
    public A_CustomAlertDialog setItems(List<?> list, AdapterView.OnItemClickListener listener) {
        Object[] items;
        if (!StringUtils.isEmptyList(list)) {
            items = new Object[list.size()];
            for (int i = 0; i < list.size(); i++) {
                items[i] = list.get(i);
            }
            setItems(items, listener);
        }
        return this;
    }

    /**
     * 设置单选
     *
     * @param items    显示文案支持 String 和Spanned类型
     * @param listener
     */
    public A_CustomAlertDialog setItems(Object[] items, AdapterView.OnItemClickListener listener) {
        tvMessage.setVisibility(View.GONE);

        this.mItemClickListener = listener;

        llButtons.setVisibility(View.GONE);
        view_line.setVisibility(View.GONE);

        ScrollView scroll = new ScrollView(mContext);
        scroll.setVerticalScrollBarEnabled(false);
        LinearLayout llItems = new LinearLayout(mContext);
        scroll.addView(llItems);
        scroll.setScrollbarFadingEnabled(false);
        llItems.setOrientation(LinearLayout.VERTICAL);
        llContent.removeAllViews();
        View view = new View(mContext);
        view.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,
                ScreenUtil.instance(mContext).dip2px(mContext, 0.5f)));
        view.setBackgroundColor(mContext.getResources().getColor(
                R.color.content_divider));
        llContent.addView(view);
        llContent.addView(scroll, new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT));
        // 设置ListView的按压效果
        for (int i = 0; i < items.length; i++) {
            final int itemPos = i;
            TextView item = (TextView) LayoutInflater.from(mContext).inflate(
                    R.layout.common_listview_item, null);
            if (itemPos == 0) {
                if (itemPos == (items.length - 1)) {
                    // 只有一项
                    item.setBackgroundResource(R.drawable.actionsheet_bottom_selector);
                } else {
                    // 第一项
                    item.setBackgroundResource(R.drawable.actionsheet_middle_selector);
                }
            } else if (itemPos == (items.length - 1))
                // 最后一项
                if (itemPos < 8) {
                    item.setBackgroundResource(R.drawable.actionsheet_bottom_selector);
                } else {
                    item.setBackgroundResource(R.drawable.actionsheet_middle_selector);
                }
            else {
                // 中间一项
                item.setBackgroundResource(R.drawable.actionsheet_middle_selector);
            }
            item.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    if (mItemClickListener != null) {
                        mItemClickListener.onItemClick(null, null, itemPos, 0);
                    }
                    dismiss();
                }
            });
            // 显示文案支持 String 和Spanned类型
            if (items[itemPos] instanceof String) {
                String text = (String) items[itemPos];
                item.setText(text);
            } else if (items[itemPos] instanceof Spanned) {
                Spanned sp = (Spanned) items[itemPos];
                item.setText(sp);
            } else {
                // 其他类型的实体，返回对应得tostring，对应实体需要重写tostring方法
                String text = items[itemPos].toString();
                item.setText(text);
            }

            item.setPadding(ScreenUtil.instance(mContext).dip2px(mContext, 20), 0, ScreenUtil.instance(mContext).dip2px(mContext, 20), 0);
            llItems.addView(item);
        }
        return this;
    }

    /**
     * 设置自定义View
     *
     * @param v
     */
    public A_CustomAlertDialog setView(View v) {
        llContent.removeAllViews();
        llContent.addView(v);
        return this;
    }

    /**
     * 设置自定义View
     */
    public A_CustomAlertDialog setView(View v, boolean hasButtons) {
        llContent.removeAllViews();
        llContent.addView(v);
        if (hasButtons) {
            llButtons.setVisibility(View.VISIBLE);
            view_line.setVisibility(View.VISIBLE);
        } else {
            llButtons.setVisibility(View.GONE);
            view_line.setVisibility(View.INVISIBLE);
        }
        return this;
    }

    /**
     * 设置title
     *
     * @param text
     */
    public A_CustomAlertDialog setTitle(String text) {
        if (StringUtils.isEmpty(text)) {
            tvTitle.setVisibility(View.GONE);
        } else {
            tvTitle.setVisibility(View.VISIBLE);
            tvTitle.setText(text);
        }
        return this;
    }

    /**
     * 设置message
     *
     * @param text
     */
    public A_CustomAlertDialog setMessage(String text) {
        if (StringUtils.isEmpty(text)) {
            tvMessage.setVisibility(View.GONE);
        } else {
            tvMessage.setVisibility(View.VISIBLE);
            tvMessage.setText(text);
        }
        // 隐藏列表布局
        llContent.setVisibility(View.GONE);
        return this;
    }

    /**
     * 设置左边按钮文案和点击事件
     *
     * @param text
     * @param listener
     */
    public A_CustomAlertDialog setLeftButton(String text, View.OnClickListener listener) {
        tvLeftButton.setVisibility(View.VISIBLE);
        view_button.setVisibility(View.VISIBLE);
        tvLeftButton.setText(text);
        if (listener != null) {
            tvLeftButton.setOnClickListener(listener);
        } else {
            tvLeftButton.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    dismiss();
                }
            });
        }
        return this;
    }

    /**
     * 设置左边按钮文案和点击事件
     *
     * @param text
     * @param listener
     */
    public A_CustomAlertDialog setRightButton(String text, View.OnClickListener listener) {
        tvRightButton.setText(text);
        tvRightButton.setBackgroundResource(R.drawable.actionsheet_confirm_selector);
        if (listener != null) {
            tvRightButton.setOnClickListener(listener);
        }
        return this;
    }

    /**
     * 只显示一个按钮
     *
     * @param text
     * @param listener 如果null,则默认dismiss
     */
    public A_CustomAlertDialog showOneButton(String text, View.OnClickListener listener) {
        tvRightButton.setText(text);
        tvRightButton
                .setBackgroundResource(R.drawable.actionsheet_bottom_selector);
        if (listener != null) {
            tvRightButton.setOnClickListener(listener);
        } else {
            tvRightButton.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    dismiss();
                }
            });
        }
        tvLeftButton.setVisibility(View.GONE);
        view_button.setVisibility(View.GONE);
        return this;
    }

    /**
     * 仅设置Message
     *
     * @param text
     */
    public A_CustomAlertDialog setMessageOnly(String text) {
        tvMessage.setVisibility(View.VISIBLE);
        tvMessage.setText(StringUtils.isEmpty(text) ? "" : text);

        view_line.setVisibility(View.GONE);
        llContent.setVisibility(View.GONE);
        llButtons.setVisibility(View.GONE);

        LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) tvMessage
                .getLayoutParams();
        lp.bottomMargin = ScreenUtil.instance(mContext).dip2px(mContext, 15);
        tvMessage.setLayoutParams(lp);
        return this;
    }

    /**
     * 是否可以取消
     */
    public A_CustomAlertDialog setIsCancel(boolean cancelable) {
        super.setCancelable(cancelable);
        return this;
    }

    @Override
    public void dismiss() {
        if (!((Activity) mContext).isFinishing() && isShowing()) {
            super.dismiss();
        }
    }

    @Override
    public void show() {
        if (!((Activity) mContext).isFinishing() && !isShowing()) {
            super.show();
        }
    }

}
