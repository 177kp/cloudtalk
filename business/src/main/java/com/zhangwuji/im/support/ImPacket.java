package com.zhangwuji.im.support;

import org.tio.core.intf.Packet;

public class ImPacket extends Packet
{
	private static final long serialVersionUID = 2000118564569232098L;

	protected Status status;//包状态码;
	
	protected byte[] body;//消息体;
	
	private Command command;//消息命令;
	/**
	 * 消息是否是另外一台机器通过topic转过来的，如果是就不要死循环地再一次转发啦
	 * 这个属性是j-im内部使用，业务层的用户请务使用
	 */
	private boolean isFromCluster = false;
	
	public ImPacket(){}
	
	public ImPacket(byte[] body){
		this.body = body;
	}
	
	public ImPacket(Command command, byte[] body)
	{
		this(body);
		this.setCommand(command);
	}
	
	public ImPacket(Command command)
	{
		this(command,null);
	}

	public static byte encodeEncrypt(byte bs,boolean isEncrypt){
		if(isEncrypt){
			return (byte) (bs | Protocol.FIRST_BYTE_MASK_ENCRYPT);
		}else{
			return (byte)(Protocol.FIRST_BYTE_MASK_ENCRYPT & 0b01111111);
		}
	}
	
	public static boolean decodeCompress(byte version)
	{
		return (Protocol.FIRST_BYTE_MASK_COMPRESS & version) != 0;
	}

	public static byte encodeCompress(byte bs, boolean isCompress)
	{
		if (isCompress)
		{
			return (byte) (bs | Protocol.FIRST_BYTE_MASK_COMPRESS);
		} else
		{
			return (byte) (bs & (Protocol.FIRST_BYTE_MASK_COMPRESS ^ 0b01111111));
		}
	}

	public static boolean decodeHasSynSeq(byte maskByte)
	{
		return (Protocol.FIRST_BYTE_MASK_HAS_SYNSEQ & maskByte) != 0;
	}

	public static byte encodeHasSynSeq(byte bs, boolean hasSynSeq)
	{
		if (hasSynSeq)
		{
			return (byte) (bs | Protocol.FIRST_BYTE_MASK_HAS_SYNSEQ);
		} else
		{
			return (byte) (bs & (Protocol.FIRST_BYTE_MASK_HAS_SYNSEQ ^ 0b01111111));
		}
	}

	public static boolean decode4ByteLength(byte version)
	{
		return (Protocol.FIRST_BYTE_MASK_4_BYTE_LENGTH & version) != 0;
	}

	public static byte encode4ByteLength(byte bs, boolean is4ByteLength)
	{
		if (is4ByteLength)
		{
			return (byte) (bs | Protocol.FIRST_BYTE_MASK_4_BYTE_LENGTH);
		} else
		{
			return (byte) (bs & (Protocol.FIRST_BYTE_MASK_4_BYTE_LENGTH ^ 0b01111111));
		}
	}

	public static byte decodeVersion(byte version)
	{
		return (byte) (Protocol.FIRST_BYTE_MASK_VERSION & version);
	}

	/**
	 * 计算消息头占用了多少字节数
	 * @return
	 *
	 * @author: wchao
	 * 2017年1月31日 下午5:32:26
	 *
	 */
	public int calcHeaderLength(boolean is4byteLength)
	{
		int ret = Protocol.LEAST_HEADER_LENGHT;
		if (is4byteLength)
		{
			ret += 2;
		}
		if (this.getSynSeq() > 0)
		{
			ret += 4;
		}
		return ret;
	}
	public Command getCommand()
	{
		return command;
	}

	public void setCommand(Command type)
	{
		this.command = type;
	}

	/**
	 * @return the body
	 */
	public byte[] getBody()
	{
		return body;
	}

	/**
	 * @param body the body to set
	 */
	public void setBody(byte[] body)
	{
		this.body = body;
	}

	/** 
	 * @see org.tio.core.intf.Packet#logstr()
	 * 
	 * @return
	 * @author: wchao
	 * 2017年2月22日 下午3:15:18
	 * 
	 */
	@Override
	public String logstr()
	{
		return this.command == null ? Command.COMMAND_UNKNOW.name() : this.command.name();
	}

	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}

	public boolean isFromCluster() {
		return isFromCluster;
	}

	public void setFromCluster(boolean isFromCluster) {
		this.isFromCluster = isFromCluster;
	}
}
