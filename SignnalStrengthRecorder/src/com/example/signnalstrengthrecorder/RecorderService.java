package com.example.signnalstrengthrecorder;

import android.app.Notification;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;

public class RecorderService extends Service {

	final private IBinder binder = new RecorderServiceBinder();
	SignalRecordThread srt = null;

	WifiManager.WifiLock w1;
	PowerManager.WakeLock p1;

	TelephonyManager Tel;
	
	int flags = 0;
	int startID = 0;

	public static boolean run;

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return binder;
	}

	class RecorderServiceBinder extends Binder {
		public RecorderService getService() {
			return RecorderService.this;
		}
	}

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		// 保持系统唤醒
		WifiManager wm = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		w1 = wm.createWifiLock("wifilock");
		PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		p1 = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "powerlock");

		// 保证服务不被任务管理器杀掉
		Notification notification = new Notification();
		startForeground(1, notification);
		
		setRun(true);
	}

	@Override
	public boolean onUnbind(Intent intent) {
		// TODO Auto-generated method stub
		if (srt != null)
			srt.destroy();
		
		
		w1.release();
		p1.release();
		
		if(Tel!=null&&srt!=null)
			Tel.listen(srt.getStateListener(), PhoneStateListener.LISTEN_NONE);
			
		this.stopSelf();
		return super.onUnbind(intent);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO Auto-generated method stub
		this.flags = flags;
		this.startID = startId;

		if(!w1.isHeld())
		w1.acquire();
		if(!p1.isHeld())
		p1.acquire();
		
		System.out.println("Call OnStartCommand.");
		
		if (srt == null)
			srt = new SignalRecordThread();
		
		Tel = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
		Tel.listen(srt.getStateListener(), PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
		srt.start();
		flags = START_STICKY;
		return super.onStartCommand(intent, flags, startId);
		
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		System.out.println("onDestory!.");
		if (srt != null) {
			srt.stop_recording();
		}
		try {
			srt.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//查看服务启动标识位，如果标志位run的值为true说明服务被其它应用杀掉，需要重启
		if (run == true) {
			System.out.println("service killed by other process, restart service.");
			startService(new Intent(this, RecorderService.class));
			return;
		}
		if(w1.isHeld())
		w1.release();
		if(p1.isHeld())
		p1.release();
		
		if(Tel!=null&&srt!=null)
			Tel.listen(srt.getStateListener(), PhoneStateListener.LISTEN_NONE);
	}

	
	public static boolean isRun() {
		return run;
	}

	public static void setRun(boolean run) {
		RecorderService.run = run;
	}

}
