#-Xverify:none -Djava.nio.channels.spi.SelectorProvider=sun.nio.ch.EPollSelectorProvider
#-Xrunjdwp:transport=dt_socket,address=8888,suspend=n,server=y

nohup java -Xverify:none -Xms64m -Xmx512m -XX:+HeapDumpOnOutOfMemoryError -Dtio.default.read.buffer.size=2048 -XX:HeapDumpPath=./zhangwuji_imwebsocket-pid.hprof -jar -Dloader.path=.,3rd-lib,config ./cloudtalk-websocket-1.0-SNAPSHOT-classes.jar > ./im.log &