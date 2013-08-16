package com.phoenix.setting;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.StatFs;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.preference.SwitchPreference;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import com.phoenix.data.Constants;
import com.phoenix.police.R;

public class SettingActivity extends PreferenceActivity implements Preference.OnPreferenceChangeListener, Preference.OnPreferenceClickListener{
	private static final boolean LOG_SWITCH = Constants.LOG_SWITCH;
	private static final String LOG_TAG = SettingActivity.class.getSimpleName();
	
	private WifiManager mWifiManager;
	
	PreferenceScreen wifiScreen;
	PreferenceScreen storageScreen;
	PreferenceScreen aboutScreen;
	
	ConnectivityManager mConnect ;
	
	Handler mHandler;
	
	static final int SECURITY_NONE = 0;
	static final int SECURITY_WEP = 1;
	static final int SECURITY_WPA = 2;
	
	ArrayList<AccessPoint> accessPoints = new ArrayList<AccessPoint>();
	HashMap<String, ScanResult> mResults = new HashMap<String, ScanResult>();
	
	@Override
	public boolean onPreferenceClick(Preference preference) {
		if(preference.getClass().getName() == AccessPoint.class.getName()){
			final ScanResult result = mResults.get(preference.getTitle());
			LayoutInflater factory = LayoutInflater.from(this);
			View view = factory.inflate(R.layout.pw_edit, null);
			((TextView)view.findViewById(R.id.leveldetail)).setText("" + result.level);
			((TextView)view.findViewById(R.id.securitydetail)).setText(result.capabilities);
			final EditText edit =  (EditText) view.findViewById(R.id.passworddetail);
			AlertDialog dialog = new AlertDialog.Builder(this).setTitle(result.SSID).setView(view)
					.setPositiveButton(R.string.connect, new OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							String pw = edit.getText().toString();
							addNetwork(CreateWifiInfo(result.SSID, pw , getSecurity(result)));
						}
					})
					.setNegativeButton(R.string.cancel, new OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
						}
					}).create();
			dialog.show();
		}
		return true;
	}
	

	//************************** Join network **************************************
    public void addNetwork(WifiConfiguration wcg) { 
		 int wcgID = mWifiManager.addNetwork(wcg); 
	     boolean b =  mWifiManager.enableNetwork(wcgID, true); 
	     Log.d(LOG_TAG, "add Network returned " + wcgID );
	     Log.d(LOG_TAG, "enableNetwork returned " + b );  
    }
    
	public WifiConfiguration CreateWifiInfo(String SSID, String Password, int Type) 
    { 
          WifiConfiguration config = new WifiConfiguration();   
           config.allowedAuthAlgorithms.clear(); 
           config.allowedGroupCiphers.clear(); 
           config.allowedKeyManagement.clear(); 
           config.allowedPairwiseCiphers.clear(); 
           config.allowedProtocols.clear(); 
           config.SSID = "\"" + SSID + "\"";   
          
          WifiConfiguration tempConfig = this.IsExsits(SSID);  
          
          if(tempConfig != null) {  
        	  mWifiManager.removeNetwork(tempConfig.networkId); 
          }
          
          if(Type == SECURITY_NONE) //WIFICIPHER_NOPASS
          { 
               config.wepKeys[0] = ""; 
               config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE); 
               config.wepTxKeyIndex = 0; 
          } 
          if(Type == SECURITY_WEP) //WIFICIPHER_WEP
          { 
              config.hiddenSSID = true;
              config.wepKeys[0]= "\""+Password+"\""; 
              config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.SHARED); 
              config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP); 
              config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP); 
              config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40); 
              config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104); 
              config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE); 
              config.wepTxKeyIndex = 0; 
          } 
          if(Type == SECURITY_WPA) //WIFICIPHER_WPA
          { 
          config.preSharedKey = "\""+Password+"\""; 
          config.hiddenSSID = true;   
          config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);   
          config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);                         
          config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);                         
          config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);                    
          //config.allowedProtocols.set(WifiConfiguration.Protocol.WPA);  
          config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
          config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
          config.status = WifiConfiguration.Status.ENABLED;   
          }
           return config; 
    } 
	
	private WifiConfiguration IsExsits(String SSID)  
    {  
        List<WifiConfiguration> existingConfigs = mWifiManager.getConfiguredNetworks();  
           for (WifiConfiguration existingConfig : existingConfigs)   
           {  
             if (existingConfig.SSID.equals("\""+SSID+"\""))  
             {  
                 return existingConfig;  
             }  
           }  
        return null;   
    }
	
	//************************** Join network **************************************
	@Override
	public boolean onPreferenceChange(Preference preference, Object newValue) {
		if(preference.getKey().equals("setting_wifi_switch_preference")){
			if(((SwitchPreference)preference).isChecked()){
				if(mWifiManager.isWifiEnabled()){
					mWifiManager.setWifiEnabled(false);
				}
			}else{
				if(!mWifiManager.isWifiEnabled()){
					mWifiManager.setWifiEnabled(true);
				}
			}
		}
		if(preference.getKey().equals("setting_3g_switch_preference")){
			setMobileDataEnabled(this, ((SwitchPreference)preference).isChecked());
		}
		return true;
	}
	
	//***********************************Runnable for get storage detail*****************************************
	Runnable scanStorageRun = new Runnable() {
		@Override
		public void run() {
			storageScreen.removeAll();
			if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
				File path = Environment.getExternalStorageDirectory();
				Preference storPreference = new Preference(SettingActivity.this);
				storPreference.setTitle(R.string.storage_describe);
				storPreference.setSummary(getAvailaStor(path.getPath()) + "/" + getTotalStor(path.getPath()));
				storageScreen.addPreference(storPreference);
				
				storPreference = new Preference(SettingActivity.this);
				storPreference.setTitle(R.string.camera_file);
				storPreference.setSummary(getFolderStor(Constants.CAMERA_PATH));
				storageScreen.addPreference(storPreference);
				
				storPreference = new Preference(SettingActivity.this);
				storPreference.setTitle(R.string.video_file);
				storPreference.setSummary(getFolderStor(Constants.VIDEO_PATH));
				storageScreen.addPreference(storPreference);
				
				storPreference = new Preference(SettingActivity.this);
				storPreference.setTitle(R.string.audio_file);
				storPreference.setSummary(getFolderStor(Constants.AUDIO_PATH));
				storageScreen.addPreference(storPreference);
			}
		}
	};

	private String getTotalStor(String path){
		StatFs statfs = new StatFs(path);
		long blockSize = statfs.getBlockSize();
		long availaBlock = statfs.getBlockCount(); 
		return new DecimalFormat("0.00").format(blockSize* availaBlock/1024/1024d/1024d) + "G";
		
	}
	
	private String getAvailaStor(String path){
		StatFs statfs = new StatFs(path);
		long blockSize = statfs.getBlockSize();
		long totalBlocks = statfs.getAvailableBlocks(); 
		return new DecimalFormat("0.00").format(blockSize* totalBlocks/1024/1024d/1024d) + "G";
	}
	
	private long getFolderStor(File path){
		long size = 0;
		File f = path;
		File flist[] = f.listFiles();
		for (int i = 0; i < flist.length; i++) {
			if (flist[i].isDirectory()) {
				size = size + getFolderStor(flist[i]);
			} else {
				size = size + flist[i].length();
			}
		}
		return size;
	}
	
	private String getFolderStor(String path){
		return new DecimalFormat("0.00").format(getFolderStor(new File(path))/1024/1024d/1024d) + "G";
	}
	
	//***********************************Runnable for get storage detail*****************************************
	//***********************************Runnable for about*********************************************
	Runnable aboutRun = new Runnable() {
		@Override
		public void run() {
			Preference aboutPreference = new Preference(SettingActivity.this);
			aboutPreference.setTitle(R.string.device_name);
			aboutPreference.setSummary(Constants.DEVICE_NAME);
			aboutScreen.addPreference(aboutPreference);
			
			aboutPreference = new Preference(SettingActivity.this);
			aboutPreference.setTitle(R.string.android_version);
			aboutPreference.setSummary(Constants.ANDROID_VERSION);
			aboutScreen.addPreference(aboutPreference);
			
			aboutPreference = new Preference(SettingActivity.this);
			aboutPreference.setTitle(R.string.version);
			aboutPreference.setSummary(Constants.VERSION);
			aboutScreen.addPreference(aboutPreference);
		}
	};
	//***********************************Runnable for about*********************************************
	//*****************************Runnable for scan network***************************
	Runnable scanWifiRun = new Runnable() {
		@Override
		public void run() {
			updateAccessPoints();
			mHandler.postDelayed(scanWifiRun, 10000);
		}
	};
	//*****************************Runnable for scan network***************************
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);
		wifiScreen = (PreferenceScreen) findPreference("setting_wifi_preference");
		storageScreen = (PreferenceScreen) findPreference("setting_storage_preference");
		aboutScreen = (PreferenceScreen) findPreference("setting_about_preference");
		mWifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		HandlerThread hThread = new HandlerThread(SettingActivity.class.getSimpleName());
		hThread.start();
		mHandler = new Handler(hThread.getLooper());
		mHandler.post(scanStorageRun);
		mHandler.post(aboutRun);
		mHandler.post(scanWifiRun);
		
		SwitchPreference wifiSwitch = (SwitchPreference) findPreference("setting_wifi_switch_preference");
		wifiSwitch.setChecked(mWifiManager.isWifiEnabled());
		wifiSwitch.setOnPreferenceChangeListener(this);
		
		mConnect = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		SwitchPreference _3gSwitch = (SwitchPreference) findPreference("setting_3g_switch_preference");
		_3gSwitch.setOnPreferenceChangeListener(this);
	}
	
	@Override
	protected void onStop() {
		super.onStop();
		mHandler.removeCallbacks(aboutRun);
		mHandler.removeCallbacks(scanWifiRun);
		mHandler.removeCallbacks(scanStorageRun);
	}
	
	private void setMobileDataEnabled(Context context, boolean enabled) {
	    try {
	        final ConnectivityManager conman = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
	        final Class conmanClass = Class.forName(conman.getClass().getName());
	        final Field iConnectivityManagerField = conmanClass.getDeclaredField("mService");
	        iConnectivityManagerField.setAccessible(true);
	        final Object iConnectivityManager = iConnectivityManagerField.get(conman);
	        final Class iConnectivityManagerClass = Class.forName(iConnectivityManager.getClass().getName());
	        final Method setMobileDataEnabledMethod = iConnectivityManagerClass.getDeclaredMethod("setMobileDataEnabled", Boolean.TYPE);
	        setMobileDataEnabledMethod.setAccessible(true);
	        setMobileDataEnabledMethod.invoke(iConnectivityManager, enabled);
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
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
		accessPoints.clear();
		mResults.clear();
		mWifiManager.startScan();
		final String curSSID = mWifiManager.getConnectionInfo().getSSID();
		List<ScanResult> results = mWifiManager.getScanResults();
		Collections.sort(results, new Comparator<ScanResult>() {
			@Override
			public int compare(ScanResult a, ScanResult b) {
				if(a.SSID.equals(curSSID)){
					return 1;
				}
				if(b.SSID.equals(curSSID)){
					return -1;
				}
//				if(a.level > b.level){
//					return 1;
//				}else{
//					return -1;
//				}
				return 1;
			}
		});
		if(results != null){
			for(ScanResult result : results){
				if(result.SSID == null || result.SSID.length() == 0 || result.capabilities.contains("[IBSS]"))
					continue;
				AccessPoint accessPoint = new AccessPoint(this);
				int c = mWifiManager.calculateSignalLevel(result.level, 10);
				accessPoint.setTitle(result.SSID);
				accessPoint.setIcon(R.drawable.ic_wifi_lock_signal_1);
				if(("\"" + result.SSID + "\"").equals(curSSID)){
					accessPoint.setSummary(R.string.connected);
				}
				accessPoint.setOnPreferenceClickListener(this);
				mResults.put(result.SSID, result);
				accessPoints.add(accessPoint);
			}
		}
		return accessPoints;
	} 
	
	private static int getSecurity(ScanResult result) {
        if (result.capabilities.contains("WEP")) {
            return SECURITY_WEP;
        } else if (result.capabilities.contains("PSK")) {
            return SECURITY_WPA;
        } 
        return SECURITY_NONE;
    }
}
