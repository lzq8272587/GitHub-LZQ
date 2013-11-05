package com.lzq.trafficdirector.gui;

import com.lzq.trafficdirector.global.Parameters;
import com.lzq.trafficdirector.proxy.BlockedSocketProxy;

public class StartInConsole {

	/**
	 * @param args 命令行启动入口
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		try {
			BlockedSocketProxy sp=new BlockedSocketProxy(Parameters.port);
			sp.initial();
			sp.start();
			System.out.println("Remote Server Run At "+Parameters.port);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
