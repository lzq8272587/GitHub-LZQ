package net.techest.socksv5.helper;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;

import javax.swing.filechooser.FileSystemView;

public class Recorder {

	static Recorder r=new Recorder();
	
	static File connection_info=new File(FileSystemView.getFileSystemView().getHomeDirectory().getAbsolutePath()+"\\Connection_Info");
	
	static BufferedWriter bw;
	
	
	public static Recorder getInstance(){
		return r;
	}
	
	public static void recordConnectionInfo(ConnectionInfo ci)
	{
System.err.println(FileSystemView.getFileSystemView().getHomeDirectory().getAbsolutePath()+"\\Connection_Info");
		try {
			if(!connection_info.exists())
				connection_info.createNewFile();
			
			
			bw=new BufferedWriter(new FileWriter(connection_info,true));
			bw.append(ci.getInfo());
			bw.flush();
			bw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
