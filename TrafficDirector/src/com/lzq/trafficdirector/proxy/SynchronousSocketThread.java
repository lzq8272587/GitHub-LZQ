package com.lzq.trafficdirector.proxy;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import com.lzq.trafficdirector.global.Parameters;

public class SynchronousSocketThread extends Thread {
	private Socket socketIn;
	private InputStream isIn;
	private OutputStream osIn;
	//
	private Socket socketOut;
	private InputStream isOut;
	private OutputStream osOut;

	public SynchronousSocketThread(Socket socket) {
		this.socketIn = socket;
	}

	private byte[] buffer = new byte[4096];

	public void run() {
		try {
			// System.out.println("a client connect " +
			// socketIn.getRemoteSocketAddress() + ":" + socketIn.getPort());
			isIn = socketIn.getInputStream();
			osIn = socketIn.getOutputStream();
			/**
			 * 代理服务器建立和目标服务器之间的连接
			 */
			socketOut = new Socket(Parameters.RemoteProxyIP, Parameters.RemoteProxyPort);
			isOut = socketOut.getInputStream();
			osOut = socketOut.getOutputStream();

			SynchronousSocketThread_Data_from_App_to_Remote out = new SynchronousSocketThread_Data_from_App_to_Remote(isIn, osOut, socketIn,
					socketOut);
			out.start();
			SynchronousSocketThread_Data_from_Remote_to_App in = new SynchronousSocketThread_Data_from_Remote_to_App(isOut, osIn, socketIn, socketOut);
			in.start();
			out.join();
			in.join();
		} catch (Exception e) {
			System.out.println("a client leave");
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
		System.out.println("socket close");
	}
}
