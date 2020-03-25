#include "websocket_respond.h"
#include <stdio.h>
#include <arpa/inet.h>
#include <inttypes.h>
Websocket_Respond::Websocket_Respond() { }

Websocket_Respond::~Websocket_Respond() { }

int Websocket_Respond::fetch_http_info(char* buff_){
    std::istringstream s(buff_);
    std::string request;

    std::getline(s, request);
    if (request[request.size()-1] == '\r') {
        request.erase(request.end()-1);
    } else {
        return -1;
    }

    std::string header;
    std::string::size_type end;

    while (std::getline(s, header) && header != "\r") {
        if (header[header.size()-1] != '\r') {
            continue; //end
        } else {
            header.erase(header.end()-1);	//remove last char
        }

        end = header.find(": ",0);
        if (end != std::string::npos) {
            std::string key = header.substr(0,end);
            std::string value = header.substr(end+2);
            header_map_[key] = value;
        }
    }
    return 0;
}

uint64_t Websocket_Respond::htonll(uint64_t val)
{
    return (((uint64_t) htonl(val)) << 32) + htonl(val >> 32);
}

uint64_t Websocket_Respond::ntohll(uint64_t val) {
    return (((uint64_t) ntohl(val)) << 32) + ntohl(val >> 32);
}

void Websocket_Respond::parse_str(char *request){
    strcat(request, "HTTP/1.1 101 Switching Protocols\r\n");
    strcat(request, "Connection: upgrade\r\n");
    strcat(request, "Sec-WebSocket-Accept: ");
    std::string server_key = header_map_["Sec-WebSocket-Key"];
    server_key += MAGIC_KEY;

    SHA1 sha;
    unsigned int message_digest[5];
    sha.Reset();
    sha << server_key.c_str();

    sha.Result(message_digest);
    for (int i = 0; i < 5; i++) {
        message_digest[i] = htonl(message_digest[i]);
    }
    server_key = base64_encode2(reinterpret_cast<const unsigned char*>(message_digest),20);
    server_key += "\r\n";
    strcat(request, server_key.c_str());
    strcat(request, "Upgrade: websocket\r\n\r\n");
}


char* Websocket_Respond::toFrameDataPkt(const std::string &data)
{
   std::string outFrame;
   uint32_t messageLength = data.size();
   if (messageLength > 20000)
   {
       // 暂不支持这么长的数据
       return false;
   }

   uint8_t payloadFieldExtraBytes = (messageLength <= 0x7d) ? 0 : 2;
   // header: 2字节, mask位设置为0(不加密), 则后面的masking key无须填写, 省略4字节
   uint8_t frameHeaderSize = 2 + payloadFieldExtraBytes;
   uint8_t *frameHeader = new uint8_t[frameHeaderSize];
   memset(frameHeader, 0, frameHeaderSize);

   // fin位为1, 扩展位为0, 操作位为frameType
   frameHeader[0] = static_cast<uint8_t>(0x80 | 0x1);

   // 填充数据长度
   if (messageLength <= 0x7d)
   {
       frameHeader[1] = static_cast<uint8_t>(messageLength);
   }
   else
   {
       frameHeader[1] = 0x7e;
       uint16_t len = htons(messageLength);
       memcpy(&frameHeader[2], &len, payloadFieldExtraBytes);
   }

   // 填充数据
   uint32_t frameSize = frameHeaderSize + messageLength;
   char *frame = new char[frameSize+1];
   memcpy(frame, frameHeader, frameHeaderSize);
   memcpy(frame + frameHeaderSize, data.c_str(), messageLength);
   frame[frameSize]='\0';
//   outFrame = std::string(frame, frameSize);
//   delete[] frame;
//   delete[] frameHeader;
   return frame;


//     unsigned size = data.size();
//     std::string frame;
//
//     uint8_t c = (1 << 7);
//     c = c | 0x1;
//     frame.push_back((char)c);
//
//     uint8_t paylength = 0;
//     if (size < 126U) {
//         paylength = size;
//         frame.push_back((char)paylength);
//     }
//     else if (size >= 126U && size <= 0xFFFFU) {
//         paylength = 126;
//         frame.push_back(paylength);
//
//         uint16_t l = htons(size);
//         char buf[2] = {0};
//         memcpy(buf, &l, 2);
//         frame.append(buf);
//     }
//     else {
//         paylength = 127;
//         frame.push_back(paylength);
//
//         uint64_t l = htonll((int64_t)size);
//         char buf[8] = {0};
//         memcpy(buf, &l, 8);
//         frame.append(buf);
//     }
//
//     frame.append(data);
//     char end[1] = {'\0'};
//     frame.append(end);
//     return frame;
}
