package com.lzq.trafficdirector.proxy;

/**
 * * 从外部读取，向内部发送信息
 */
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.zip.GZIPOutputStream;

import com.lzq.trafficdirector.global.TransmissionUnit;
import com.lzq.trafficdirector.utils.GZipTools;
import com.lzq.trafficdirector.utils.Logger;

public class BlockedSocketThread_Data_from_Server_to_Terminal extends Thread {
	// 本线程中，负责从服务器读取返回信息，并将这些信息打包压缩，返回给本地代理
	private InputStream isOut;
	// for compression
	private ObjectOutputStream oos = null;
	private GZIPOutputStream gos = null;
	private Socket socketIn;
	private Socket socketOut;

	public BlockedSocketThread_Data_from_Server_to_Terminal(InputStream isOut, ObjectOutputStream osIn, Socket in, Socket out) {
		this.isOut = isOut;
		oos = osIn;

		socketIn = in;
		socketOut = out;

		// this.gos=gos;

	}

	private byte[] buffer = new byte[409600];
	private TransmissionUnit tu = new TransmissionUnit(null, false);

	public void run() {
		try {
			int len;
			byte[] tempData;
			while ((len = isOut.read(buffer)) != -1) {
				if (len > 0) {
					// compress
					tempData = new byte[len];
					System.arraycopy(buffer, 0, tempData, 0, len);
					tempData = GZipTools.compress(tempData);
					// System.err.println("compress rate: "+(float)(tempData.length)/len);
					if (BlockedSocketProxy.getOutputLogger() != null)
						BlockedSocketProxy.getOutputLogger().addData(len, tempData.length);
					tu.datas = tempData;
					tu.isCompressed = true;
					oos.writeObject(tu);
					oos.flush();
					// osIn.write(buffer, 0, len);
					// osIn.flush();
				}
			}
		} catch (Exception e) {
			System.out.println("SocketThreadInput leave");
		} finally {
			// 数据读取完毕之后关闭两个方向上的输入输出流
			try {
				// gos.finish();
				socketIn.shutdownOutput();
				socketOut.shutdownInput();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
	}
}
