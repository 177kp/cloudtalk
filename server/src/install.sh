#!/bin/bash
# author: cloudtalk
# date: 03/24/2020

LOG4CXX=apache-log4cxx-0.10.0
LOG4CXX_PATH=http://cloud.b56.cn/res/apache/logging/log4cxx/0.10.0/$LOG4CXX.tar.gz
PROTOBUF=protobuf-3.6.1
HIREDIS=hiredis-master
CUR_DIR=

check_user() {
    if [ $(id -u) != "0" ]; then
        echo "Error: You must be root to run this script, please use root to install cloudtalk 3.0"
        exit 1
    fi
}
get_cur_dir() {
    # Get the fully qualified path to the script
    case $0 in
        /*)
            SCRIPT="$0"
            ;;
        *)
            PWD_DIR=$(pwd);
            SCRIPT="${PWD_DIR}/$0"
            ;;
    esac
    # Resolve the true real path without any sym links.
    CHANGED=true
    while [ "X$CHANGED" != "X" ]
    do
        # Change spaces to ":" so the tokens can be parsed.
        SAFESCRIPT=`echo $SCRIPT | sed -e 's; ;:;g'`
        # Get the real path to this script, resolving any symbolic links
        TOKENS=`echo $SAFESCRIPT | sed -e 's;/; ;g'`
        REALPATH=
        for C in $TOKENS; do
            # Change any ":" in the token back to a space.
            C=`echo $C | sed -e 's;:; ;g'`
            REALPATH="$REALPATH/$C"
            # If REALPATH is a sym link, resolve it.  Loop for nested links.
            while [ -h "$REALPATH" ] ; do
                LS="`ls -ld "$REALPATH"`"
                LINK="`expr "$LS" : '.*-> \(.*\)$'`"
                if expr "$LINK" : '/.*' > /dev/null; then
                    # LINK is absolute.
                    REALPATH="$LINK"
                else
                    # LINK is relative.
                    REALPATH="`dirname "$REALPATH"`""/$LINK"
                fi
            done
        done

        if [ "$REALPATH" = "$SCRIPT" ]
        then
            CHANGED=""
        else
            SCRIPT="$REALPATH"
        fi
    done
    # Change the current directory to the location of the script
    CUR_DIR=$(dirname "${REALPATH}")
}

init_dev_lib(){
echo "start installing the development library.."
echo "=========================================="
sleep 3s
yum -y install apr-devel
yum -y install apr-util-devel
yum -y install mariadb-devel
yum -y install cmake
yum -y install libuuid-devel
yum -y install openssl-devel
yum -y install curl-devel
yum -y install maven
yum -y install java
echo "development library installation complete."
echo "==========================================="
sleep 2s
}

check_sys_var(){
    echo "============================"
    echo "Welcome to cloudtalk 3.0"
    echo "============================"
    sleep 3s
    CENTOS_VERSION=$(less /etc/redhat-release)
    echo "start checking the run environment...."
    sleep 1s
    if [[ $CENTOS_VERSION =~ "7." ]]; then
        echo "environmental inspection passed.";
        sleep 1s
        echo "==================================";
        echo "start installing cloudtalk 3.0...";
        echo "==================================";
        sleep 3s
        init_dev_lib;
    else
        echo "centos version must >=7.0 !!!";
        exit 1;
    fi 
}

build_log4cxx(){
    echo "=================================================="
    echo "start installing the development library [log4cxx]"
    echo "=================================================="
    sleep 3s
    cd log4cxx
 #  download $LOG4CXX.tar.gz $LOG4CXX_PATH
    tar -xf $LOG4CXX.tar.gz
    cd $LOG4CXX
    ./configure --prefix=$CUR_DIR/log4cxx --with-apr=/usr --with-apr-util=/usr --with-charset=utf-8 --with-logchar=utf-8
    cp ../inputstreamreader.cpp ./src/main/cpp/
    cp ../socketoutputstream.cpp ./src/main/cpp/
    cp ../console.cpp ./src/examples/cpp/
    make
    make install
    cd ../../
    cp -rf log4cxx/include slog/
    mkdir -p slog/lib/
    cp -f log4cxx/lib/liblog4cxx.so* slog/lib/
    echo "==================================================="
    echo "development library installation complete.[log4cxx]"
    echo "==================================================="
    sleep 2s
}

build_protobuf(){
    echo "=================================================="
    echo "start installing the development library [protobuf]"
    echo "=================================================="
    sleep 3s
    cd protobuf
    tar -xf $PROTOBUF.tar.gz
    cd $PROTOBUF
    ./configure --prefix=$CUR_DIR/protobuf
    make
    make install
    cd ..
    mkdir -p ../base/pb/lib/linux/
    cp lib/libprotobuf-lite.a ../base/pb/lib/linux/
    cp  -r include/* ../base/pb/
    cd ..
    echo "==================================================="
    echo "development library installation complete.[protobuf]"
    echo "==================================================="
    sleep 3s
}
build_hiredis(){
    echo "=================================================="
    echo "start installing the development library [hiredis]"
    echo "=================================================="
    sleep 3s
    cd hiredis
    unzip $HIREDIS.zip
    cd $HIREDIS
    make
    cp -a libhiredis.a ../../ct_db_proxy_server/
    cp -a hiredis.h async.h read.h sds.h adapters ../../ct_db_proxy_server
    cd ../../
    echo "==================================================="
    echo "development library installation complete.[hiredis]"
    echo "==================================================="
    sleep 3s
}

check_user
get_cur_dir
check_sys_var
build_log4cxx
build_protobuf
build_hiredis

#ln -s libmysqlclient.so libmysqlclient_r.so
./c_build.sh version 3.0.1