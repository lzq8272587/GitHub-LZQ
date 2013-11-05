package com.lzq.trafficdirector.proxy;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import com.lzq.trafficdirector.utils.Logger;

public class BlockedSocketProxy {
	ServerSocket serverSocket;
	int servPort;
	
	//数据记录器
	static Logger InputLogger=null;
	static Logger OutputLogger=null;

	/**
	 * @param args
	 */
	public BlockedSocketProxy(int servPort) throws Exception {

		this.servPort = servPort;

	}

	public void initial() {
		//启动本地监听Socket
		try {
			serverSocket = new ServerSocket(servPort);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("Sock5 代理在 " + servPort + " 端口启动。");
	}

	public void start() {
		new Thread()
		{
			public void run()
			{
				while (true) {
					Socket socket = null;
					try {
						socket = serverSocket.accept();
					//	System.out.println("New Socks5 Connection.");
						new BlockedSocketThread(socket).start();
					} catch (Exception e) {
						e.printStackTrace();
						if(serverSocket==null)
						{
							System.err.println("server socket error, exit ! ");
							System.exit(0);
						}
					}
				}
			}
		}.start();

	}

	public static Logger getInputLogger() {
		return InputLogger;
	}

	public static void setInputLogger(Logger inputLogger) {
		InputLogger = inputLogger;
	}

	public static Logger getOutputLogger() {
		return OutputLogger;
	}

	public static void setOutputLogger(Logger outputLogger) {
		OutputLogger = outputLogger;
	}
}
