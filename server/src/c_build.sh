#!/bin/bash

build() {
	echo "#ifndef __VERSION_H__" > base/version.h
	echo "#define __VERSION_H__" >> base/version.h
	echo "#define VERSION \"$1\"" >> base/version.h
	echo "#endif" >> base/version.h

    if [ ! -d lib ]
    then
        mkdir lib
    fi

    cd libsecurity/unix
    ./build.sh
    cd ../../

	cd base
    cmake .
	make
    if [ $? -eq 0 ]; then
        echo "make base successed";
    else
        echo "make base failed";
        exit;
    fi
    if [ -f libbase.a ]
    then
        cp libbase.a ../lib/
    fi
    cd ../slog
    cmake .
    make
    if [ $? -eq 0 ]; then
        echo "make slog successed";
    else
        echo "make slog failed";
        exit;
    fi
    mkdir ../base/slog/lib
    cp slog_api.h ../base/slog/
    cp libslog.so ../base/slog/lib/
    cp -a lib/liblog4cxx* ../base/slog/lib/


	cd ../ct_route_server
    cmake .
	make
    if [ $? -eq 0 ]; then
        echo "make ct_route_server successed";
    else
        echo "make ct_route_server failed";
        exit;
    fi

	cd ../ct_msg_server
    cmake .
	make
    if [ $? -eq 0 ]; then
        echo "make ct_msg_server successed";
    else
        echo "make ct_msg_server failed";
        exit;
    fi

    cd ../ct_http_msg_server
    cmake .
    make
    if [ $? -eq 0 ]; then
        echo "make ct_http_msg_server successed";
    else
        echo "make ct_http_msg_server failed";
        exit;
    fi

    cd ../ct_file_server
    cmake .
    make
    if [ $? -eq 0 ]; then
        echo "make ct_file_server successed";
    else
        echo "make ct_file_server failed";
        exit;
    fi

    cd ../ct_push_server
    cmake .
    make
    if [ $? -eq 0 ]; then
        echo "make ct_push_server successed";
    else
        echo "make ct_push_server failed";
        exit;
    fi

    cd ../tools
    make
    if [ $? -eq 0 ]; then
        echo "make tools successed";
    else
        echo "make tools failed";
        exit;
    fi

    cd ../ct_db_proxy_server
    cmake .
    make
    if [ $? -eq 0 ]; then
        echo "make ct_db_proxy_server successed";
    else
        echo "make ct_db_proxy_server failed";
        exit;
    fi

    cd ../ct_msfs
    cmake .
    make
    if [ $? -eq 0 ]; then
        echo "make ct_msfs successed";
    else
        echo "make ct_msfs failed";
        exit;
    fi

	cd ../

    mkdir -p ../run/ct_route_server
    mkdir -p ../run/ct_msg_server
    mkdir -p ../run/ct_file_server
    mkdir -p ../run/ct_msfs
    mkdir -p ../run/ct_push_server
    mkdir -p ../run/ct_http_msg_server
    mkdir -p ../run/ct_db_proxy_server

	#copy executables to run/ dir

	cp ct_route_server/ct_route_server ../run/ct_route_server/

	cp ct_msg_server/ct_msg_server ../run/ct_msg_server/

    cp ct_http_msg_server/ct_http_msg_server ../run/ct_http_msg_server/

    cp ct_file_server/ct_file_server ../run/ct_file_server/

    cp ct_push_server/ct_push_server ../run/ct_push_server/

    cp ct_db_proxy_server/ct_db_proxy_server ../run/ct_db_proxy_server/

    cp ct_msfs/ct_msfs ../run/ct_msfs/

    cp tools/daeml ../run/

    build_version=cloudtalk-server-$1
    build_name=$build_version.tar.gz
	if [ -e "$build_name" ]; then
		rm $build_name
	fi
    mkdir -p ../$build_version
    mkdir -p ../$build_version/ct_route_server
    mkdir -p ../$build_version/ct_msg_server
    mkdir -p ../$build_version/ct_file_server
    mkdir -p ../$build_version/ct_msfs
    mkdir -p ../$build_version/ct_http_msg_server
    mkdir -p ../$build_version/ct_push_server
    mkdir -p ../$build_version/ct_db_proxy_server
    mkdir -p ../$build_version/lib


    cp ct_route_server/ct_route_server ../$build_version/ct_route_server/
    cp ct_route_server/ct_routeserver.conf ../$build_version/ct_route_server/

    cp ct_msg_server/ct_msg_server ../$build_version/ct_msg_server/
    cp ct_msg_server/ct_msgserver.conf ../$build_version/ct_msg_server/

    cp ct_http_msg_server/ct_http_msg_server ../$build_version/ct_http_msg_server/
    cp ct_http_msg_server/ct_httpmsgserver.conf ../$build_version/ct_http_msg_server/

    cp ct_file_server/ct_file_server ../$build_version/ct_file_server/
    cp ct_file_server/ct_fileserver.conf ../$build_version/ct_file_server/

    cp ct_push_server/ct_push_server ../$build_version/ct_push_server/
    cp ct_push_server/ct_pushserver.conf ../$build_version/ct_push_server/

    cp ct_db_proxy_server/ct_db_proxy_server ../$build_version/ct_db_proxy_server/
    cp ct_db_proxy_server/ct_dbproxyserver.conf ../$build_version/ct_db_proxy_server/

    cp ct_msfs/ct_msfs ../$build_version/ct_msfs/
    cp ct_msfs/ct_msfs.conf ../$build_version/ct_msfs/

    cp slog/log4cxx.properties ../$build_version/lib/
    cp slog/libslog.so  ../$build_version/lib/
    cp -a slog/lib/liblog4cxx.so* ../$build_version/lib/
    cp sync_lib_for_zip.sh ../$build_version/

    cp tools/daeml ../$build_version/
    cp ./restart.sh ../$build_version/
    cp ./restart2.sh ../$build_version/

    cd ../
    tar zcvf    $build_name $build_version

    rm -rf $build_version
}

clean() {
	cd base
	make clean
	cd ../ct_route_server
	make clean
	cd ../ct_msg_server
	make clean
	cd ../ct_http_msg_server
    make clean
	cd ../ct_file_server
    make clean
    cd ../ct_push_server
    make clean
	cd ../ct_db_proxy_server
	make clean
    cd ../ct_push_server
    make clean
}

print_help() {
    echo "cloudtalk 3.0"
	echo "Usage: "
	echo "  $0 clean --- clean all build"
	echo "  $0 version version_str --- build a version"
}

case $1 in
	clean)
		echo "clean all build..."
		clean
		;;
	version)
		if [ $# != 2 ]; then 
			echo $#
			print_help
			exit
		fi

		echo $2
	    echo "start compiling cloudtalk 3.0..."
        echo "=================================="
		build $2
		;;
	*)
		print_help
		;;
esac
