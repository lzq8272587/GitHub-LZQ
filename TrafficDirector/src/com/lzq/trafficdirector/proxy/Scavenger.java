package com.lzq.trafficdirector.proxy;

import java.io.IOException;
import java.net.Socket;
import java.util.HashSet;
import java.util.Iterator;

public class Scavenger extends Thread {
	private HashSet<Socket> SocketPool;
	private Iterator<Socket> itr;
private boolean loop;
	Scavenger() {
		loop=true;
		SocketPool = new HashSet<Socket>();
	}

	public void addSocket(Socket s) {
		SocketPool.add(s);
	}

	public void stoplooping()
	{
		loop=false;
	}
	public void run() {
		while (loop) {
			// 每15秒扫描一次
			try {
				sleep(20* 1000);
				itr = SocketPool.iterator();
				// 将一些无用Socket关闭掉
				while (itr.hasNext()) {
					Socket s = itr.next();
					if (s.isInputShutdown() && s.isOutputShutdown()) {
						s.close();
						itr.remove();
					}
				}
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

	}

}
