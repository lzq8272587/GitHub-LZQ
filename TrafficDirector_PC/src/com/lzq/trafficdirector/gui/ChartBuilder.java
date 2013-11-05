package com.lzq.trafficdirector.gui;


import org.jfree.ui.RefineryUtilities;

import com.lzq.trafficdirector.global.Parameters;
import com.lzq.trafficdirector.proxy.BlockedSocketProxy;
import com.lzq.trafficdirector.utils.Logger;
import com.lzq.trafficdirector.utils.TimeSeriesChart;



public class ChartBuilder {

	/**
	 * @param args
	 */
	public  void build() {
		// TODO Auto-generated method stub
		//启动图像
		TimeSeriesChart demo_terminal_to_server = new TimeSeriesChart("Time varying Un-compressed & Compressed data.(Data from terminal to server)","CDF of uncompressed data","CDF of compressed data","Data from terminal to server");
		demo_terminal_to_server.pack();
		demo_terminal_to_server.setSize(950, 1050);
		RefineryUtilities.positionFrameOnScreen(demo_terminal_to_server, 0.005, 0.3);
		demo_terminal_to_server.setVisible(true);
		
		TimeSeriesChart demo_server_to_terminal = new TimeSeriesChart("Time varying Un-compressed & Compressed data.(Data from server to terminal)","CDF of uncompressed data","CDF of compressed data","Data from server to terminal");
		demo_server_to_terminal.pack();
		demo_server_to_terminal.setSize(950, 1050);
		RefineryUtilities.positionFrameOnScreen(demo_server_to_terminal, 0.995, 0.3);
		demo_server_to_terminal.setVisible(true);
		
		//启动记录服务
		Logger logger_for_demo1=new Logger(demo_terminal_to_server);
		logger_for_demo1.start();
		Logger logger_for_demo2=new Logger(demo_server_to_terminal);
		logger_for_demo2.start();
		
		BlockedSocketProxy.setInputLogger(logger_for_demo1);
		BlockedSocketProxy.setOutputLogger(logger_for_demo2);
		
		try {
			BlockedSocketProxy sp=new BlockedSocketProxy(Parameters.port);
			sp.initial();
			sp.start();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
