package com.zhangwuji.im.support;

public interface Protocol{
	/**
	 * 心跳字节
	 */
	public static final byte HEARTBEAT_BYTE = -128;
	
	/**
	 * 握手字节
	 */
	public static final byte HANDSHAKE_BYTE = -127;

	/**
	 * 协议版本号
	 */
	public final static byte VERSION = 0x01;
	
	public final static String WEBSOCKET = "ws";
	
	public final static String HTTP = "http";
	
	public final static String TCP = "tcp";
	
	public static final String COOKIE_NAME_FOR_SESSION = "jim-s";
	/**
	 * 消息体最多为多少
	 */
	public static final int MAX_LENGTH_OF_BODY = (int) (1024 * 1024 * 2.1); //只支持多少M数据

	/**
	 * 消息头最少为多少个字节
	 */
	public static final int LEAST_HEADER_LENGHT = 4;//1+1+2 + (2+4)
	
	/**
	 * 加密标识位mask，1为加密，否则不加密
	 */
	public static final byte FIRST_BYTE_MASK_ENCRYPT = -128;

	/**
	 * 压缩标识位mask，1为压缩，否则不压缩
	 */
	public static final byte FIRST_BYTE_MASK_COMPRESS = 0B01000000;

	/**
	 * 是否有同步序列号标识位mask，如果有同步序列号，则消息头会带有同步序列号，否则不带
	 */
	public static final byte FIRST_BYTE_MASK_HAS_SYNSEQ = 0B00100000;

	/**
	 * 是否是用4字节来表示消息体的长度
	 */
	public static final byte FIRST_BYTE_MASK_4_BYTE_LENGTH = 0B00010000;

	/**
	 * 版本号mask
	 */
	public static final byte FIRST_BYTE_MASK_VERSION = 0B00001111;
}