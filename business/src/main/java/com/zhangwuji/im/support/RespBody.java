/**
 * 
 */
package com.zhangwuji.im.support;

/**
 * 版本: [1.0]
 * 功能说明: 
 * 作者: WChao 创建时间: 2017年7月26日 上午11:31:48
 */
public class RespBody{
	
	private Integer code;//响应状态码;
	
	private String msg;//响应状态信息提示;
	
	private Command command;//响应cmd命令码;
	
	private Object data;//响应数据;

	public RespBody(){}
	public RespBody(Command command){
		this.command = command;
	}
	public RespBody(Command command,Object data){
		this(command);
		this.data = data;
	}
	public RespBody(Command command , ImStatus status){
		this(command);
		this.code = status.getCode();
		this.msg = status.getText();
	}
	public RespBody(ImStatus status){
		this.code = status.getCode();
		this.msg = status.getText();
	}
	public Integer getCode() {
		return code;
	}

	public RespBody setCode(Integer code) {
		this.code = code;
		return this;
	}

	public String getMsg() {
		return msg;
	}

	public RespBody setMsg(String msg) {
		this.msg = msg;
		return this;
	}

	public Command getCommand() {
		return command;
	}

	public RespBody setCommand(Command command) {
		this.command = command;
		return this;
	}
	public Object getData() {
		return data;
	}
	public RespBody setData(Object data) {
		this.data = data;
		return this;
	}
	@Override
	public String toString() {
		return JsonKit.toJSONEnumNoUsingName(this);
	}
	
	public byte[] toByte(){
		return JsonKit.toJSONBytesEnumNoUsingName(this);
	}
	
}
