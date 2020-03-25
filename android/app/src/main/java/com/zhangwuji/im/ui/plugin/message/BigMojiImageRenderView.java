package com.zhangwuji.im.ui.plugin.message;

import android.content.Context;
import android.content.res.Resources;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.alibaba.fastjson.JSON;
import com.dongtu.sdk.widget.DTImageView;
import com.dongtu.store.DongtuStore;
import com.zhangwuji.im.DB.entity.Message;
import com.zhangwuji.im.DB.entity.User;
import com.zhangwuji.im.R;
import com.zhangwuji.im.imcore.entity.MessageTag;
import com.zhangwuji.im.imcore.entity.TextMessage;
import com.zhangwuji.im.server.utils.json.JsonMananger;
import com.zhangwuji.im.ui.plugin.IMessageData;
import com.zhangwuji.im.ui.plugin.IMessageModule;
import com.zhangwuji.im.ui.plugin.message.entity.BigMojiMessage;
import com.zhangwuji.im.ui.widget.message.BaseMsgRenderView;
import org.json.JSONArray;
import com.dongtu.store.widget.DTStoreMessageView;

@MessageTag(value = "cloudtalk:bigmoji",messageContent=BigMojiMessage.class)
public class BigMojiImageRenderView extends BaseMsgRenderView implements IMessageModule {
    private DTStoreMessageView messageContent;
    private DTImageView dtImageView;

    public DTStoreMessageView getMessageContent()
    {
        return messageContent;
    }
    public static BigMojiImageRenderView inflater(Context context, ViewGroup viewGroup, boolean isMine){
        int resource = isMine? R.layout.tt_mine_bigmojiimage_message_item :R.layout.tt_other_bigmojiimage_message_item;
        BigMojiImageRenderView gifRenderView = (BigMojiImageRenderView) LayoutInflater.from(context).inflate(resource, viewGroup, false);
        gifRenderView.setMine(isMine);
        return gifRenderView;
    }

    public BigMojiImageRenderView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    protected void onFinishInflate() {
        super.onFinishInflate();
        messageContent = (DTStoreMessageView)findViewById(R.id.message_image);
        dtImageView= (DTImageView)findViewById(R.id.chat_item_content_dt_image);
    }

    /**
     * 控件赋值
     * @param messageEntity
     * @param userEntity
     */
    @Override
    public void render(IMessageData iMessageData,Message messageEntity, User userEntity, Context context) {
        super.render(iMessageData,messageEntity, userEntity,context);

        Resources r = context.getResources();
        float pixels=TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, (float) 120, r.getDisplayMetrics());

       String content=messageEntity.getContent();
       BigMojiMessage bigMojiMessage= JsonMananger.jsonToBean(content,BigMojiMessage.class);

        if(bigMojiMessage.getType().contains("#im_big_bqmm#")) {

            String facejson="[[\""+bigMojiMessage.getCode()+"\",\"1\"]]";
            JSONArray jsonArray=null;
            try {
                jsonArray= new JSONArray(facejson);
            } catch (Exception e) {}

            if (jsonArray != null) {
                messageContent.setVisibility(VISIBLE);
                messageContent.showSticker(bigMojiMessage.getCode());
                messageContent.setStickerSize((int)pixels);
              //  messageContent.setUnicodeEmojiSpanSizeRatio(1.5f);
                dtImageView.setVisibility(GONE);
            }
        }
        else if(bigMojiMessage.getType().contains("#im_gif_bqmm#")){
            try {
                dtImageView.setVisibility(VISIBLE);
                DongtuStore.loadImageInto(dtImageView, bigMojiMessage.getMainimage(),bigMojiMessage.getCode(),320,320);
                messageContent.getBackground().setAlpha(0);
                messageContent.setVisibility(GONE);
            } catch (Exception e) {}
        }
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
    public  View messageRender(IMessageData iMessageData, Object message, int position, View convertView, ViewGroup parent, boolean isMine) {
        BigMojiImageRenderView imageRenderView;
        final TextMessage imageMessage = (TextMessage)message;
        User userEntity = iMessageData.getImService().getContactManager().findContact(imageMessage.getFromId(),2);
        if (null != convertView && convertView.getClass().equals(BigMojiImageRenderView.class)) {
           imageRenderView = (BigMojiImageRenderView) convertView;
        } else {
           imageRenderView = BigMojiImageRenderView.inflater(iMessageData.getCtx(), parent, isMine);
        }

        imageRenderView.render(iMessageData,imageMessage, userEntity, iMessageData.getCtx());
        return imageRenderView;
    }
}
