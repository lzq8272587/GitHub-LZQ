package com.example.signnalstrengthrecorder;

import java.io.File;
import java.io.IOException;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

	private boolean mIsBound = false;
	static RecorderService recorderservice = null;
	static TextView signalView;

	ProgressDialog pd = null;
	AverageTemperatureChart atc = null;
	
	// double data[]=new double[30000];
	// static public Handler updateSignal=new Handler(){
	// @Override
	// public void handleMessage(Message msg) {
	// // TODO Auto-generated method stub
	// super.handleMessage(msg);
	// //if(recorderservice==null)
	// //signalView.setText("Service not start.");
	// // else
	// // signalView.setText((String)msg.obj);
	//
	// }
	//
	// };
	// 连接到Service时调用

	private Button ShowSignalStrength = null;

	private ServiceConnection sc = new ServiceConnection() {
		public void onServiceConnected(ComponentName name, IBinder service) {
			// TODO Auto-generated method stub
			recorderservice = ((RecorderService.RecorderServiceBinder) service)
					.getService();
		}

		public void onServiceDisconnected(ComponentName name) {
			// TODO Auto-generated method stub
			recorderservice = null;
		}

	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		signalView = (TextView) findViewById(R.id.SignalView);
		Button startButton = (Button) findViewById(R.id.StartRecorder);
		Button stopButton = (Button) findViewById(R.id.StopRecorder);
		Button deleteButton = (Button) findViewById(R.id.DeleteLogFile);

		ShowSignalStrength = (Button) findViewById(R.id.ShowSignalStrength);

		System.out.println("Activity OnCreated.");
		// 启动
		startButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				System.out.println("Start Button Clicked.");

				try {
					Runtime.getRuntime().exec("su");
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				doBindService();
				Toast.makeText(MainActivity.this, "service start!",
						Toast.LENGTH_SHORT).show();
			}
		});
		// 结束
		stopButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				doUnbindService();
				Toast.makeText(MainActivity.this, "service stop!",
						Toast.LENGTH_SHORT).show();
			}
		});

		// 删除日志信息
		deleteButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				File logFile = new File("/sdcard/phone_state_log.csv");
				if (RecorderService.isRun())
					Toast.makeText(
							MainActivity.this,
							"service is running, please delete log file later.",
							Toast.LENGTH_SHORT).show();
				else {
					if (logFile.exists()) {
						logFile.delete();
					}
					Toast.makeText(MainActivity.this, "delete log file.",
							Toast.LENGTH_SHORT).show();

				}
			}
		});

		// 显示信号强度
		ShowSignalStrength.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub

				File logFile = new File("/sdcard/phone_state_log.csv");

				if (!logFile.exists()) {
					new AlertDialog.Builder(getApplicationContext())
							.setTitle("没有记录").setIcon(R.drawable.android)
							.setMessage("日志文件不存在。")
							.setPositiveButton("确定", null);
				}

				else {

					pd = ProgressDialog.show(MainActivity.this, "载入数据",
							"Loading ... ... ");

					
					new Thread() {
						public void run() {
							atc = new AverageTemperatureChart();
							Intent intent= atc
									.execute(getApplicationContext());
							pd.dismiss();
							startActivity(intent);

						}
					}.start();

					
				}

			}

		});

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	public boolean doBindService() {
		if (RecorderService.isRun())
			return false;
		// Establish a connection with the service. We use an explicit
		// class name because we want a specific service implementation that
		// we know will be running in our own process (and thus won't be
		// supporting component replacement by other applications).
		// Intent intent=new
		// Intent("com.example.signalstrengthrecorder.RecorderService");
		// bindService(intent, sc, Context.BIND_AUTO_CREATE);
		startService(new Intent(this, RecorderService.class));
		RecorderService.setRun(true);
		return mIsBound;
	}

	public boolean doUnbindService() {
		// if (mIsBound) {
		// Detach our existing connection.
		// unbindService(sc);
		if (RecorderService.isRun()) {
			RecorderService.setRun(false);
			stopService(new Intent(this, RecorderService.class));
		}
		return true;
		// }
		// return false;
	}

}
