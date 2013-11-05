package com.lzq.trafficdirector.proxy;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import com.lzq.trafficdirector.global.TransmissionUnit;
import com.lzq.trafficdirector.utils.GZipTools;

public class BlockedSocketThread extends Thread {
	private Socket socketIn;
	private InputStream isIn;
	private OutputStream osIn;
	//
	private Socket socketOut;
	private InputStream isOut;
	private OutputStream osOut;
	

	private ObjectOutputStream oosIn;
	private ObjectInputStream oisIn;
	private GZIPInputStream gisIn;
	private GZIPOutputStream gosIn;
	
	public BlockedSocketThread(Socket socket) {
		this.socketIn = socket;
	}

	private byte[] buffer;
	private static final byte[] VER = { 0x5, 0x0 };
	private static final byte[] CONNECT_OK = { 0x5, 0x0, 0x0, 0x1, 0, 0, 0, 0, 0, 0 };

	public void run() {
		try {
			//System.out.println("a client connect " + socketIn.getRemoteSocketAddress() + ":" + socketIn.getPort());
			isIn = socketIn.getInputStream();
			osIn = socketIn.getOutputStream();
//			gisIn=new GZIPInputStream(isIn);
//			gosIn=new GZIPOutputStream(osIn);
			oosIn=new ObjectOutputStream(osIn);
			oisIn=new ObjectInputStream(isIn);
			/*
			 * Sock5代理的几个通信步骤
			 */
			/**
			 * 1.客户端向代理服务器发送0x 05 01 00 02
			 */
			////System.out.println("Begin Read Object.");
			TransmissionUnit tu=(TransmissionUnit) oisIn.readObject();
			////System.out.println("客户端向代理服务器发送连接请求数据：0x 05 01 00 02 < " + bytesToHexString(tu.getData(), 0, tu.getData().length));
			////System.out.println("客户端向代理服务器发送连接请求数据：0x 05 01 00 02 < " + bytesToHexString(GZipTools.decompress(tu.getData()), 0, GZipTools.decompress(tu.getData()).length));
			/**
			 * 2.代理服务器向客户端回写0x5 0x0，表示允许连接
			 */
			oosIn.writeObject(new TransmissionUnit(VER,false));
			oosIn.flush();

			/**
			 * 3.客户端向代理服务器发送发送 05 01 00 01 
			 * + 目的地址(4字节） + 目的端口（2字节），目的地址和端口都是16进制码（不是字符串）
			 */
			tu = (TransmissionUnit) oisIn.readObject();
			//buffer=tu.getData();
			if(tu.isCompressed)
				buffer=GZipTools.decompress(tu.datas);
			else
				buffer=tu.datas;
			
			//System.out.println("客户端向代理服务器发送原始地址信息：05 01 00 01+目的地址(4字节） + 目的端口（2字节）  < " + bytesToHexString(buffer, 0, buffer.length));
			// 查找主机和端口
			String[] desInfo=generateDestination(buffer).split("@");
			final String host = desInfo[0];//findHost(buffer, 4, 7);
			final int port = Integer.parseInt(desInfo[1]);//findPort(buffer, 8, 9);
			//System.out.println("original destination: host=" + host + ",port=" + port);
			/**
			 * 代理服务器建立和目标服务器之间的连接
			 */
			socketOut = new Socket(host, port);
			isOut = socketOut.getInputStream();
			osOut = socketOut.getOutputStream();
			/**
			 * 4.接受服务器返回的自身地址和端口，连接完成
			 * 此时代理服务器返回CONNECT_OK给客户端，客户端实际上只需检查第二字节是否为零，以确定连接建立
			 */
			for (int i = 4; i <= 9; i++) {
				CONNECT_OK[i] = buffer[i];
			}
			oosIn.writeObject(new TransmissionUnit(CONNECT_OK,false));
			oosIn.flush();
			////System.out.println("> " + bytesToHexString(CONNECT_OK, 0, CONNECT_OK.length));
			BlockedSocketThread_Data_from_Terminal_to_Server out = new BlockedSocketThread_Data_from_Terminal_to_Server(oisIn, osOut,socketIn,socketOut);
			out.start();
			BlockedSocketThread_Data_from_Server_to_Terminal in = new BlockedSocketThread_Data_from_Server_to_Terminal(isOut, oosIn,socketIn,socketOut);
			in.start();
			out.join();
			in.join();
		} catch (Exception e) {
			//System.out.println("a client leave  ");
			e.printStackTrace();
		} finally {
			try {
				if (socketIn != null) {
					socketIn.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		//System.out.println("socket close");
	}

	public static String findHost(byte[] bArray, int begin, int end) {
		StringBuffer sb = new StringBuffer();
		for (int i = begin; i <= end; i++) {
			sb.append(Integer.toString(0xFF & bArray[i]));
			sb.append(".");
		}
		sb.deleteCharAt(sb.length() - 1);
		return sb.toString();
	}

	public static int findPort(byte[] bArray, int begin, int end) {
		int port = 0;
		for (int i = begin; i <= end; i++) {
			port <<= 8;
			port += bArray[i];
		}
		return port;
	}

	
	// 4A 7D EB 69
	// 74 125 235 105
	public static final String bytesToHexString(byte[] bArray, int begin, int end) {
		StringBuffer sb = new StringBuffer(bArray.length);
		String sTemp;
		for (int i = begin; i < end; i++) {
			sTemp = Integer.toHexString(0xFF & bArray[i]);
			if (sTemp.length() < 2)
				sb.append(0);
			sb.append(sTemp.toUpperCase());
			sb.append(" ");
		}
		return sb.toString();
	}
	
	
	private String generateDestination(byte[] bt) {
		byte type = bt[3];
		if (type == 0x03) {
			//第三个字节为0x03说明需要进行域名解析
			int len=(0x00FF&bt[4]);
			////System.out.println("length: "+len);
			StringBuilder host=new StringBuilder();
			//依次提取出len个字节作为host
			for(int i=5;i<5+len;i++)
			{
				int ascii=0xFF&bt[i];
				char c=(char)ascii;
				//System.err.println(c);
				host.append(c);
			}
			String h=null;
			try {
			 h=InetAddress.getByName(host.toString().trim()).getHostAddress();
			 //System.out.println(h);
			} catch (UnknownHostException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			//紧跟在Host之后的两个字节数据为端口号
			int port=(bt[len+5]<<8)+bt[len+6];
			////System.out.println(h+"@"+port);
			return h+"@"+port;
			

		} else if (type == 0x01) {

			//第三个字节为0x01说明是IP地址
			StringBuffer sb = new StringBuffer();
			for (int i = 4; i <= 7; i++) {
				sb.append(Integer.toString(0x00FF & bt[i]));
				sb.append(".");
			}
			sb.deleteCharAt(sb.length() - 1);
			
			int port = 0;
			for (int i = 8; i <= 9; i++) {
				port <<= 8;
				int b=0x00FF&bt[i];
//				System.err.println(b);
//				System.err.println(bt[i]);
				port += b;
			}
			
			//return port;
			return sb.toString()+"@"+port;
			
		} else {
			//System.out.println("no such tyte.");
		}
		return null;
	}
	
}
