package com.lzq.trafficdirector.gui;

import com.lzq.trafficdirector.global.Parameters;
import com.lzq.trafficdirector.proxy.ProxyServer;
import com.lzq.trafficdirector.proxy.SynchronousProxyServer;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

public class LocalProxyService extends Service {

	private ProxyServer LocalProxy = null;
	private final IBinder binder = new LocalProxyServiceBinder();

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return binder;
	}

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		//绑定服务时会调用onCreate方法，此时启动服务
		LocalProxy = new SynchronousProxyServer(Parameters.LocalProxyPort);
		LocalProxy.initial();
		LocalProxy.start();
		System.out.println("LocalProxy run on " + Parameters.LocalProxyPort);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO Auto-generated method stub
		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public boolean onUnbind(Intent intent) {
		// TODO Auto-generated method stub
		//解除绑定时调用，此时关闭服务
		LocalProxy.stop();
		return super.onUnbind(intent);
	}

	class LocalProxyServiceBinder extends Binder {
		public LocalProxyService getService() {
			return LocalProxyService.this;
		}
	}
}
