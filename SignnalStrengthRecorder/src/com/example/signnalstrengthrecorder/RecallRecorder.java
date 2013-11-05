package com.example.signnalstrengthrecorder;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.sax.StartElementListener;

public class RecallRecorder extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
//		if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
//			context.startService(new Intent(context,RecorderService.class));
//			System.out.println("ACTION_BOOT_COMPLETED");
//		}
//		else 
//		{
//			System.out.println(intent.toString());
//			if()
//		}
		if(!RecorderService.isRun())
		{
			System.out.println("RecallRecorder BroadcastReceiver the service is not running, start service.");
			context.startService(new Intent(context, RecorderService.class));
		}
	}

}
