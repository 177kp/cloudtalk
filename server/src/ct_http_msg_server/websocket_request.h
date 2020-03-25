#ifndef __WEBSOCKET_REQUEST__
#define __WEBSOCKET_REQUEST__

#include <stdio.h>
#include <time.h>
#include <string.h>
#include <stdarg.h>
#include <stdint.h>
#include <arpa/inet.h>
#include "util.h"

class Websocket_Request {
public:
	Websocket_Request();
	~Websocket_Request();
	int fetch_websocket_info(char *msg);
	void print();
	void reset();

private:
	int fetch_fin(char *msg, int &pos);
	int fetch_opcode(char *msg, int &pos);
	int fetch_mask(char *msg, int &pos);
	int fetch_masking_key(char *msg, int &pos);
	int fetch_payload_length(char *msg, int &pos);
	int fetch_payload(char *msg, int &pos);
public:
	uint8_t fin_;
	uint8_t opcode_;
	uint8_t mask_;
	uint8_t masking_key_[4];
	uint64_t payload_length_;
	char payload_[20480];
	char *alldata;
};

#endif
