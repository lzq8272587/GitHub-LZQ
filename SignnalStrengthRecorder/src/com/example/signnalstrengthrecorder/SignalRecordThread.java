package com.example.signnalstrengthrecorder;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.os.Message;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;

public class SignalRecordThread extends Thread {
	private StateListener sl = null;

	File log;
	BufferedWriter bw;
	boolean run = true;
	int asu = 0;

	public SignalRecordThread() {
		sl = new StateListener();
		log = new File("/sdcard/phone_state_log.csv");
		try {
			if (!log.exists())
				log.createNewFile();
			bw = new BufferedWriter(new FileWriter(log, true));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void stop_recording() {
		run = false;
	}

	public void run() {
		try {
			while (run) {
				sleep(1000);
				try {
					int temp;
					if ((temp = sl.getGsm()) != asu)// 如果信号强度有变化，记录
					{
						asu = temp;
						System.err.println("Thread is running ... Time: " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) + " Gsm="
								+ sl.getGsm() + " asu " + (-113 + 2 * sl.getGsm()) + " dbm.");
						int asu = sl.getGsm();
						bw.write("" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) + "," + asu + "," + (-113 + 2 * asu) + "\n");
						Message msg = new Message();
						msg.obj = String.valueOf("" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) + " " + asu + " asu "
								+ (-113 + 2 * asu) + " dbm\n");
						// MainActivity.updateSignal.sendMessage(msg);
						bw.flush();
					} else {
						System.out.println("Thread is running ... but siganl dosen't change.");
						bw.write("" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) + "," + asu + "," + (-113 + 2 * asu) + "\n");
						bw.flush();
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			

		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public StateListener getStateListener() {
		return sl;
	}

	class StateListener extends PhoneStateListener {

		private int CdmaDbm = 0;
		private int Gsm = 0;
		private int EvdoDbm = 0;

		@Override
		public void onSignalStrengthsChanged(SignalStrength signalStrength) {
			// TODO Auto-generated method stub
			super.onSignalStrengthsChanged(signalStrength);
			CdmaDbm = signalStrength.getCdmaDbm();
			Gsm = signalStrength.getGsmSignalStrength();
			EvdoDbm = signalStrength.getEvdoDbm();
			System.out.println("from function: onSignalStrengthsChanged, Gsm= " + Gsm + " dbm.");
		}

		public int getCdmaDbm() {
			return CdmaDbm;
		}

		public int getGsm() {
			return Gsm;
		}

		public int getEvdoDbm() {
			return EvdoDbm;
		}

	}
}
