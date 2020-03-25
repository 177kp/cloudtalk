/*
 * WebSocketConn.h
 *
 *  Created on: 2019-12-10
 *      Author: cloudtalk
 */

#ifndef __WEBSOCKET_CONN_H__
#define __WEBSOCKET_CONN_H__

#include "netlib.h"
#include "json/json.h"
#include "util.h"
#include "HttpParserWrapper.h"
#include "websocket_request.h"
#include "websocket_respond.h"
#include "sha1.h"
#include "base64.h"
#include <sstream>
#include <string.h>
#include <arpa/inet.h>
#include <iostream>
#include "imconn.h"
#include "ServInfo.h"

#define WEBSOCKET_CONN_TIMEOUT   60000

#define READ_BUF_SIZE	2048


enum {
	WS_STATE_UNCONNECTED,
	WS_STATE_CONNECTED,
	WS_STATE_HANDSHARK,
	WS_STATE_AUTH,
	WS_STATE_CLOSED,
};

// 数据包操作类型
enum WebSocketOpCode {
	ContinuationFrame = 0x0,				//连续帧
	TextFrame = 0x1,					//文本帧
	BinaryFrame = 0x2,					//二进制帧
	ConnectionClose = 0x8,					//连接关闭
	Ping = 0x9,
	Pong = 0xA
};


//Websocket数据包数据头信息
struct WebSocketStreamHeader {
	unsigned int header_size;                //数据包头大小
	int mask_offset;                    //掩码偏移
	unsigned int payload_size;                //数据大小
	bool fin;                                               //帧标记
	bool masked;                            //掩码
	unsigned char opcode;                    //操作码
	unsigned char res[3];
};



class CWebSocketConn : public CRefObject
{

public:
	CWebSocketConn();
	virtual ~CWebSocketConn();

    void updateUserStatus(uint32_t user_status);
	string GetLoginName() { return m_login_name; }
	uint32_t GetUserId() { return m_user_id; }
	void SetUserId(uint32_t user_id) { m_user_id = user_id; }
	uint32_t GetConnHandle() { return m_conn_handle; }
    void HandlePdu(std::string src_data);
    void _HandleLoginRequest(Json::Value &json_msg);
	void _HandleMsgDataRequest(Json::Value &json_msg);
	void toFrameDataPkt(const std::string &data);
	char* GetPeerIP() { return (char*)m_peer_ip.c_str(); }
	int Send(void* data, int len);
    void Close();
    void OnConnect(net_handle_t handle);
    void OnRead();
    void OnWrite();
    void OnClose();
    void OnTimer(uint64_t curr_tick);
	void SendMessageToWS(const char* msg);
	char buff_[2048];
	int fd_;
	bool isHandshark;
    
    virtual void OnWriteCompelete();

protected:

	string          m_login_name;        //登录名拼音
	uint32_t        m_user_id;
	uint32_t		m_conn_handle;
	bool			m_busy;

    uint32_t        m_state;
	std::string		m_peer_ip;
	uint16_t		m_peer_port;
	CSimpleBuffer	m_out_buf;
    CSimpleBuffer   m_in_buf;

    uint64_t		m_last_send_tick;
	uint64_t		m_last_recv_tick;
    
    CHttpParserWrapper m_HttpParser;
	Websocket_Request *ws_request;
    Websocket_Respond *ws_respond;
};

typedef hash_map<uint32_t, CWebSocketConn*> WebSocketConnMap_t;

CWebSocketConn* FindWebSocketConnByHandle(uint32_t handle);
void init_websocket_conn();

#endif /* IMCONN_H_ */
