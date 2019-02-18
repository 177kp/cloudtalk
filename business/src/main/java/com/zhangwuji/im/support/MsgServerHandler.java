package com.zhangwuji.im.support;

import java.io.IOException;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBufferInputStream;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;

import com.google.protobuf.CodedInputStream;
import com.zhangwuji.im.protobuf.IMBaseDefine;
import com.zhangwuji.im.protobuf.IMMessage;
import com.zhangwuji.im.protobuf.MessageEntity;
import com.zhangwuji.im.protobuf.base.DataBuffer;
import com.zhangwuji.im.protobuf.helper.ProtoBuf2JavaBean;
import com.zhangwuji.im.websocket.server.IMMsgServerManager;
import com.zhangwuji.im.websocket.server.IMWebSocketManager;

public class MsgServerHandler extends SimpleChannelHandler {


	@Override
	public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e)
			throws Exception {
		
		IMWebSocketManager.instance().channelConnectSucc();
		
		super.channelConnected(ctx, e);
	}

	@Override
	public void channelDisconnected(ChannelHandlerContext ctx,
			ChannelStateEvent e) throws Exception {
		
		IMWebSocketManager.instance().channelDisconnected();
		
        /**
         * 1. 已经与远程主机建立的连接，远程主机主动关闭连接，或者网络异常连接被断开的情况
         2. 已经与远程主机建立的连接，本地客户机主动关闭连接的情况
         3. 本地客户机在试图与远程主机建立连接时，遇到类似与connection refused这样的异常，未能连接成功时
         而只有当本地客户机已经成功的与远程主机建立连接（connected）时，连接断开的时候才会触发channelDisconnected事件，即对应上述的1和2两种情况。
         *
         **/
  		super.channelDisconnected(ctx, e);
  		
	}

	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e)
			throws Exception {
		super.messageReceived(ctx, e);
        // 重置AlarmManager的时间
        ChannelBuffer channelBuffer = (ChannelBuffer) e.getMessage();
        if(null!=channelBuffer)
        {
        	IMMsgServerManager.instance().packetDispatch(channelBuffer);
        }
	}
	
    /**
     * bug问题点:
     * exceptionCaught会调用断开链接， channelDisconnected 也会调用断开链接，事件通知冗余不合理。
     * a.另外exceptionCaught 之后channelDisconnected 依旧会被调用。 [切花网络方式]
     * b.关闭channel 也可能触发exceptionCaught
     * recvfrom failed: ETIMEDOUT (Connection timed out) 没有关闭长连接
     * */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception {
        super.exceptionCaught(ctx, e);
        if(e.getChannel() == null || !e.getChannel().isConnected()){
           // IMSocketManager.instance().onConnectMsgServerFail();
        }
    }
}
