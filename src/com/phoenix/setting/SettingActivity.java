package com.phoenix.setting;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import android.content.Context;
import android.content.res.Configuration;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;

import com.phoenix.police.R;

public class SettingActivity extends PreferenceActivity{
	
	private WifiManager mWifiManager;
	PreferenceScreen wifiScreen;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);
		wifiScreen = (PreferenceScreen) findPreference("setting_wifi_preference");
//		Preference pre = new Preference(this);
//		pre.setTitle("111");
//		screen.addPreference(pre);
		mWifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		updateAccessPoints();
	}
	
	private void updateAccessPoints(){
		int wifiState = mWifiManager.getWifiState();
		switch(wifiState){
		case WifiManager.WIFI_STATE_ENABLED:
			Collection<AccessPoint> accessPoints = constructAccessPoints();
			wifiScreen.removeAll();
			for(AccessPoint accessPoint : accessPoints){
				wifiScreen.addPreference(accessPoint);
			}
			break;
		case WifiManager.WIFI_STATE_ENABLING:
			break;
		case WifiManager.WIFI_STATE_DISABLED:
			break;
		case WifiManager.WIFI_STATE_DISABLING:
			break;
		}
	}
	
	private List<AccessPoint> constructAccessPoints(){
		ArrayList<AccessPoint> accessPoints = new ArrayList<AccessPoint>();
		mWifiManager.startScan();
		List<ScanResult> results = mWifiManager.getScanResults();
		if(results != null){
			for(ScanResult result : results){
				if(result.SSID == null || result.SSID.length() == 0 || result.capabilities.contains("[IBSS]"))
					continue;
				AccessPoint accessPoint = new AccessPoint(this);
				int c = mWifiManager.calculateSignalLevel(result.level, 10);
				accessPoint.setText(result.SSID);
				accessPoint.setIcon(R.drawable.ic_wifi_lock_signal_1);
				accessPoints.add(accessPoint);
			}
		}
		Collections.sort(accessPoints);
		return accessPoints;
	}
	
}
