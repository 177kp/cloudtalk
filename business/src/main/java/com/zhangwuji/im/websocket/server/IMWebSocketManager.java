package com.zhangwuji.im.websocket.server;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBufferInputStream;
import org.tio.core.ChannelContext;
import org.tio.core.GroupContext;
import org.tio.core.Tio;
import org.tio.websocket.common.WsResponse;

import com.alibaba.fastjson.JSONObject;
import com.google.protobuf.ByteString;
import com.google.protobuf.CodedInputStream;
import com.google.protobuf.GeneratedMessageLite;
import com.google.protobuf.GeneratedMessageV3;
import com.zhangwuji.im.protobuf.IMBaseDefine;
import com.zhangwuji.im.protobuf.IMLogin;
import com.zhangwuji.im.protobuf.IMMessage;
import com.zhangwuji.im.protobuf.IMOther;
import com.zhangwuji.im.protobuf.IMSwitchService;
import com.zhangwuji.im.callback.ListenerQueue;
import com.zhangwuji.im.callback.Packetlistener;
import com.zhangwuji.im.config.SysConstant;
import com.zhangwuji.im.protobuf.MessageEntity;
import com.zhangwuji.im.protobuf.TextMessage;
import com.zhangwuji.im.protobuf.base.DataBuffer;
import com.zhangwuji.im.protobuf.base.DefaultHeader;
import com.zhangwuji.im.protobuf.helper.Java2ProtoBuf;
import com.zhangwuji.im.protobuf.helper.ProtoBuf2JavaBean;
import com.zhangwuji.im.support.JsonKit;
import com.zhangwuji.im.support.MsgServerHandler;
import com.zhangwuji.im.support.SocketThread;

public class IMWebSocketManager 
{
	private static IMWebSocketManager inst = new IMWebSocketManager();
	public static IMWebSocketManager instance() {
	        return inst;
	}
	public GroupContext groupContext=null;
	public Hashtable allUserClient = new Hashtable(); //所有用户的列表
	public Hashtable un_auth_allUserClient = new Hashtable(); //所有用户的列表
	
	public void channelConnectSucc()
	{
		 //断开重连后。需要断开用户链接。。
		 for(Iterator<String> iterator=allUserClient.keySet().iterator();iterator.hasNext();){
			 String key=iterator.next();
			 Tio.close((ChannelContext)allUserClient.get(key),"");
		 }
	}
	
	public void channelDisconnected()
	{
	}
   
	public void addun_auth_User(String userid, ChannelContext channelContext)
	{
		if(un_auth_allUserClient.containsKey(userid))
		{
			un_auth_allUserClient.remove(userid);
		}
		un_auth_allUserClient.put(userid, channelContext);
	}
	
	//认证成功后，加入到用户列表
	public void addUser(String userid, ChannelContext channelContext)
	{
		if(allUserClient.containsKey(userid))
		{
			allUserClient.remove(userid);
		}
		allUserClient.put(userid, channelContext);
	}
	
	public void setgroupContext(GroupContext groupContext)
	{
	   this.groupContext=groupContext;
	}
	
	/*
	 * 解析从Web发送过来的消息，对cid和sid进行相应解析并做处理
	 */
	public void parseWebClientCMD(String text,ChannelContext channelContext)
	{
		JSONObject obj=(JSONObject)JSONObject.parse(text);
		int sid=obj.getIntValue("serviceID"); 
		int cid=obj.getIntValue("commandID");
		
		switch (sid){
          case IMBaseDefine.ServiceID.SID_MSG_VALUE:
              {
            	  packetDispatchMessage(cid,obj);
              }
              break;
          case IMBaseDefine.ServiceID.SID_LOGIN_VALUE:
          {
        	  switch (cid){
    		  case IMBaseDefine.LoginCmdID.CID_LOGIN_REQ_USERLOGIN_VALUE:
                {
                	String username=obj.getString("username");
                	String token=obj.getString("token");
                	
                	Tio.bindUser(channelContext,"unauth_"+username); //先绑定这个未认证的用户。
            		addun_auth_User(username, channelContext);
                	sendUserLogin(username,token);
                	
                }break;
              default:
                break;
        	  }
          }
          break;
          default:	
              break;
      }
	}

	/*
	 * Web端发送过来的消息，经处理后将消息转发到HttpMsgServer
	 */
	public void packetDispatchMessage(int commandID,JSONObject obj){
		switch (commandID){
		  case IMBaseDefine.MessageCmdID.CID_MSG_DATA_VALUE:
            {
            	String content=obj.getString("content");
            	int fromId=obj.getIntValue("fromId");
            	int toId=obj.getIntValue("toId");
            	int sessionType=obj.getIntValue("sessionType");
            	int msgType=obj.getIntValue("msgType");
            	
            	//文字消息
            	if(msgType==1)
            	{
            		
                 TextMessage textMessage = TextMessage.buildForSend(content, fromId, toId,sessionType);
            	  IMBaseDefine.MsgType msgType2 = Java2ProtoBuf.getProtoMsgType(textMessage.getMsgType());
                  byte[] sendContent = textMessage.getSendContent();
                  IMMessage.IMMsgData msgData = IMMessage.IMMsgData.newBuilder()
                          .setFromUserId(textMessage.getFromId())
                          .setToSessionId(textMessage.getToId())
                          .setMsgId(0)
                          .setCreateTime(textMessage.getCreated())
                          .setMsgType(msgType2)
                          .setMsgData(ByteString.copyFrom(sendContent))  // 这个点要特别注意 todo ByteString.copyFrom
                          .build();
                  int sid = IMBaseDefine.ServiceID.SID_MSG_VALUE;
                  int cid = IMBaseDefine.MessageCmdID.CID_MSG_DATA_VALUE;
                  
                  IMMsgServerManager.instance().sendRequestToMsgServer(msgData,sid,cid,new Packetlistener() {
       	           @Override
       	           public void onSuccess(Object response) {
       	               try {
       	            	sendMessageToWebClient(fromId+"","消息发送成功!");
       	               } catch (Exception e) {}
       	           }
       	           @Override
       	           public void onFaild() {
       	           }
       	           @Override
       	           public void onTimeout() {
       	           }
       	          });
            	}
            	
            }
            break;
        default:	
            break;
        }
	}


	/*
	 * 发送登录请求到HttpMsgServer
	 */
	public void sendUserLogin(String username,String token)
	{
		try {
			 IMLogin.IMLoginReq imLoginReq = IMLogin.IMLoginReq.newBuilder()
	                    .setUserId(username)
	                    .setToken(token)
	                    .setAppid(IMWebSocketServerConfig.instance().IMAPPID)
	                    .setUserName("")
	                    .setPassword("")
	                    .setOnlineStatus(IMBaseDefine.UserStatType.USER_STATUS_ONLINE)
	                    .setClientType(IMBaseDefine.ClientType.CLIENT_TYPE_WINDOWS)
	                    .setClientVersion("2.0.1").build();
			 
	       int sid = IMBaseDefine.ServiceID.SID_LOGIN_VALUE;
	       int cid = IMBaseDefine.LoginCmdID.CID_LOGIN_REQ_USERLOGIN_VALUE;
	       IMMsgServerManager.instance().sendRequestToMsgServer(imLoginReq,sid,cid,new Packetlistener() {
	           @Override
	           public void onSuccess(Object response) {
	               try {
	            	   IMLogin.IMLoginRes  loginRes = IMLogin.IMLoginRes.parseFrom((CodedInputStream)response);
	                   if (loginRes == null) {
	                       return;
	                   }
	                   IMBaseDefine.ResultType  code = loginRes.getResultCode();
	                   switch (code){
	                       case REFUSE_REASON_NONE:
	                       {
	                           IMBaseDefine.UserStatType userStatType = loginRes.getOnlineStatus();
	                           IMBaseDefine.UserInfo userInfo =  loginRes.getUserInfo();
	                           int loginId = userInfo.getUserId();
	                           
	                           if(un_auth_allUserClient.containsKey(username))
	                           {
	                              //这里的流程--先解除之前绑定的ChannelContext,然后将获取到的uid绑定起来
	                              ChannelContext channelContext=(ChannelContext)un_auth_allUserClient.get(username);
	                              Tio.unbindUser(channelContext);
	                              Tio.bindUser(channelContext, loginId+"");
	                              un_auth_allUserClient.remove(username);
	                              addUser(loginId+"",channelContext);
	                              sendMessageToWebClient(loginId+"","{\"serviceID\":1,\"commandID\":260,\"code\":1,\"msg\":\"登录验证成功!\"}");
	                           }
	                           
	                       }break;
	                       case REFUSE_REASON_DB_VALIDATE_FAILED:{
	                    	   ChannelContext channelContext=(ChannelContext)un_auth_allUserClient.get(username);
	                    	   sendMessageToWebClient("unauth_"+username,"{\"serviceID\":1,\"commandID\":260,\"code\":-1,\"msg\":\"登录验证失败!请关闭链接，重新提交登录!\"}");
	                       }break;
	                       default:{
	                       }break;
	                   }
	                  
	               } catch (Exception e) {}
	               
	           }

	           @Override
	           public void onFaild() {
	           }

	           @Override
	           public void onTimeout() {
	           }
	       });
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/*
	 * 发送消息到Web前端
	 */
	public void sendMessageToWebClient(String userId,String Messages)
	{
		WsResponse wsResponse = WsResponse.fromText(Messages, IMWebSocketServerConfig.instance().CHARSET);
		Tio.sendToUser(groupContext, userId, wsResponse);
	}
	
}
