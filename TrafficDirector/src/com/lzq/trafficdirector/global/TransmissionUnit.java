package com.lzq.trafficdirector.global;
import java.io.Serializable;

public class TransmissionUnit implements Serializable{
    public boolean isCompressed;
	public byte[] datas=null;
	public TransmissionUnit(byte[] data,boolean cpr)
	{
		this(data,0,data.length,cpr);
	}
	public TransmissionUnit(byte[] data,int begin,int end,boolean cpr)
	{
		datas=new byte[end-begin];
		System.arraycopy(data, 0, datas, 0, data.length);
		isCompressed=cpr;
	}
}
