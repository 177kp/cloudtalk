package com.zhangwuji.im;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import com.jfinal.kit.PropKit;
import com.zhangwuji.im.websocket.server.IMMsgServerManager;
import com.zhangwuji.im.websocket.server.IMWebSocketServerConfig;
import com.zhangwuji.im.websocket.server.IMWebSocketStarter;
import com.zhangwuji.im.websocket.server.IMWebSocketWsMsgHandler;

import javax.annotation.Resource;

@Component
@Configuration
public class StartTioRunner implements CommandLineRunner {

    private IMWebSocketWsMsgHandler tioWsMsgHandler;
    
    private IMWebSocketStarter appStarter;
    
	@Value("${IMAPPID}")
	public String IMAPPID;
	/*
	 * HttpMsgServer服务器的ip和端口，一般放在同一服务器最佳
	 */
	@Value("${HttpMsgServerIp}")
	public String HttpMsgServer="127.0.0.1";
	
	@Value("${HttpMsgServerPort}")
	public int HttpMsgServerPort=9900;
	/**
	 * 监听的ip
	 */
	@Value("${SERVER_IP}")
	public String SERVER_IP = null;//null表示监听所有，并不指定ip
	
	/**
	 * 监听端口 对Web开放的端口  ws/wss
	 */
	@Value("${SERVER_PORT}")
	public int SERVER_PORT = 9326;
	
	
    @Override
    public void run(String... args) throws Exception {
    	
//    	//从配置文件里面读取参数配置
//    			PropKit.use("app.properties");
//    			IMWebSocketServerConfig.instance().SERVER_IP=PropKit.get("SERVER_IP");
//    			IMWebSocketServerConfig.instance().SERVER_PORT=PropKit.getInt("SERVER_PORT");
//    			IMWebSocketServerConfig.instance().IMAPPID=PropKit.get("IMAPPID");
//    			IMWebSocketServerConfig.instance().HttpMsgServer=PropKit.get("HttpMsgServerIp");
//    			IMWebSocketServerConfig.instance().HttpMsgServerPort=PropKit.getInt("HttpMsgServerPort");
    	
    try
    {
    	//从springboot中读取配置文件
    	IMWebSocketServerConfig.instance().SERVER_IP=SERVER_IP;
		IMWebSocketServerConfig.instance().SERVER_PORT=SERVER_PORT;
		IMWebSocketServerConfig.instance().IMAPPID=IMAPPID;
		IMWebSocketServerConfig.instance().HttpMsgServer=HttpMsgServer;
		IMWebSocketServerConfig.instance().HttpMsgServerPort=HttpMsgServerPort;
        this.appStarter = new IMWebSocketStarter(IMWebSocketServerConfig.instance().SERVER_PORT, tioWsMsgHandler.me);
        appStarter.getWsServerStarter().start();
        
		IMMsgServerManager.instance().initConnectHttpServer();
		IMMsgServerManager.instance().initHeartBeat();
		
    }catch(Exception e) {}
    	
		
    }

}
