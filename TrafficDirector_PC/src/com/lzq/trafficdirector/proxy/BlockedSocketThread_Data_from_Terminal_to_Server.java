package com.lzq.trafficdirector.proxy;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.net.Socket;

import com.lzq.trafficdirector.global.TransmissionUnit;
import com.lzq.trafficdirector.utils.GZipTools;

/**
 * 从内部读取，向外部发送信息
 * 
 * @author zxq
 * 
 */
public class BlockedSocketThread_Data_from_Terminal_to_Server extends Thread {
	//本线程中，负责读取从本地代理端发送过来的打包压缩后的数据，解压缩，并转发给服务器
	private InputStream isIn;
	private OutputStream osOut;

	private ObjectInputStream oisIn;

	private Socket socketIn;
	private Socket socketOut;
	
	public BlockedSocketThread_Data_from_Terminal_to_Server(ObjectInputStream oisIn, OutputStream osOut, Socket in, Socket out) {
		this.osOut = osOut;
		this.oisIn = oisIn;
		
		socketIn = in;
		socketOut = out;
	}

	private byte[] tempData;
	long Time;
	public void run() {
		try {
			TransmissionUnit tempUnit;
			while (true) {
				Time=System.currentTimeMillis();
				tempUnit = (TransmissionUnit) oisIn.readObject();
				if (tempUnit.isCompressed) {
					tempData = GZipTools.decompress(tempUnit.datas);
					if(BlockedSocketProxy.getInputLogger()!=null)
					BlockedSocketProxy.getInputLogger().addData(tempData.length, tempUnit.datas.length);
					osOut.write(tempData, 0, tempData.length);
					osOut.flush();
				} 
				else {
					byte[] tempData = tempUnit.datas;
					if(BlockedSocketProxy.getInputLogger()!=null)
					BlockedSocketProxy.getInputLogger().addData(tempData.length, tempData.length);
					osOut.write(tempData, 0, tempData.length);
					osOut.flush();
				}
			}
		} catch (Exception e) {
			//e.printStackTrace();
			System.out.println("SocketThreadOutput leave");
		}
		finally {
			Time=System.currentTimeMillis()-Time;
			//System.out.println("Remote Proxy: Send data decompressed To server,using "+Time*0.001+" s.");
			// 数据读取完毕之后关闭两个方向上的输入输出流
			try {
				socketIn.shutdownInput();
				socketOut.shutdownOutput();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
	}
}
