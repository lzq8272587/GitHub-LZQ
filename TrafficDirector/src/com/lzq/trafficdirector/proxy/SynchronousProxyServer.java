package com.lzq.trafficdirector.proxy;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
public class SynchronousProxyServer implements ProxyServer {
	/**
	 * @param args
	 */
	private int port = 0;
	private ServerSocket serverSocket = null;
    private Scavenger scavenger=new Scavenger();
	public SynchronousProxyServer(int servPort) {
		port = servPort;
	}
	@Override
	public void initial() {
		// TODO Auto-generated method stub
		try {
			serverSocket = new ServerSocket(port);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// System.out.println("LocalProxy run on " + port);
	}
	@Override
	public void start() {
		// TODO Auto-generated method stub
		scavenger.start();
		new Thread() {
			public void run() {
				while (!serverSocket.isClosed()) {
					Socket socket = null;
					try {
						socket = serverSocket.accept();
						scavenger.addSocket(socket);
						new SynchronousSocketThread(socket).start();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}.start();
	}
	@Override
	public void stop() {
		// TODO Auto-generated method stub
		// 关闭服务
		try {
			if (!serverSocket.isClosed())
				serverSocket.close();
			scavenger.stoplooping();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}