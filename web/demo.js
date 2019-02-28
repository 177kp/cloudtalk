var ws_protocol = 'ws'; // ws 或 wss
var ip = 'imapi.b56.cn'
var port = 9326
var apiUrl="http://imapi.b56.cn:8080";

var heartbeatTimeout = 5000; // 心跳超时时间，单位：毫秒
var reconnInterval = 5000; // 重连间隔时间，单位：毫秒

var binaryType = 'blob'; // 'blob' or 'arraybuffer';//arraybuffer是字节
var handler = new DemoHandler()

var tiows
var isrun=true;
var logindata='';
var myUserId=0;
var myUserToken='';

function initWs () {
  isrun=true;
  var queryString = '';
  var param = null
  tiows = new tio.ws(ws_protocol, ip, port, queryString, param, handler, heartbeatTimeout, reconnInterval, binaryType)
  tiows.connect()
}

function sendlogin() {
  var username = document.getElementById('username').value;
  var password = document.getElementById('password').value;
  passwordMd5=hex_md5(password);
  
  $.post(apiUrl+"/api/checkLogin", { appId: "88888", username:username,password:passwordMd5},
   function(data){
     if(data.code==200)
     {
        myUserId=data.data.userinfo.id;
        myUserToken=data.data.userinfo.apiToken;
     
        logindata='{"serviceID":1,"commandID":259,"username":"'+myUserId+'","token":"'+myUserToken+'"}';
        tiows.saveLogindata(logindata);//保存登录信息
        tiows.loginServer();
     }
     else
     {
	     alert('账号密码错误!');
     }
       
   });
  
}

function send () {
	if(tiows!=null)
	{
		  var msg = document.getElementById('textId');
		  var userid= document.getElementById('userId').value;
          tiows.send('{"serviceID":3,"commandID":769,"content":"'+msg.value+'","fromId":'+myUserId+',"msgType":1,"sessionType":1,"toId":'+userid+'}')
          document.getElementById('textId').value='';
	}

}

function clearMsg () {
  document.getElementById('contentId').innerHTML = ''
}
initWs();