package com.lzq.trafficdirector.gui;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Vibrator;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.lzq.trafficdirector.global.Parameters;
import com.lzq.trafficdirector.global.Utils;
import com.lzq.trafficdirector.proxy.AsynchronousProxyServer;
import com.lzq.trafficdirector.proxy.ProxyServer;
import com.lzq.trafficdirector.proxy.SynchronousProxyServer;

public class SwitchServiceFragment extends Fragment {

	View SwitchServiceView = null;
	ImageButton start = null;
	ImageButton stop = null;
	ImageView iv = null;

	// LocalProxy
	ProxyServer LocalProxy = null;
	Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			loading.setMessage((String) msg.obj);
			System.out.println("Handler MSG: " + (String) msg.obj);
		}

	};

	Handler toast_handler = new Handler();

	ProgressDialog loading = null;

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onActivityCreated(savedInstanceState);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		// TODO Auto-generated method stubreturn super.onCreateView(inflater,
		// container, savedInstanceState);
		SwitchServiceView = inflater.inflate(R.layout.switch_service_layout, null);
		iv = (ImageView) (SwitchServiceView.findViewById(R.id.Background));
		start = (ImageButton) (SwitchServiceView.findViewById(R.id.StartServiceButton));
		start.getBackground().setAlpha(0);// 透明按钮
		stop = (ImageButton) (SwitchServiceView.findViewById(R.id.StopServiceButton));
		stop.getBackground().setAlpha(0);// 透明按钮

		start.setOnClickListener(new StartListener());
		stop.setOnClickListener(new StopListener());
		return SwitchServiceView;
	}

	// 启动服务之前重置redsocks状态
	private void resetStates() {
		try {
			/**
			 * 重置redsocks状态
			 */
			Runtime.getRuntime().exec("su");
			Runtime.getRuntime().exec("su -c " + getActivity().getCacheDir().getAbsolutePath() + "/stop.sh").waitFor();
			Runtime.getRuntime().exec("su -c " + getActivity().getCacheDir().getAbsolutePath() + "/start.sh").waitFor();
			/**
			 * 制作并执行启动脚本，用于配置IPTABLES
			 */
			StringBuilder scriptBuilder = new StringBuilder();
			scriptBuilder.append(getActivity().getCacheDir().getAbsolutePath() + "/iptables -t nat -F\n");
			for (int uid : Parameters._Wifi_Set) {
				System.out.println("执行iptables规则：" + getActivity().getCacheDir().getAbsolutePath() + "/iptables -t nat -m owner --uid-owner " + uid
						+ " -A OUTPUT -p tcp -j REDIRECT --to " + Parameters.LocalRedsocksPort + " || exit\n");

				scriptBuilder.append(getActivity().getCacheDir().getAbsolutePath() + "/iptables -t nat -m owner --uid-owner " + uid
						+ " -A OUTPUT -p tcp -j REDIRECT --to " + Parameters.LocalRedsocksPort + " \n");

			}
			generateScriptInCache(scriptBuilder);
			Runtime.getRuntime().exec("su -c " + getActivity().getCacheDir().getAbsolutePath() + "/script.sh").waitFor();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	class StartListener implements OnClickListener {
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			/**
			 * 启动服务相关操作
			 */
			loading = ProgressDialog.show(iv.getContext(), "Start Service", "Reset redsocks... ...");
			new Thread() {
				@Override
				public void run() {
					// 重置redsocks
					resetStates();
					// start local proxy
					//从配置文件中读取RemoteProxy相关信息
					Parameters.RemoteProxyIP=Utils.getPreference(getActivity(), "REMOTE_DST", "REMOTE_IP", Parameters.DefaultRemoteProxyIP);
					Parameters.RemoteProxyPort=Integer.parseInt(Utils.getPreference(getActivity(), "REMOTE_DST", "REMOTE_Port", ""+Parameters.DefaultRemoteProxyPort));	
					if (((MainActivity) getActivity()).doBindService()) {
						// 服务启动成功
						loading.dismiss();
						Vibrator vibrator = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);
						long[] pattern = { 0, 500, 500, 500 }; // OFF/ON/OFF/ON...
						vibrator.vibrate(pattern, -1);
						Looper.prepare();
						Toast.makeText(getActivity(), "service start!", Toast.LENGTH_SHORT).show();
						Looper.loop();
					} else {
						// 服务已经启动
						loading.dismiss();
						Vibrator vibrator = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);
						long[] pattern = { 0, 500, 500, 500 }; // OFF/ON/OFF/ON...
						vibrator.vibrate(pattern, -1);
						Looper.prepare();
						Toast.makeText(getActivity(), "service is already running!", Toast.LENGTH_SHORT).show();
						Looper.loop();
					}
				}
			}.start();

		}
	}

	private void generateScriptInCache(StringBuilder ctx) throws Exception {

		File sh_file = new File(getActivity().getCacheDir().getAbsoluteFile() + "/script.sh");
		if (sh_file.exists())
			sh_file.delete();
		sh_file.createNewFile();
		BufferedWriter bw = new BufferedWriter(new FileWriter(sh_file));
		bw.write(ctx.toString());
		bw.flush();
		bw.close();
		Runtime.getRuntime().exec("chmod 777 " + sh_file.getAbsolutePath()).waitFor();
		Runtime.getRuntime().exec("su -c" + sh_file.getAbsolutePath());

	}

	class StopListener implements OnClickListener {
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			loading = ProgressDialog.show(iv.getContext(), "Stop Service", "Reset redsocks... ...");
			new Thread() {
				@Override
				public void run() {
					// 清除已有的IPTABLES规则，并且关闭redsocks
					try {
						Runtime.getRuntime().exec("su");
						Runtime.getRuntime().exec("su -c " + getActivity().getCacheDir().getAbsolutePath() + "/stop.sh");
						Runtime.getRuntime().exec("su -c " + getActivity().getCacheDir().getAbsolutePath() + "/stop_re.sh");
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					// 关闭服务
					if (((MainActivity) getActivity()).doUnbindService()) {
						loading.dismiss();
						Vibrator vibrator = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);
						long[] pattern = { 0, 500, 500, 500 }; // OFF/ON/OFF/ON...
						vibrator.vibrate(pattern, -1);
						Looper.prepare();
						Toast.makeText(getActivity(), "service stop!", Toast.LENGTH_SHORT).show();
						Looper.loop();
					} else {
						loading.dismiss();
						Vibrator vibrator = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);
						long[] pattern = { 0, 500, 500, 500 }; // OFF/ON/OFF/ON...
						vibrator.vibrate(pattern, -1);
						Looper.prepare();
						Toast.makeText(getActivity(), "service has already stop!", Toast.LENGTH_SHORT).show();
						Looper.loop();
					}

				}
			}.start();
		}

	}

}
