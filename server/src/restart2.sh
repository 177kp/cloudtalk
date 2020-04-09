#/bin/sh
#start or stop the im-server


function restart() {
    cd $1
    if [ ! -e *.conf  ]
    then
        echo "no config file"
        return
    fi

    if [ -e server.pid  ]; then
        pid=`cat server.pid`
        echo "kill pid=$pid"
        kill -9 $pid
        ../daeml ./$2
    else 
        ../daeml ./$2
    fi
}

restart $1 $2
