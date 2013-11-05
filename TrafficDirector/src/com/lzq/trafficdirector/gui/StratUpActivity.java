package com.lzq.trafficdirector.gui;


import com.lzq.trafficdirector.global.Parameters;
import com.lzq.trafficdirector.global.Utils;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.view.Menu;

public class StratUpActivity extends Activity {
	 ProgressDialog loading=null;
	 
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.strat_up_layout);
		Handler x = new Handler();
		x.postDelayed(new splashhandler(), 0);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.strat_up, menu);
		return true;
	}
	class splashhandler implements Runnable {

		public void run() {
			loading=ProgressDialog.show(StratUpActivity.this, "Start Up......", "Reading appinfo from saved prefernce ......\ncopying relative file and generate scripts....");	
			new Thread()
			{
				public void run()
				{
					//初始化工作
					/**
					 * 从配置文件中读取信息，看哪些应用程序被标记了
					 */
					try {
						Utils.read_conf_from_Preference_to_Set(getApplication());
						/**
						 * 拷贝redsocks以及脚本文件
						 */
						Utils.ensureRedsocks(getApplicationContext());
						/**
						 * 设置远端代理,如果没有保存任何地址信息，初始化为Parameters中的数值
						 */
						Utils.initialRemoteDst(getApplicationContext());
						sleep(1000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					finally
					{
						loading.dismiss();
						startActivity(new Intent(getApplication(), MainActivity.class));
						StratUpActivity.this.finish();
					}
				}
				
			}.start();
			

		}

	}
	
	

}
