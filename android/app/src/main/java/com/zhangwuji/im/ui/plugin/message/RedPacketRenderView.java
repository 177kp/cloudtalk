package com.zhangwuji.im.ui.plugin.message;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.media.MediaPlayer;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.qmuiteam.qmui.widget.dialog.QMUITipDialog;
import com.zhangwuji.im.DB.entity.Message;
import com.zhangwuji.im.DB.entity.User;
import com.zhangwuji.im.R;
import com.zhangwuji.im.imcore.entity.MessageTag;
import com.zhangwuji.im.imcore.entity.TextMessage;
import com.zhangwuji.im.server.network.BaseAction;
import com.zhangwuji.im.server.network.IMAction;
import com.zhangwuji.im.server.utils.json.JsonMananger;
import com.zhangwuji.im.ui.activity.AMAPLocationActivity;
import com.zhangwuji.im.ui.activity.RedPacketResultActivity;
import com.zhangwuji.im.ui.entity.RedPacketLogBean;
import com.zhangwuji.im.ui.helper.ApiAction;
import com.zhangwuji.im.ui.plugin.IMessageData;
import com.zhangwuji.im.ui.plugin.IMessageModule;
import com.zhangwuji.im.ui.plugin.message.entity.LocationMessage;
import com.zhangwuji.im.ui.plugin.message.entity.RedPacketMessage;
import com.zhangwuji.im.ui.widget.BubbleImageView;
import com.zhangwuji.im.ui.widget.CTMessageFrameLayout;
import com.zhangwuji.im.ui.widget.IMBaseImageView;
import com.zhangwuji.im.ui.widget.message.BaseMsgRenderView;
import com.zhangwuji.im.utils.AvatarGenerate;

import org.json.JSONException;
import org.json.JSONObject;


@MessageTag(value = "cloudtalk:redpacket",messageContent=RedPacketMessage.class)
public class RedPacketRenderView extends BaseMsgRenderView implements IMessageModule {

    public IMessageData iMessageData;
    private CTMessageFrameLayout mLayout;
    private TextView tv_detail;
    private TextView tv_getredpacket;

    public Context mcontext;
    private Dialog dialog;
    private boolean isdialog = false;
    private Button bt_qiang;
    private LinearLayout ll_close;
    private IMBaseImageView iv_touxiang;
    private ImageView iv_donghua;
    private TextView tv_name,tv_info_detail;
    private LinearLayout ll_chakanjilu;
    protected ApiAction action;
    private String money = "";
    private ProgressDialog pd;
    private String testtext = "";
    private QMUITipDialog tipDialog;

    public static RedPacketRenderView inflater(Context context, ViewGroup viewGroup, boolean isMine){

        int resource = isMine? R.layout.tt_mine_redpacket_item :R.layout.tt_other_redpacket_item;
        RedPacketRenderView gifRenderView = (RedPacketRenderView) LayoutInflater.from(context).inflate(resource, viewGroup, false);
        gifRenderView.setMine(isMine);
        return gifRenderView;
    }

    public RedPacketRenderView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    protected void onFinishInflate() {
        super.onFinishInflate();

    }

    /**
     * 控件赋值
     * @param messageEntity
     * @param userEntity
     */
    @Override
    public void render(IMessageData iMessageData2,final Message messageEntity, final User userEntity, final Context context) {
        super.render(iMessageData2,messageEntity, userEntity,context);
        iMessageData=iMessageData2;


    }


    /**
     * 自定义对话框布局
     *
     * @param context     :上下文
     * @param layout      ：显示布局
     * @param windowStyle ：windows显示的样式
     */
    public void showCustomDialog(final IMessageData iMessageData2,final User userEntity,final Context context, int layout, int windowStyle, boolean cancelable, final RedPacketMessage content,final int type) {

    }



    @Override
    public void msgFailure(Message messageEntity) {
        super.msgFailure(messageEntity);
    }


    /**----------------set/get---------------------------------*/
    public void setMine(boolean isMine) {
        this.isMine = isMine;
    }


    public void setParentView(ViewGroup parentView) {
        this.parentView = parentView;
    }

    @Override
    public  View messageRender(IMessageData iMessageData2, Object message, int position, View convertView, ViewGroup parent, boolean isMine2) {

        return null;
    }
}
