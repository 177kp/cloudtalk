package com.zhangwuji.im.websocket.server;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.tio.utils.time.Time;

@Configuration
public  class IMWebSocketServerConfig {
	/**
	 * 协议名字(可以随便取，主要用于开发人员辨识)
	 */
	public String PROTOCOL_NAME = "cloudtalkwebsocket";
	public String CHARSET = "utf-8";
	/**
	 * AppID 标识此服务的默认AppId
	 */
	public String IMAPPID="88888";
	/*
	 * HttpMsgServer服务器的ip和端口，一般放在同一服务器最佳
	 */
	public String HttpMsgServer="127.0.0.1";
	
	public int HttpMsgServerPort=9900;
	/**
	 * 监听的ip
	 */
	public String SERVER_IP = null;//null表示监听所有，并不指定ip
	/**
	 * 监听端口 对Web开放的端口  ws/wss
	 */
	public int SERVER_PORT = 9326;
	/**
	 * 心跳超时时间，单位：毫秒
	 */
	public int HEARTBEAT_TIMEOUT = 1000 * 10;

	/**
	 * ip数据监控统计，时间段
	 * @author tanyaowu
	 *
	 */
	public static interface IpStatDuration {
		public static final Long DURATION_1 = Time.MINUTE_1 * 5;
		public static final Long[] IPSTAT_DURATIONS = new Long[] { DURATION_1 };
	}
	private static IMWebSocketServerConfig inst = new IMWebSocketServerConfig();
	public static IMWebSocketServerConfig instance() {
	        return inst;
	}

}
