package com.way.client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * 客户端
 * 
 * @author way
 * 
 */
public class Client extends Thread {

	private Socket client;
	private ClientThread clientThread;
	private String ip;
	private int port;

	boolean IsStart;

	Thread callTrd=null;
	
	public Client(String ip, int port) {
		this.ip = ip;
		this.port = port;
	}

	public void run() {
		//System.out.println("in run: "+Thread.currentThread().getName());
		client = new Socket();
		// client.connect(new InetSocketAddress(Constants.SERVER_IP,
		// Constants.SERVER_PORT), 3000);
		try {
			client.connect(new InetSocketAddress(ip, port), 3000);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (client.isConnected()) {
			// System.out.println("Connected..");
			clientThread = new ClientThread(client);
			clientThread.start();
			IsStart=true;
		}
		else
		{
			IsStart=false;
		}
	}
 
	public boolean isIsStart() {
		return IsStart;
	}

	public boolean start_conn() {
		//System.out.println("in start_conn: "+Thread.currentThread().getName());
		this.start();
		try {
			this.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return client.isConnected();
	}

	// 直接通过client得到读线程
	public ClientInputThread getClientInputThread() {
		return clientThread.getIn();
	}

	// 直接通过client得到写线程
	public ClientOutputThread getClientOutputThread() {
		return clientThread.getOut();
	}

	// 直接通过client停止读写消息
	public void setIsStart(boolean isStart) {
		clientThread.getIn().setStart(isStart);
		clientThread.getOut().setStart(isStart);
	}

	public class ClientThread extends Thread {

		private ClientInputThread in;
		private ClientOutputThread out;

		public ClientThread(Socket socket) {
			in = new ClientInputThread(socket);
			out = new ClientOutputThread(socket);
		}

		public void run() {
			in.setStart(true);
			out.setStart(true);
			in.start();
			out.start();
		}

		// 得到读消息线程
		public ClientInputThread getIn() {
			return in;
		}

		// 得到写消息线程
		public ClientOutputThread getOut() {
			return out;
		}
	}
}
