#ifndef __WEBSOCKET_RESPOND__
#define __WEBSOCKET_RESPOND__

#include <stdio.h>
#include <time.h>
#include <string.h>
#include <stdarg.h>
#include <stdint.h>
#include <arpa/inet.h>
#include "util.h"
#include "base64.h"
#include "sha1.h"
#include <iostream>
#include <sstream>
#include <arpa/inet.h>

#define MAGIC_KEY "258EAFA5-E914-47DA-95CA-C5AB0DC85B11"
typedef std::map<std::string, std::string> HEADER_MAP;

class Websocket_Respond {
public:
    HEADER_MAP header_map_;
	Websocket_Respond();
	~Websocket_Respond();
	uint64_t ntohll(uint64_t val);
	uint64_t htonll(uint64_t val);
    int fetch_http_info(char* buff_);
	char* toFrameDataPkt(const std::string &inMessage);
    void parse_str(char *request);
};

#endif
