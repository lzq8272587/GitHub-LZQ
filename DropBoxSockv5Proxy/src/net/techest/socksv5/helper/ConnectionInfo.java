package net.techest.socksv5.helper;

public class ConnectionInfo {
	
	
	public ConnectionInfo(String src_IP, String des_IP, String src_Port,
			String des_Port, String start_time, String end_time,
			String upload_traffic, String download_traffic) {
		super();
		this.src_IP = src_IP;
		this.des_IP = des_IP;
		this.src_Port = src_Port;
		this.des_Port = des_Port;
		this.start_time = start_time;
		this.end_time = end_time;
		this.upload_traffic = upload_traffic;
		this.download_traffic = download_traffic;
	}
	
	public String getInfo()
	{
		return src_IP+","+des_IP+","+src_Port+","+des_Port+","+start_time+","+end_time+","+upload_traffic+","+download_traffic+"\n";
	}
	String src_IP;
	String des_IP;
	String src_Port;
	String des_Port;
	String start_time;
	String end_time;
	String upload_traffic;
	String download_traffic;
	
	
	

}
