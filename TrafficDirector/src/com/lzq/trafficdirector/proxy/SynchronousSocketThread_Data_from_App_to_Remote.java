package com.lzq.trafficdirector.proxy;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.zip.GZIPOutputStream;

import com.lzq.trafficdirector.global.GZipTools;
import com.lzq.trafficdirector.global.TransmissionUnit;

/**
 * 从内部读取，向外部发送信息
 * 
 * @author zxq
 * 
 */
public class SynchronousSocketThread_Data_from_App_to_Remote extends Thread {
	// 在本线程中，来自应用程序的数据被封装，压缩后发送到远端代理上
	// 利用ObjectOutputStream来向远端代理发送封装好的压缩数据
	private InputStream isIn;
	private OutputStream osOut;
	// 用来压缩，并发送封装好的压缩包
	private ObjectOutputStream oosOut;
	private GZIPOutputStream gosOut;
	private Socket socketIn;
	private Socket socketOut;

	public SynchronousSocketThread_Data_from_App_to_Remote(InputStream isIn, OutputStream osOut, Socket in, Socket out) {
		this.isIn = isIn;
		this.osOut = osOut;
		socketIn = in;
		socketOut = out;
		try {
			// 将原始的OutputStream封装到GZIP流中，再封装到Object流中
			// gosOut =new GZIPOutputStream(this.osOut);
			oosOut = new ObjectOutputStream(this.osOut);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			// e.printStackTrace();
		}
	}

	private byte[] buffer = new byte[40960];
	byte[] tempData;

	public void run() {
		try {
			int len;
			long Time = System.currentTimeMillis();
			while ((len = isIn.read(buffer)) != -1) {
				if (len > 0) {
					// 把应用程序发来的数据压缩后转发给远端代理
					tempData = new byte[len];
					System.arraycopy(buffer, 0, tempData, 0, len);
					tempData = GZipTools.compress(tempData);
					oosOut.writeObject(new TransmissionUnit(tempData, true));
					oosOut.flush();
				}
			}
			Time = System.currentTimeMillis() - Time;
			System.out.println("Local Proxy: send all data compressed from App to Remote Server, using " + Time * 0.001 + " s");
		} catch (Exception e) {
			System.out.println("SocketThread App to Remote leave");
			// e.printStackTrace();
		} finally {
			// 数据读取完毕之后关闭两个方向上的输入输出流
			try {
				// gosOut.finish();//完成压缩
				socketIn.shutdownInput();
				socketOut.shutdownOutput();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
	}
}
