var DemoHandler = function () {
  var isLogin=false;
  this.onopen = function (event, ws) {
    document.getElementById('contentId').innerHTML += '连接服务器成功!<br>';
  }
  
  this.getloginstat=function(){
	  return this.isLogin;
  }

  /**
   * 收到服务器发来的消息
   * @param {*} event 
   * @param {*} ws 
   */
  this.onmessage = function (event, ws) {
    var data = event.data;
    document.getElementById('contentId').innerHTML += data + '<br>';
    var jsondata=eval("("+data+")");
    switch(jsondata.serviceID)
    {
	    case 1:
	    {
		    if(jsondata.commandID==260)
		    {
			    if(jsondata.code!=1)
			    {
				        this.isLogin=false;
				        document.getElementById('contentId').innerHTML += '验证登录失败!<br>';
	                    isrun=false;
	                    //ws.close();
			    }
			    else
			    {
				    this.isLogin=true;
				    document.getElementById('logindiv').display='none';
				    document.getElementById('contentId').innerHTML += '用户身份验证成功!<br>';
			    }
		    }
	    }
	    break;
	    case 3:
	    {
		    document.getElementById('contentId').innerHTML += jsondata.fromId+" 说:"+jsondata.content+"  时间:"+jsondata.created + '<br>';
	    }
	    break;
	    default:
	        document.getElementById('contentId').innerHTML += data + '<br>';
	        break;
    }
    

  }
  
  this.onclose = function (e, ws) {
    // error(e, ws)
  }

  this.onerror = function (e, ws) {
    //error(e, ws)
  }
  
  this.isUserLogin = function () {
	  return this.isLogin;
  }
  /**
   * 发送心跳，本框架会自动定时调用该方法，请在该方法中发送心跳
   * @param {*} ws 
   */
  this.ping = function (ws) {
   // log("发心跳了")
    ws.send('心跳内容')
  }
}
