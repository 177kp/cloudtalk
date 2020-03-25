#!/bin/bash

cp -a ./lib/log4cxx.properties ./ct_route_server/
cp -a ./lib/libslog.so  ./ct_route_server/
cp -a ./lib/liblog4cxx.so* ./ct_route_server/

cp -a ./lib/log4cxx.properties ./ct_msg_server/
cp -a ./lib/libslog.so  ./ct_msg_server/
cp -a ./lib/liblog4cxx.so* ./ct_msg_server/

cp -a ./lib/log4cxx.properties ./ct_http_msg_server/
cp -a ./lib/libslog.so  ./ct_http_msg_server/
cp -a ./lib/liblog4cxx.so* ./ct_http_msg_server/

cp -a ./lib/log4cxx.properties ./ct_file_server/
cp -a ./lib/libslog.so  ./ct_file_server/
cp -a ./lib/liblog4cxx.so* ./ct_file_server/

cp -a ./lib/log4cxx.properties ./ct_push_server/
cp -a ./lib/libslog.so  ./ct_push_server/
cp -a ./lib/liblog4cxx.so* ./ct_push_server/

cp -a ./lib/log4cxx.properties ./ct_db_proxy_server/
cp -a ./lib/libslog.so  ./ct_db_proxy_server/
cp -a ./lib/liblog4cxx.so* ./ct_db_proxy_server/

cp -a ./lib/log4cxx.properties ./ct_msfs/
cp -a ./lib/libslog.so  ./ct_msfs/
cp -a ./lib/liblog4cxx.so* ./ct_msfs/