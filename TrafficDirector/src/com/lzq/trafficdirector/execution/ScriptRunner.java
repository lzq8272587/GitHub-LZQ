package com.lzq.trafficdirector.execution;

import java.io.File;
import java.io.IOException;

public class ScriptRunner extends Thread{
	File script=null;
	String scriptPath=null;
	public ScriptRunner(File s)
	{
		script=s;
		scriptPath=s.getParent();
	}
	public void run()
	{
		try {
			System.out.println(script.getAbsolutePath());
			Runtime.getRuntime().exec(script.getAbsolutePath()).waitFor();
			System.out.println("end execute "+script.getAbsolutePath());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}
