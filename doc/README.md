## 1、依赖

   CloudTalk需要 CentOs7.0 以上版本。推荐使用纯净的新系统进行安装。
   在安装前，推荐安装使用Bt.cn的宝塔服务器管理平台，管理安装所需要的数据库,网站等。安全,方便，专业.安装命令如下:

   宝塔运维平台介绍url: https://www.bt.cn/?invite_code=MV9weHdhYmg=

    yum install -y wget && wget -O install.sh http://download.bt.cn/install/install_6.0.sh && bash install.sh

    安装完宝塔面板后，请在后台管理里面，安装数据库 Mysql 5.6.x，Redis，Java 1.8以上版本等环境。

	服务端对pb,hiredis,mysql_client,log4cxx有依赖，所以服务端需要先安装pb，hiredis,mysql,log4cxx。


    ##进入 server/src/ 目录下面，执行install.sh,进行编译操作,在编译前，请确保安装了msysql,redis,java 1.8等环境##


	
## 2、部署说明
 
     成功编译完服务端后，会生成 cloudtalk-server-x.x.tar.gz 的压缩包，这个是服务端运行程序，解压它。然后进入到这个目录
     1.运行 sync_lib_for_zip.sh 脚本文件，他的作用是将lib分发到每个服务端目录里面。该脚本只需运行一次
     2.restart.sh 是运行脚本，是运行服务端的。总共有             
       ct_msg_server|ct_route_server|ct_http_msg_server|ct_file_server|ct_push_server|ct_msfs  六个服务端。
     3.在运行上面的服务前，请先配置每个服务端config文件。
       例如:ct_db_proxy_server/ct_dbproxyserver.conf  这个主要是操作数据库和缓存的，需要配置的是：监听端口，数据库ip，端口，账号，密码，redis的ip地址，端口等。每一个服务端都有一个监听的端口，然后还可能去连接其它服务端的地址。
     4.导入数据库文件，在 database 目录下面，新建一个数据库，将sql文件导入进来。这个数据库在 db_proxy_server中需要用到它，还有business下面的java 业务api也需要用到它。
     5.配置好上面六个服务端的conf文件后，就可以启动一下它们试试。先启动 db_proxy_server 。运行 ./restart.sh ct_db_proxy_server 。即可启动数据库操作的服务端，运行完后，到 ct_db_proxy_server 目录下面的Log目录下面看日志，看有没有启动成功。启动成功后，再接下来依次启动 ct_route_server，ct_file_server，ct_msfs，ct_push_server，ct_msg_server.如果都顺利启动，恭喜你，已经成功一半了。
     6.启动完服务端后，接下来编译启动java 的业务api,这个api主要是为app提供用户登录，好友列表等一系列业务api.在 business 目录下面，用入maven项目。运行 maven install后即可生成运行程序。在运行java api是，请先配置 application.properties 等相关配置文件，application.properties里面需要修改数据库的ip地址，数据库名，mysql账号密码等，特别注意的是 cloudtalk.files.msfsprior 的值是 msfs的外网ip+端口。这个是提供给app上传图片的接口。cloudtalk.api.url http_msg_server服务端的ip地址和端口，是用来发送系统消息时用的。然后再修改 application-local.properties,application-prod.properties,application-qa.properties 这些文件里面的配置，主要是修改相关的端口。如 HttpMsgServerPort是http_msg_server的websocket服务监听端口。
     7.配置完后，可启动 startup.sh 脚本即可运行java api服务。

     8.修改app的相关api地址，重新编译即可。app运行流程是：请求java api里面的登录接口，验证账号密码后获得token,请求api获取负载最小的msg_server的ip和端口，app连接msg_server发送token，验证成功后即可以了，然后再请求api获取用户列表，群列表等数据。




   官方技术交流QQ群:6445609

   cloudtalk团队可为企业提供定制化服务,提供高可用的商用版本高度定制化，各类IM功能业务定制,企业内部沟通/办公系统定制，请进群联系官方或加官方业务QQ:689541
   cloudtalk开源团队承诺永久 100%开源,免费使用cloudtalk，并可应用于商业产品中。