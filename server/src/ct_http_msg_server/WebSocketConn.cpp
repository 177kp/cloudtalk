/*
 * WebSocketConn.cpp
 *
 *  Created on: 2019-12-10
 *  Author: cloudtalk
 */
#include "EncDec.h"
#include "DBServConn.h"
#include "WebSocketConn.h"
#include "HttpParserWrapper.h"
#include "HttpQuery.h"
#include <unistd.h>
#include <stdlib.h>
#include <inttypes.h>
#include "../base/Base64.h"
#include "IM.Server.pb.h"
#include "IM.Other.pb.h"
#include "ImPduBase.h"
#include "IM.Buddy.pb.h"
#include "IM.Group.pb.h"
#include "IM.Message.pb.h"
#include "IM.SwitchService.pb.h"
#include "IM.Login.pb.h"
#include "public_define.h"
#import "IM.BaseDefine.pb.h"
#include "AttachData.h"
#include "ImUser.h"
#include "security.h"
#include <cstdlib>
#include <memory>
#include <string>

using namespace std;
using namespace IM::BaseDefine;
static WebSocketConnMap_t g_websocket_conn_map;
extern CAes *pAes;
// conn_handle 从0开始递增，可以防止因socket handle重用引起的一些冲突
static uint32_t g_conn_handle_generator = 0;

CWebSocketConn* FindWebSocketConnByHandle(uint32_t conn_handle)
{
	CWebSocketConn* pConn = NULL;
	WebSocketConnMap_t::iterator it = g_websocket_conn_map.find(conn_handle);
    if (it != g_websocket_conn_map.end()) {
        pConn = it->second;
    }

    return pConn;
}

void CWebSocketConn::updateUserStatus(uint32_t user_status)
{
    CImUser* pImUser = CImUserManager::GetInstance()->GetImUserById(GetUserId());
    if (!pImUser) {
        return;
    } 

    CDBServConn* pDbConn = get_db_serv_conn();
    if (!pDbConn) {
        return;
	}

    IM::Server::IMUserStatusUpdate msg2;
    msg2.set_user_status(user_status);
    msg2.set_user_id(pImUser->GetUserId());
    msg2.set_client_type(::IM::BaseDefine::ClientType::CLIENT_TYPE_WEB);
    CImPdu pdu2;
    pdu2.SetPBMsg(&msg2);
    pdu2.SetServiceId(SID_OTHER);
    pdu2.SetCommandId(CID_OTHER_USER_STATUS_UPDATE);
    pDbConn->SendPdu(&pdu2);//发送到数据库在线状态
}

void websocketconn_callback(void* callback_data, uint8_t msg, uint32_t handle, uint32_t uParam, void* pParam)
{
	NOTUSED_ARG(uParam);
	NOTUSED_ARG(pParam);

	// convert void* to uint32_t, oops
	uint32_t conn_handle = *((uint32_t*)(&callback_data));
	CWebSocketConn* pConn = FindWebSocketConnByHandle(conn_handle);
    if (!pConn) {
        return;
    }

	switch (msg)
	{
	case NETLIB_MSG_READ:
		pConn->OnRead();
		break;
	case NETLIB_MSG_WRITE:
		pConn->OnWrite();
		break;
	case NETLIB_MSG_CLOSE:
		pConn->OnClose();
		break;
	default:
		log("!!!httpconn_callback error msg: %d ", msg);
		break;
	}
}

void websocket_conn_timer_callback(void* callback_data, uint8_t msg, uint32_t handle, void* pParam)
{
	CWebSocketConn* pConn = NULL;
	WebSocketConnMap_t::iterator it, it_old;
	uint64_t cur_time = get_tick_count();

	for (it = g_websocket_conn_map.begin(); it != g_websocket_conn_map.end(); ) {
		it_old = it;
		it++;

		pConn = it_old->second;
		pConn->OnTimer(cur_time);
	}
}

void init_websocket_conn()
{
	netlib_register_timer(websocket_conn_timer_callback, NULL, 5000);
}

//////////////////////////
CWebSocketConn::CWebSocketConn()
{
	m_busy = false;
	m_conn_handle= NETLIB_INVALID_HANDLE;
    m_state = WS_STATE_UNCONNECTED;
	ws_request=new Websocket_Request();
	ws_respond=new Websocket_Respond();

	m_last_send_tick = m_last_recv_tick = get_tick_count();
	m_conn_handle = ++g_conn_handle_generator;
	if (m_conn_handle == 0) {
		m_conn_handle = ++g_conn_handle_generator;
	}

	//log("CHttpConn, handle=%u ", m_conn_handle);
}

CWebSocketConn::~CWebSocketConn()
{
	//log("~CHttpConn, handle=%u ", m_conn_handle);
}

int CWebSocketConn::Send(void* data, int len)
{
	int ret = netlib_send(m_conn_handle, data, len);
	return len;
}

void CWebSocketConn::Close()
{

	CImUser *pImUser = CImUserManager::GetInstance()->GetImUserById(GetUserId());
	if (pImUser) {
		updateUserStatus(::IM::BaseDefine::USER_STATUS_OFFLINE);
		pImUser->DelMsgConn(GetConnHandle());
		pImUser->DelUnValidateMsgConn(this);
		if (pImUser->IsMsgConnEmpty()) {
			CImUserManager::GetInstance()->RemoveImUser(pImUser);
		}
	}

	pImUser = CImUserManager::GetInstance()->GetImUserByLoginName(GetLoginName());
	if (pImUser) {
		pImUser->DelUnValidateMsgConn(this);
		if (pImUser->IsMsgConnEmpty()) {
			CImUserManager::GetInstance()->RemoveImUser(pImUser);
		}
	}

    if (m_state != WS_STATE_CLOSED) {
        m_state = WS_STATE_CLOSED;

		g_websocket_conn_map.erase(m_conn_handle);
        netlib_close(m_conn_handle);
        
        ReleaseRef();
    }
}

void CWebSocketConn::OnConnect(net_handle_t handle)
{
    printf("new client OnConnect, handle=%d\n", handle);

	isHandshark= false;
	m_conn_handle = handle;
    m_state = WS_STATE_CONNECTED;
	g_websocket_conn_map.insert(make_pair(m_conn_handle, this));
    
    netlib_option(handle, NETLIB_OPT_SET_CALLBACK, (void*)websocketconn_callback);
    netlib_option(handle, NETLIB_OPT_SET_CALLBACK_DATA, reinterpret_cast<void *>(m_conn_handle) );
    netlib_option(handle, NETLIB_OPT_GET_REMOTE_IP, (void*)&m_peer_ip);
}

void CWebSocketConn::OnRead()
{
    m_in_buf = *new CSimpleBuffer();
	for (;;)
	{
		uint32_t free_buf_len = m_in_buf.GetAllocSize() - m_in_buf.GetWriteOffset();
		if (free_buf_len < READ_BUF_SIZE + 1)
			m_in_buf.Extend(READ_BUF_SIZE + 1);

		int ret = netlib_recv(m_conn_handle, m_in_buf.GetBuffer() + m_in_buf.GetWriteOffset(), READ_BUF_SIZE);
		if (ret <= 0)
			break;

		m_in_buf.IncWriteOffset(ret);

		m_last_recv_tick = get_tick_count();
	}

	char* in_buf = (char*)m_in_buf.GetBuffer();
	uint32_t buf_len = m_in_buf.GetWriteOffset();


	//先握手。
	if (isHandshark == false) {

		strcpy(buff_,in_buf);
		m_HttpParser.ParseHttpContent(in_buf, buf_len);
		char *b = "Connection: Upgrade";
		string content = m_HttpParser.GetBodyContent();
		if (strstr(in_buf, b) != NULL) {
			char *c = "Sec-WebSocket-Key:";
		    if (strstr(in_buf, c) != NULL) {

				char request[1024] = {};
				ws_respond->fetch_http_info(buff_);
				ws_respond->parse_str(request);

				//发送握手回复
				Send(request, (uint32_t)strlen(request));

				isHandshark = true;
				m_state = WS_STATE_HANDSHARK;
				memset(buff_, 0, sizeof(buff_));
		   }
		   else
		   {
			 	Close();
		   }
		}
		else
		{
			Close();
		}
		return;
	}

    ws_request->reset();
    ws_request->fetch_websocket_info(in_buf);

    if(in_buf)
    {
        memset(in_buf, 0, sizeof(in_buf));
    }

	if(ws_request->opcode_==0x1) {

        if(buf_len>20480) //大于1W长度的数据，不处理
        {

			SendMessageToWS("{\"code\":-1,\"messages\":\"文本长度超过限制!请分隔发送!\"}");
        }
        else {
			std::string reqData=ws_request->payload_;
            if(reqData.empty())
            {
                return;
            }
			else if(reqData.size()<10)//心跳处理
			{
				updateUserStatus(::IM::BaseDefine::USER_STATUS_ONLINE);
                return;
			}
			HandlePdu(reqData);
        }
	}
	else if(ws_request->opcode_==0x8) {//连接关闭
		Close();
	}


}

void CWebSocketConn::HandlePdu(std::string src_data)
{
    Json::Value value;
    Json::Value root;
	Json::Reader *reader = new Json::Reader(Json::Features::strictMode());
    if (!reader->parse(src_data, value) ) {
        log("json parse failed, data=%s ", src_data.c_str());
        return;
    }
    log("rec, data=%s ", src_data.c_str());

    int commandId = value["commandID"].asInt();
    switch (commandId) {
        case CID_OTHER_HEARTBEAT:
            break;
        case CID_LOGIN_REQ_USERLOGIN:
            _HandleLoginRequest(value);
            break;
    	case CID_MSG_DATA:
    		_HandleMsgDataRequest(value);
        default:
            log("wrong msg, cmd id=%d ",commandId);
            break;
    }
}

/**
 * 发送聊天信息方法
 * @param json_msg
 */

void CWebSocketConn::_HandleMsgDataRequest(Json::Value &json_msg) {


	IM::Message::IMMsgData msg;
	std::string msg_data = json_msg["content"].asString();
	/*
	std::string pOutData = "";
	char *pOutDataTemp = 0;
	uint32_t nOutLen = 0;
	int retCode = pAes->Encrypt(msg_data.c_str(), msg_data.length(), &pOutDataTemp, nOutLen);
	if (retCode == 0 && nOutLen > 0 && pOutDataTemp != 0) {
		pOutData = std::string(pOutDataTemp, nOutLen);
		Free(pOutDataTemp);
	} else {
		pOutData = msg_data;
	}
	*/

	msg.set_msg_data(msg_data);
	msg.set_msg_type((IM::BaseDefine::MsgType)json_msg["msgType"].asInt());
	msg.set_msg_id(0);
	msg.set_from_user_id(json_msg["fromId"].asInt());
	msg.set_to_session_id(json_msg["toId"].asInt());
	msg.set_session_type((IM::BaseDefine::SessionType)json_msg["sessionType"].asInt());
	uint32_t cur_time = time(NULL);
	msg.set_create_time(cur_time);

	CImPdu pdu;
	CDbAttachData attach_data(ATTACH_TYPE_HANDLE, m_conn_handle);
	msg.set_attach_data(attach_data.GetBuffer(), attach_data.GetLength());
	pdu.SetPBMsg(&msg);
	pdu.SetServiceId(IM::BaseDefine::SID_MSG);
	pdu.SetCommandId(IM::BaseDefine::CID_MSG_DATA);
	CDBServConn *pConn =get_db_serv_conn();
	if (pConn) {
		pConn->SendPdu(&pdu);
	}
}

/**
 * 用户登录方法
 * @param json_msg
 */

void CWebSocketConn::_HandleLoginRequest(Json::Value &json_msg) {

	uint32_t result = 0;
	string result_string = "";

	CDBServConn *pConn = get_db_serv_conn();


	if (!pConn || json_msg["username"].isNull() || json_msg["token"].isNull()) {

		SendMessageToWS("{\"code\":-1,\"messages\":\"系统错误!\"}");
		Close();
		return;
	}

	IM::Login::IMLoginReq msg;

	string loginname = json_msg["username"].asString();
	string password = json_msg["token"].asString();

	m_login_name = loginname;
	uint32_t online_status = msg.online_status();
	if (online_status < IM::BaseDefine::USER_STATUS_ONLINE || online_status > IM::BaseDefine::USER_STATUS_LEAVE) {
		log("HandleLoginReq, online status wrong: %u ", online_status);
		online_status = IM::BaseDefine::USER_STATUS_ONLINE;
	}

	CImUser *pImUser = CImUserManager::GetInstance()->GetImUserByLoginName(loginname);
	if (!pImUser) {
		pImUser = new CImUser(loginname);
		pImUser->SetHandle(m_conn_handle);
		CImUserManager::GetInstance()->AddImUserByLoginName(loginname, pImUser);
	}

	pImUser->AddUnValidateMsgConn(this);

	CDbAttachData attach_data(ATTACH_TYPE_HANDLE, m_conn_handle, 0);
	// continue to validate if the user is OK

	IM::Server::IMValidateReq msg2;
	msg2.set_user_name(loginname);
	msg2.set_password(password);
	msg2.set_attach_data(attach_data.GetBuffer(), attach_data.GetLength());
	CImPdu pdu;
	pdu.SetPBMsg(&msg2);
	pdu.SetServiceId(SID_OTHER);
	pdu.SetCommandId(CID_OTHER_VALIDATE_REQ);
	pdu.SetSeqNum(0);
	pConn->SendPdu(&pdu);
}

void CWebSocketConn::SendMessageToWS(const char* msg)
{

	printf("发送消息: %s \r\n ",msg);
	toFrameDataPkt(msg);
}


void CWebSocketConn::toFrameDataPkt(const std::string &data) {
	std::string outFrame;
	uint32_t messageLength = data.size();
	if (messageLength > 20000) {
		return;
	}
	uint8_t payloadFieldExtraBytes = (messageLength <= 0x7d) ? 0 : 2;
	// header: 2字节, mask位设置为0(不加密), 则后面的masking key无须填写, 省略4字节
	uint8_t frameHeaderSize = 2 + payloadFieldExtraBytes;
	uint8_t *frameHeader = new uint8_t[frameHeaderSize];
	memset(frameHeader, 0, frameHeaderSize);
	// fin位为1, 扩展位为0, 操作位为frameType
	frameHeader[0] = static_cast<uint8_t>(0x80 | 0x1);
	if (messageLength <= 0x7d) {
		frameHeader[1] = static_cast<uint8_t>(messageLength);
	} else {
		frameHeader[1] = 0x7e;
		uint16_t len = htons(messageLength);
		memcpy(&frameHeader[2], &len, payloadFieldExtraBytes);
	}

	// 填充数据
	uint32_t frameSize = frameHeaderSize + messageLength;
	char *frame = new char[frameSize];
	memcpy(frame, frameHeader, frameHeaderSize);
	memcpy(frame + frameHeaderSize, data.c_str(), messageLength);
	//frame[frameSize] = '\0';

	Send(frame, frameSize);
	delete[] frame;
}


void CWebSocketConn::OnWrite()
{
}

void CWebSocketConn::OnClose()
{
    Close();
}

void CWebSocketConn::OnTimer(uint64_t curr_tick)
{
	if (curr_tick > m_last_recv_tick + WEBSOCKET_CONN_TIMEOUT) {
		log("WebSocketConn timeout, handle=%d ", m_conn_handle);
		Close();
	}
}

void CWebSocketConn::OnWriteCompelete()
{
   // Close();
}

