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
        ../daeml ./$1
    else 
        ../daeml ./$1
    fi
}

case $1 in
	ct_msg_server)
		restart $1
		;;
	ct_route_server)
		restart $1
		;;
	ct_http_msg_server)
		restart $1
		;;
	ct_file_server)
		restart $1
		;;
  ct_push_server)
    restart $1
    ;;
  ct_db_proxy_server)
  restart $1
  ;;
     ct_msfs)
    restart $1
    ;;
	*)
		echo "Usage: "
		echo "  ./restart.sh (ct_msg_server|ct_route_server|ct_http_msg_server|ct_file_server|ct_push_server|ct_msfs)"
		;;
esac
