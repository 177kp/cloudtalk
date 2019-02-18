package com.zhangwuji.im.websocket.server;

import java.io.IOException;

import org.springframework.stereotype.Component;
import org.tio.server.ServerGroupContext;
import org.tio.websocket.server.WsServerStarter;
import com.jfinal.kit.PropKit;

public class IMWebSocketStarter {

	private WsServerStarter wsServerStarter;
	private ServerGroupContext serverGroupContext;
	
	public IMWebSocketStarter(int port, IMWebSocketWsMsgHandler wsMsgHandler) throws Exception {
		wsServerStarter = new WsServerStarter(port, wsMsgHandler);

		serverGroupContext = wsServerStarter.getServerGroupContext();
		serverGroupContext.setName(IMWebSocketServerConfig.instance().PROTOCOL_NAME);
	//	serverGroupContext.setServerAioListener(IMWebSocketServerAioListener.me);

		//设置ip监控
		//serverGroupContext.setIpStatListener(IMWebSocketIpStatListener.me);
		//设置ip统计时间段
		//serverGroupContext.ipStats.addDurations(IMWebSocketServerConfig.IpStatDuration.IPSTAT_DURATIONS);
		
		//设置心跳超时时间
		serverGroupContext.setHeartbeatTimeout(IMWebSocketServerConfig.instance().HEARTBEAT_TIMEOUT);
		//如果你希望通过wss来访问，就加上下面的代码吧，不过首先你得有SSL证书（证书必须和域名相匹配，否则可能访问不了ssl）
		String keyStoreFile = "classpath:config/ssl/imtt.b56.cn.jks";
		String trustStoreFile = "classpath:config/ssl/imtt.b56.cn.jks";
		String keyStorePwd = "123456";
		//serverGroupContext.useSsl(keyStoreFile, trustStoreFile, keyStorePwd);
	}

	/**
	 * @param args
	 * @author tanyaowu
	 * @throws IOException
	 */
	public static void start() throws Exception {
		IMWebSocketStarter appStarter = new IMWebSocketStarter(IMWebSocketServerConfig.instance().SERVER_PORT, IMWebSocketWsMsgHandler.me);
		appStarter.wsServerStarter.start();
	}

	/**
	 * @return the serverGroupContext
	 */
	public ServerGroupContext getServerGroupContext() {
		return serverGroupContext;
	}

	public WsServerStarter getWsServerStarter() {
		return wsServerStarter;
	}
	
	public static void main(String[] args) throws Exception {
		
		//从配置文件里面读取参数配置
		PropKit.use("app.properties");
		IMWebSocketServerConfig.instance().SERVER_IP=PropKit.get("SERVER_IP");
		IMWebSocketServerConfig.instance().SERVER_PORT=PropKit.getInt("SERVER_PORT");
		IMWebSocketServerConfig.instance().IMAPPID=PropKit.get("IMAPPID");
		IMWebSocketServerConfig.instance().HttpMsgServer=PropKit.get("HttpMsgServerIp");
		IMWebSocketServerConfig.instance().HttpMsgServerPort=PropKit.getInt("HttpMsgServerPort");
		start();
		IMMsgServerManager.instance().initConnectHttpServer();
		IMMsgServerManager.instance().initHeartBeat();
	}

}
