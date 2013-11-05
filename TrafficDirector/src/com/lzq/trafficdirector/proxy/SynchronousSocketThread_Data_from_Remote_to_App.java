package com.lzq.trafficdirector.proxy;

/**
 * * 从外部读取，向内部发送信息
 */
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.io.StreamCorruptedException;
import java.net.Socket;
import java.util.zip.GZIPInputStream;

import com.lzq.trafficdirector.global.GZipTools;
import com.lzq.trafficdirector.global.TransmissionUnit;

public class SynchronousSocketThread_Data_from_Remote_to_App extends Thread {
	// 在本线程中，将从远端代理接收压缩过的数据，然后进行解压缩，再把它们返回给上层应用程序

	private InputStream isOut;
	private OutputStream osIn;
	// decompression
	private ObjectInputStream oisOut = null;
	private GZIPInputStream gisOut;
	private Socket socketIn;
	private Socket socketOut;

	public SynchronousSocketThread_Data_from_Remote_to_App(InputStream isOut, OutputStream osIn, Socket in, Socket out) {
		this.isOut = isOut;
		this.osIn = osIn;
		socketIn = in;
		socketOut = out;
		try {
			// gisOut=new GZIPInputStream(this.isOut);
			oisOut = new ObjectInputStream(this.isOut);
		} catch (StreamCorruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private byte[] buffer;

	public void run() {
		try {
			TransmissionUnit tempUnit;
			while (true) {
				tempUnit = (TransmissionUnit) oisOut.readObject();
				if (tempUnit.isCompressed) {
					buffer = GZipTools.decompress(tempUnit.datas);
					osIn.write(buffer, 0, buffer.length);
					osIn.flush();
				} else {
					buffer = tempUnit.datas;
					osIn.write(buffer, 0, buffer.length);
					osIn.flush();
				}
			}
		} catch (EOFException e) {
			// System.out.println("Data transfered, SocketThreadInput leave");
		} catch (Exception e) {
			System.out.println("SocketThread Remote to App leave");
			e.printStackTrace();
		} finally {
			try {
				socketIn.shutdownOutput();
				socketOut.shutdownInput();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				// e1.printStackTrace();
			}
		}
	}
}
