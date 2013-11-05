package com.lzq.trafficdirector.utils;

public class Logger extends Thread {

	 private double uncompressedData = 0;
	 private double compressedData = 0;

	 private TimeSeriesChart tsc = null;

	public Logger(TimeSeriesChart t) {
		uncompressedData = 0;
		compressedData = 0;
		tsc = t;
	}

	 synchronized public void addData(double s1,double s2)
	{
		addUncompressedData(s1);
		addCompressedData(s2);
	}
	 synchronized public void addUncompressedData(double a)
	{
		uncompressedData+=a;
	}
	
	 synchronized public void addCompressedData(double a)
	{
		compressedData+=a;
	}


	public void run() {
		try {
			//每一秒钟刷新一次统计图
			while(true)
			{
				sleep(1000);
				tsc.refresh(uncompressedData, compressedData);

			}
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
