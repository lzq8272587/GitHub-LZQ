package com.lzq.trafficdirector.gui;

import com.lzq.trafficdirector.global.Parameters;
import com.lzq.trafficdirector.global.Utils;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceFragment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.EditText;

public class PreferenceConfigureActivity extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.hello_world);
		getFragmentManager().beginTransaction()
				.replace(R.id.preference_linearlayout, new PrefsFragement()).commit();
	}


	public static class PrefsFragement extends PreferenceFragment {
		EditTextPreference etpIP;
		EditTextPreference etpPort;
		SharedPreferences sbRemoteDst;
		String IP;
		String Port;
		@Override
		public void onCreate(Bundle savedInstanceState) {
			// TODO Auto-generated method stub
			super.onCreate(savedInstanceState);
			addPreferencesFromResource(R.xml.preference_configure);		
			sbRemoteDst=getActivity().getSharedPreferences("REMOTE_DST", getActivity().MODE_WORLD_READABLE|getActivity().MODE_WORLD_WRITEABLE);
            IP=sbRemoteDst.getString("REMOTE_IP", Parameters.RemoteProxyIP);
            Port=sbRemoteDst.getString("REMOTE_Port", ""+Parameters.RemoteProxyPort);
			//设置远端代理IP
		    etpIP=(EditTextPreference)findPreference("REMOTE_IP_EDITTEXTPREFERENCE");
		    etpIP.setSummary(IP);
		    etpIP.setText(IP);
		    
			etpIP.setOnPreferenceChangeListener(new OnPreferenceChangeListener()
			{
				@Override
				public boolean onPreferenceChange(Preference arg0, Object IP) {
					// TODO Auto-generated method stub
					System.out.println(IP.toString());
					Utils.setPreference(getActivity(), "REMOTE_DST", "REMOTE_IP", IP.toString());
					etpIP.setSummary(IP.toString());
					etpIP.setText(IP.toString());
					return false;
				}
			});
			
			//设置远端代理端口
		    etpPort=(EditTextPreference)findPreference("REMOTE_PORT_EDITTEXTPREFERENCE");
		    etpPort.setSummary(Port);
		    etpPort.setText(Port);
		    etpPort.setOnPreferenceChangeListener(new OnPreferenceChangeListener()
			{
				@Override
				public boolean onPreferenceChange(Preference arg0, Object Port) {
					// TODO Auto-generated method stub
					System.out.println(Port.toString());
					Utils.setPreference(getActivity(), "REMOTE_DST", "REMOTE_Port", Port.toString());
					etpPort.setSummary(Port.toString());
					etpPort.setText(Port.toString());
					return false;
				}
			});
		}
	}

}
