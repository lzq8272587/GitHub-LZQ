package com.lzq.trafficdirector.global;

import java.util.HashSet;

public class Parameters {
	final public static String DefaultRemoteProxyIP="184.22.185.152";
	final public static int DefaultRemoteProxyPort=12345;
	
	public static String RemoteProxyIP="184.22.185.152";
	public static int RemoteProxyPort=12345;
	
	public static String LoacalRedsocksIP="127.0.0.1";
	public static int LocalRedsocksPort=15982;
	
	public static String LocalProxyIP="127.0.0.1";
	public static int LocalProxyPort=15652;
	
	public static HashSet<Integer> _Wifi_Set=new HashSet();
	public static HashSet<Integer> _3G_Set=new HashSet();
	
	public static final String PREFS_NAME = "TrafficDirector_Preference";
	public static final String PREF_WIFI_UIDS = "TrafficDirector_Preference_WIFI_UID";
	public static final String PREF_3G_UIDS = "TrafficDirector_Preference_3G_UID";
}
