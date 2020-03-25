kill -9 `cat command.pid`
kill -9 `cat command.pid`

nohup java -Xverify:none -Xms64m -Xmx512m -XX:+HeapDumpOnOutOfMemoryError -Dtio.default.read.buffer.size=2048 -XX:HeapDumpPath=./cloudtalk-business-pid.hprof -jar -Dloader.path=.,3rd-lib,config ./cloudtalk-business-1.0-SNAPSHOT-classes.jar > ./im.log 2>&1 & echo $! > command.pid