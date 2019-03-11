## 1、依赖

   CloudTalk需要 CentOs7.0 以上版本。推荐使用纯净的新系统进行安装。
   在安装前，推荐安装使用Bt.cn的宝塔服务器管理平台，管理安装所需要的数据库,网站等。安全,方便，专业.安装命令如下:

   yum install -y wget && wget -O install.sh http://download.bt.cn/install/install_6.0.sh && bash install.sh

    安装完宝塔面板后，请在后台管理里面，安装数据库 Mysql 5.6.x，Redis，Java 1.8以上版本等环境。

	服务端对pb,hiredis,mysql_client,log4cxx有依赖，所以服务端需要先安装pb，hiredis,mysql,log4cxx。
	在正式编译服务端之前，请先执行server/src目录下的：
	make_log4cxx.sh
	make_protobuf.sh
	make_hiredis.sh
	这些脚本会先安装以上依赖。

	如果安装了宝塔面板，并且安装了mysql和redis库后，就不用运行下面的两个脚本，如果没有安装宝塔面板，你需要自行安装所需环境，并且运行下面脚本:
    make_mariadb.sh
	
## 2、编译协议文件
	
	所有的协议文件在pb目录下，其中有create.sh以及sync.sh两个shell脚本。
	create.sh的作用是使用protoc将协议文件转化成相应语言的源码。
	sync.sh是将生成的源码拷贝到server的目录下。
	
## 3、编译服务端
	
	经历了编译服务端依赖，pb之后，就可以执行server目录下的build.sh脚本
	
## 4、部署说明
 
     成功编译完服务端后，会生成 im-server-x.x.tar.gz 的压缩包，这个是服务端运行程序，解压它。然后进入到这个目录
     1.运行 sync_lib_for_zip.sh 脚本文件，他的作用是将lib分发到每个服务端目录里面。该脚本只需运行一次
     2.restart.sh 是运行脚本，是运行服务端的。总共有             
       msg_server|route_server|http_msg_server|file_server|push_server|msfs  六个服务端。
     3.在运行上面的服务前，请先配置每年服务端config文件。
       例如:db_proxy_server/dbproxyserver.conf  这个主要是操作数据库和缓存的，需要配置的是：监听端口，数据库ip，端口，账号，密码，redis的ip地址，端口等。每一个服务端都有一个监听的端口，然后还可能去连接其它服务端的地址。
     4.导入数据库文件，在 database 目录下面，新建一个数据库，将sql文件导入进来。这个数据库 db_proxy_server 需要用到它，还有business下面的java 业务api也需要用到它。
     5.配置好上面六个服务端的conf文件后，就可以启动一下它们试试。先启动 db_proxy_server 。运行 ./restart.sh db_proxy_server 。即可启动数据库操作的服务端，运行完后，到 db_proxy_server 目录下面的Log目录下面看日志，看有没有启动成功。启动成功后，再接下来依次启动 route_server，file_server，msfs，push_server，msg_server.如果都顺利启动，恭喜你，已经成功一半了。
     6.启动完服务端后，接下来编译启动java 的业务api,这个api主要是为app提供用户登录，好友列表等一系列业务api.在 business 目录下面，用入maven项目。运行 maven install后即可生成运行程序。在运行java api是，请先配置 application.properties 等相关配置文件，application.properties里面需要修改数据库的ip地址，数据库名，mysql账号密码等，特别注意的是 cloudtalk.files.msfsprior 的值是 msfs的外网ip+端口。这个是提供给app上传图片的接口。cloudtalk.api.url http_msg_server服务端的ip地址和端口，是用来发送系统消息时用的。然后再修改 application-local.properties,application-prod.properties,application-qa.properties 这些文件里面的配置，主要是修改相关的端口。如 HttpMsgServerPort是http_msg_server的websocket服务监听端口。
     7.配置完后，可启动 startup.sh 脚本即可运行java api服务。

     8.修改app的相关api地址，重新编译即可。app运行流程是：请求java api里面的登录接口，验证账号密码后获得token,请求api获取负载最小的msg_server的ip和端口，app连接msg_server发送token，验证成功后即可以了，然后再请求api获取用户列表，群列表等数据。




   官方技术交流QQ群:6445609