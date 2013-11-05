package com.lzq.trafficdirector.global;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class Utils {
	/**
	 * 获取Preference中内容
	 */
	public static  String getPreference(Context ctx,String PreferenceName,String PreferenceKey,String defaultValue)
	{
		SharedPreferences sb=ctx.getSharedPreferences(PreferenceName, ctx.MODE_WORLD_READABLE|ctx.MODE_WORLD_WRITEABLE);
		return sb.getString(PreferenceKey, defaultValue);
	}
	/**
	 * 修改Preference内容
	 */
	public static void setPreference(Context ctx,String PreferenceName,String PreferenceKey,String Value)
	{
		SharedPreferences sb=ctx.getSharedPreferences(PreferenceName, ctx.MODE_WORLD_READABLE|ctx.MODE_WORLD_WRITEABLE);
		Editor e=sb.edit();
		e.putString(PreferenceKey, Value);
		e.commit();
		return;
	}
	/**
	 * 初始化远端代理地址信息
	 */
	public static void initialRemoteDst(Context ctx)
	{
		SharedPreferences sbRemoteDst;
		String IP;
		String Port;
		sbRemoteDst=ctx.getSharedPreferences("REMOTE_DST", ctx.MODE_WORLD_READABLE|ctx.MODE_WORLD_WRITEABLE);
        IP=sbRemoteDst.getString("REMOTE_IP", null);
        Port=sbRemoteDst.getString("REMOTE_Port", null);
        Editor e=sbRemoteDst.edit();
        if(IP==null)
        {
        	IP=Parameters.RemoteProxyIP;
        	e.putString("REMOTE_IP", IP);
        }
        if(Port==null)
        {
        	Port=""+Parameters.RemoteProxyPort;
        	e.putString("REMOTE_PORT", Port);
        }
		e.commit();
	}
	public static void root()
	{
		/**
		 * 获取root权限
		 */
		try {
			Runtime.getRuntime().exec("su").waitFor();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	/**
	 * 把全局变量中的保存的UID信息写入配置文件中
	 * @param ctx
	 */
	public static void write_conf_from_Set_to_Preference(Context ctx)
	{
		SharedPreferences sb=ctx.getSharedPreferences(Parameters.PREFS_NAME, ctx.MODE_WORLD_READABLE|ctx.MODE_WORLD_WRITEABLE);
		StringBuilder _3G_set=new StringBuilder();
		StringBuilder _Wifi_set=new StringBuilder();
		Editor editor=sb.edit();
		
		for(int i:Parameters._3G_Set)
		{
			_3G_set.append("@"+i);
		}
		for(int i:Parameters._Wifi_Set)
		{
			_Wifi_set.append("@"+i);
		}
		
		editor.clear();
		
		editor.putString(Parameters.PREF_3G_UIDS,_3G_set.toString());
		editor.putString(Parameters.PREF_WIFI_UIDS,_Wifi_set.toString());
		System.out.println("3g: "+_3G_set.toString());
		System.out.println("wifi: "+_Wifi_set.toString());
		editor.commit();
	}
	/**
	 * 将配置文件中保存的UID信息写入全局静态变量Set中
	 * @param ctx
	 */
	public static void read_conf_from_Preference_to_Set(Context ctx)
	{
		SharedPreferences sb=ctx.getSharedPreferences(Parameters.PREFS_NAME, ctx.MODE_WORLD_READABLE|ctx.MODE_WORLD_WRITEABLE);
		String[] _3G_set=null;
		String[] _Wifi_set=null;
		Parameters._3G_Set.clear();
		Parameters._Wifi_Set.clear();	
		System.out.println("read 3g: "+sb.getString(Parameters.PREF_3G_UIDS, ""));
		System.out.println("read wifi: "+sb.getString(Parameters.PREF_WIFI_UIDS, ""));
		_3G_set=sb.getString(Parameters.PREF_3G_UIDS, "").split("@");
		_Wifi_set=sb.getString(Parameters.PREF_WIFI_UIDS, "").split("@");

		for(String s:_3G_set)
		{
			if(s.length()>0)
			Parameters._3G_Set.add(Integer.parseInt(s));
		}
		
		
		for(String s:_Wifi_set)
		{
			if(s.length()>0)
			Parameters._Wifi_Set.add(Integer.parseInt(s));
		}
	}
	/**
	 * 本函数实现以下功能：
	 * 0.拷贝IPTABLES,redsocks到cache目录下
	 * 1.获取root权限
	 * 2.刷新cache目录下的redsocks.conf文件
	 * 3.刷新cache目录下的start.sh文件
	 * 4.刷新cache目录下的start_re.sh文件，此文件中包含IPTABLES规则，它是依照Parameters类中的两个集合来设定的
	 *   集合中的值在每次“AppList”中的数据刷新时，会被修改，并且在“AppList”选项卡不可见时会被写入配置文件中；
	 *   每次启动应用程序时，会从配置文件里读取UID信息放入两个集合中。
	 * 5.刷新cache目录下的stop.sh文件
	 * 6.刷新cache目录下的stop_re.sh文件
	 * @param ctx
	 * @throws InterruptedException 
	 */
	public static void ensureRedsocks(Context ctx) throws InterruptedException {
		/**
		 * 获取root权限
		 */
		try {
			Runtime.getRuntime().exec("su");
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		/**
		 * 1.拷贝redsocks,iptables到目录下
		 */
		assertBinaries(ctx);		
		/**
		 * 2.设置redsocks.conf
		 */
		BufferedWriter bw=null;
		
		File redsocks_conf_file=new File(ctx.getCacheDir(),"redsocks.conf");
		/**
		 * 保证每次的配置文件都是最新的
		 */
		if(redsocks_conf_file.exists())
			redsocks_conf_file.delete();
		try {
			redsocks_conf_file.createNewFile();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		

		StringBuilder redsocks_conf_builder = new StringBuilder();
		redsocks_conf_builder.append("base {\n");
		redsocks_conf_builder.append("    log_debug = on;\n");
		redsocks_conf_builder.append("    log_info = on;\n");
		redsocks_conf_builder.append("    log = \"file:" + ctx.getCacheDir()
				+ "/redsocks.log\";\n");
		redsocks_conf_builder.append("    daemon = on;\n");
		redsocks_conf_builder.append("    redirector = iptables;\n");
		redsocks_conf_builder.append("}\n");
		
		redsocks_conf_builder.append("redsocks {\n");
		redsocks_conf_builder.append("    local_ip ="+Parameters.LoacalRedsocksIP+";\n");
		redsocks_conf_builder.append("    local_port ="+Parameters.LocalRedsocksPort+";\n");
		redsocks_conf_builder.append("    ip ="+Parameters.LocalProxyIP+";\n");
		redsocks_conf_builder.append("    port ="+Parameters.LocalProxyPort+";\n");
		redsocks_conf_builder.append("    type = socks5;\n");
	//	redsocks_conf_builder.append("    login = \"\";\n");
	//	redsocks_conf_builder.append("    password = \"\";\n");
		redsocks_conf_builder.append("}\n");
		
		try {
			bw=new BufferedWriter(new FileWriter(redsocks_conf_file));
			bw.write(redsocks_conf_builder.toString());
			bw.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		/**
		 * 3.设置start.sh,用于启动redsock
		 */
		StringBuilder redsocks_start_builder=new StringBuilder();
		
		File redsocks_start_file=new File(ctx.getCacheDir(),"start.sh");
		/**
		 * 保证每次的配置文件都是最新的
		 */
		if(redsocks_start_file.exists())
			redsocks_start_file.delete();
		try {
			redsocks_start_file.createNewFile();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		redsocks_start_builder.append("cd "+ctx.getCacheDir().getAbsolutePath()+"\n");
		redsocks_start_builder.append("if [ -e redsocks.log ] ; then \n");
		redsocks_start_builder.append("    rm redsocks.log\n");
		redsocks_start_builder.append("fi\n");
		redsocks_start_builder.append("./redsocks -p "+ctx.getCacheDir().getAbsolutePath()+"/redsocks.pid\n");
		try {
			bw=new BufferedWriter(new FileWriter(redsocks_start_file));
			bw.write(redsocks_start_builder.toString());
			bw.flush();
			Runtime.getRuntime().exec("chmod " + 755 + " " + redsocks_start_file.getAbsolutePath()).waitFor();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		/**
		 * 4.设置start_re.sh,用于配置iptables进而重定向数据包
		 */
		StringBuilder redsocks_start_re_builder=new StringBuilder();
		
		File redsocks_start_re_file=new File(ctx.getCacheDir(),"start_re.sh");
		/**
		 * 保证每次的配置文件都是最新的
		 */
		if(redsocks_start_re_file.exists())
			redsocks_start_re_file.delete();
		try {
			redsocks_start_re_file.createNewFile();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
// # Create the droidwall chains if necessary
//$IPTABLES -L droidwall >/dev/null 2>/dev/null || $IPTABLES --new droidwall || exit 2
//$IPTABLES -L droidwall-3g >/dev/null 2>/dev/null || $IPTABLES --new droidwall-3g || exit 3
//$IPTABLES -L droidwall-wifi >/dev/null 2>/dev/null || $IPTABLES --new droidwall-wifi || exit 4
//$IPTABLES -L droidwall-reject >/dev/null 2>/dev/null || $IPTABLES --new droidwall-reject || exit 5
		//$IPTABLES -L OUTPUT | $GREP -q droidwall || $IPTABLES -A OUTPUT -j droidwall || exit 6
		
//		$IPTABLES -F droidwall || exit 7
//		$IPTABLES -F droidwall-3g || exit 8
//		$IPTABLES -F droidwall-wifi || exit 9
//		$IPTABLES -F droidwall-reject || exit 10
		
		/**
		 * 插入几条规则链
		 */
//		redsocks_start_re_builder.append("/system/bin/iptables -L trafficdirector >/dev/null 2>/dev/null || /system/bin/iptables --new trafficdirector || exit 1\n");
//		redsocks_start_re_builder.append("/system/bin/iptables -L trafficdirector-3g >/dev/null 2>/dev/null || /system/bin/iptables --new trafficdirector-3g || exit 2\n");
//		redsocks_start_re_builder.append("/system/bin/iptables -L trafficdirector-wifi >/dev/null 2>/dev/null || /system/bin/iptables --new trafficdirector-wifi || exit 3\n");
//		redsocks_start_re_builder.append("/system/bin/iptables -L trafficdirector-accept >/dev/null 2>/dev/null || /system/bin/iptables --new trafficdirector-accept || exit 4\n");
//		redsocks_start_re_builder.append("/system/bin/iptables -L OUTPUT >/dev/null 2>/dev/null || /system/bin/iptables -A OUTPUT -j trafficdirector || exit 5\n");
		/**
		 * 刷新规则链
		 */
//		redsocks_start_re_builder.append("/system/bin/iptables -F trafficdirector || exit 6\n");		
//		redsocks_start_re_builder.append("/system/bin/iptables -F trafficdirector-3g || exit 7\n");	
//		redsocks_start_re_builder.append("/system/bin/iptables -F trafficdirector-wifi || exit 8\n");	
//		redsocks_start_re_builder.append("/system/bin/iptables -F trafficdirector-accept || exit 9\n");	
//		
//		redsocks_start_re_builder.append("/system/bin/iptables -A trafficdirector-accept -j ACCEPT || exit \n");	

		//		$IPTABLES -A droidwall -o rmnet+ -j droidwall-3g || exit
//		$IPTABLES -A droidwall -o pdp+ -j droidwall-3g || exit
//		$IPTABLES -A droidwall -o ppp+ -j droidwall-3g || exit
//		$IPTABLES -A droidwall -o uwbr+ -j droidwall-3g || exit
//		$IPTABLES -A droidwall -o wimax+ -j droidwall-3g || exit
//		$IPTABLES -A droidwall -o vsnet+ -j droidwall-3g || exit
//		$IPTABLES -A droidwall -o ccmni+ -j droidwall-3g || exit
//		$IPTABLES -A droidwall -o usb+ -j droidwall-3g || exit
//		$IPTABLES -A droidwall -o tiwlan+ -j droidwall-wifi || exit
//		$IPTABLES -A droidwall -o wlan+ -j droidwall-wifi || exit
//		$IPTABLES -A droidwall -o eth+ -j droidwall-wifi || exit
//		$IPTABLES -A droidwall -o ra+ -j droidwall-wifi || exit
		
		/**
		 * 将不同网卡转发的数据包分配到不同的过滤链上去
		 */
//		redsocks_start_re_builder.append("/system/bin/iptables -A trafficdirector -o rmnet+ -j trafficdirector-3g || exit\n");
//		redsocks_start_re_builder.append("/system/bin/iptables -A trafficdirector -o pdp+ -j trafficdirector-3g || exit\n");
//		redsocks_start_re_builder.append("/system/bin/iptables -A trafficdirector -o ppp+ -j trafficdirector-3g || exit\n");
//		redsocks_start_re_builder.append("/system/bin/iptables -A trafficdirector -o uwbr+ -j trafficdirector-3g || exit\n");
//		redsocks_start_re_builder.append("/system/bin/iptables -A trafficdirector -o wimax+ -j trafficdirector-3g || exit\n");
//		redsocks_start_re_builder.append("/system/bin/iptables -A trafficdirector -o vsnet+ -j trafficdirector-3g || exit\n");
//		redsocks_start_re_builder.append("/system/bin/iptables -A trafficdirector -o ccmni+ -j trafficdirector-3g || exit\n");
//		redsocks_start_re_builder.append("/system/bin/iptables -A trafficdirector -o usb+  -j trafficdirector-3g || exit\n");
//		
//		redsocks_start_re_builder.append("/system/bin/iptables -A trafficdirector -o tiwlan+  -j trafficdirector-wifi || exit\n");
//		redsocks_start_re_builder.append("/system/bin/iptables -A trafficdirector -o wlan+  -j trafficdirector-wifi || exit\n");
//		redsocks_start_re_builder.append("/system/bin/iptables -A trafficdirector -o eth+  -j trafficdirector-wifi || exit\n");
//		redsocks_start_re_builder.append("/system/bin/iptables -A trafficdirector -o ra+  -j trafficdirector-wifi || exit\n");
//		
		/**
		 * 在不同的链上设置不同的过滤规则
		 */
//		$IPTABLES -A droidwall-wifi -m owner --uid-owner 1010 -j RETURN || exit
//		$IPTABLES -A droidwall-3g -m owner --uid-owner 10144 -j RETURN || exit
//		$IPTABLES -A droidwall-3g -m owner --uid-owner 10026 -j RETURN || exit
//		$IPTABLES -A droidwall-3g -m owner --uid-owner 10116 -j RETURN || exit
//		$IPTABLES -A droidwall-3g -m owner --uid-owner 10102 -j RETURN || exit
//		$IPTABLES -A droidwall-3g -j droidwall-reject || exit
//		$IPTABLES -A droidwall-wifi -j droidwall-reject || exit
		
		//redsocks_start_re_builder.append("/system/bin/iptables -A trafficdirector -o ra+  -j trafficdirector-wifi || exit");
		
		//暂时考虑重定向wifi流量
		redsocks_start_re_builder.append(ctx.getCacheDir().getAbsolutePath()+"/iptables -t nat -F\n");
		for(int uid:Parameters._Wifi_Set)
		{
		//iptables -t nat -A OUTPUT -p tcp -j REDIRECT --to 8123\n
			redsocks_start_re_builder.append(ctx.getCacheDir().getAbsolutePath()+"/iptables -t nat -m owner --uid-owner "+uid+" -A OUTPUT -p tcp -j REDIRECT --to "+Parameters.LocalRedsocksPort+" || exit\n");
		System.err.println("写入IPTABLES规则："+ctx.getCacheDir().getAbsolutePath()+"/iptables -t nat -m owner --uid-owner "+uid+" -A OUTPUT -p tcp -j REDIRECT --to "+Parameters.LocalRedsocksPort+" || exit\n");
		}
		
//		redsocks_start_re_builder.append("/system/bin/iptables -A trafficdirector-3g -j trafficdirector-accept || exit\n");
//		redsocks_start_re_builder.append("/system/bin/iptables -A trafficdirector-wifi -j trafficdirector-accept || exit\n");
//		
		try {
			bw=new BufferedWriter(new FileWriter(redsocks_start_re_file));
			bw.write(redsocks_start_re_builder.toString());
			bw.flush();
			Runtime.getRuntime().exec("chmod " + 755 + " " + redsocks_start_re_file.getAbsolutePath()).waitFor();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		/**
		 * 5.设置stop.sh，用于关闭redsocks
		 */
		StringBuilder redsocks_stop_builder=new StringBuilder();
		
		File redsocks_stop_file=new File(ctx.getCacheDir(),"stop.sh");
		/**
		 * 保证每次的配置文件都是最新的
		 */
		if(redsocks_stop_file.exists())
			redsocks_stop_file.delete();
		try {
			redsocks_stop_file.createNewFile();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		redsocks_stop_builder.append("cd "+ctx.getCacheDir().getAbsolutePath()+"\n");
		redsocks_stop_builder.append("if [ -e redsocks.pid ]; then\n");
		redsocks_stop_builder.append("    kill `cat redsocks.pid`\n");
		redsocks_stop_builder.append("    rm redsocks.pid\n");
		redsocks_stop_builder.append("else\n");
		redsocks_stop_builder.append("    echo already killed, anyway, I will try killall\n");
		redsocks_stop_builder.append("    killall -9 redsocks\n");
		redsocks_stop_builder.append("fi\n");
		
		try {
			bw=new BufferedWriter(new FileWriter(redsocks_stop_file));
			bw.write(redsocks_stop_builder.toString());
			bw.flush();
			Runtime.getRuntime().exec("chmod " + 755 + " " + redsocks_stop_file.getAbsolutePath()).waitFor();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		/**
		 * 6.设置stop_re.sh，用于关闭iptables功能
		 */
		
		StringBuilder redsocks_stop_re_builder=new StringBuilder();
		
		File redsocks_stop_re_file=new File(ctx.getCacheDir(),"stop_re.sh");
		/**
		 * 保证每次的配置文件都是最新的
		 */
		if(redsocks_stop_re_file.exists())
			redsocks_stop_re_file.delete();
		try {
			redsocks_stop_re_file.createNewFile();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		redsocks_stop_re_builder.append(ctx.getCacheDir().getAbsolutePath()+"/iptables  -t nat -F OUTPUT\n");
		try {
			bw=new BufferedWriter(new FileWriter(redsocks_stop_re_file));
			bw.write(redsocks_stop_re_builder.toString());
			bw.flush();
			Runtime.getRuntime().exec("chmod " + 755 + " " + redsocks_stop_re_file.getAbsolutePath()).waitFor();

			bw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Copies a raw resource file, given its ID to the given location
	 * 
	 * @param ctx
	 *            context
	 * @param resid
	 *            resource id
	 * @param file
	 *            destination file
	 * @param mode
	 *            file permissions (E.g.: "755")
	 * @throws IOException
	 *             on error
	 * @throws InterruptedException
	 *             when interrupted
	 */
	private static void copyRawFile(Context ctx, int resid, File file,
			String mode) throws IOException, InterruptedException {
		final String abspath = file.getAbsolutePath();
		// Write the iptables binary
		final FileOutputStream out = new FileOutputStream(file);
		final InputStream is = ctx.getResources().openRawResource(resid);
		byte buf[] = new byte[1024];
		int len;
		while ((len = is.read(buf)) > 0) {
			out.write(buf, 0, len);
		}
		out.flush();
		out.close();
		is.close();
		// Change the permissions
		Runtime.getRuntime().exec("chmod " + mode + " " + abspath).waitFor();
	}
	/**
	 * Asserts that the binary files are installed in the cache directory.
	 * 
	 * @param ctx
	 *            context
	 * @param showErrors
	 *            indicates if errors should be alerted
	 * @return false if the binary files could not be installed
	 */
	public static boolean assertBinaries(Context ctx) {
		//boolean changed = false;
		try {
			// Check iptables_g1
			File file = new File(ctx.getCacheDir(), "redsocks");
			if (file.exists()) 
				file.delete();		
				file.createNewFile();
				copyRawFile(ctx, com.lzq.trafficdirector.gui.R.raw.redsocks,
						file, "755");
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		try {
			// Check iptables_g1
			File file = new File(ctx.getCacheDir(), "iptables");
			if (file.exists()) 
				file.delete();		
				file.createNewFile();
				copyRawFile(ctx, com.lzq.trafficdirector.gui.R.raw.iptables,
						file, "755");
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return true;
	}
}
