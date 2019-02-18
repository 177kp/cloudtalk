package com.zhangwuji.im.api.entity;

import lombok.Data;

import java.util.Date;


@Data
public class ServerInfoEntity {
	
	public String server_ip="";
	public String server_ip2="";
	public int server_port=8600;
	public String msfsPrior="";
	public String msfsBackup="";

	
}
