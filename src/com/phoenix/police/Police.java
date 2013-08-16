package com.phoenix.police;

import java.io.File;
import java.lang.reflect.Method;

import com.phoenix.data.Account;
import com.phoenix.data.Constants;

import android.os.Bundle;
import android.provider.Settings;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;

public class Police extends Activity implements View.OnClickListener{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_police);
        getContentResolver().query(Account.CONTENT_URI, null, null, null, null);
        setupButtonListener();
        
        for(String str : new String[]{Constants.CAMERA_PATH, Constants.AUDIO_PATH, Constants.VIDEO_PATH})
        {
        	File floderPath = new File(str);
        	if(!floderPath.exists()){
        		floderPath.mkdirs();
        	}
        }
        
    }
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
    	// TODO Auto-generated method stub  
        System.out.println("hasfocus--->>>" + hasFocus);  
        super.onWindowFocusChanged(hasFocus);  
        try  
        {  
            Object service = getSystemService("statusbar");  
            Class<?> statusbarManager =  
                    Class.forName("android.app.StatusBarManager");  
            Method test = statusbarManager.getMethod("collapse");  
            test.invoke(service);  
        }  
        catch (Exception ex)  
        {  
            ex.printStackTrace();  
        }  
    }
    
    private void setupButtonListener(){
    	ImageView startCamera = (ImageView) findViewById(R.id.camera);
    	startCamera.setOnClickListener(this);
    	ImageView startVideo = (ImageView) findViewById(R.id.video);
    	startVideo.setOnClickListener(this);
    	ImageView startRecord = (ImageView) findViewById(R.id.record);
    	startRecord.setOnClickListener(this);
    	ImageView startFiles = (ImageView) findViewById(R.id.files);
    	startFiles.setOnClickListener(this);
    	ImageView startSetting = (ImageView) findViewById(R.id.setting);
    	startSetting.setOnClickListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.police, menu);
        return true;
    }
	@Override
	public void onClick(View v) {
		switch(v.getId()){
		case R.id.camera:
			startActivity(new Intent("com.phoenix.police.CameraActivity"));
			break;
		case R.id.video:
			startActivity(new Intent("com.phoenix.police.VideoActivity"));
			break;
		case R.id.record:
			startActivity(new Intent("com.phoenix.police.AudioActivity"));
			break;
		case R.id.files:
			startActivity(new Intent("com.phoenix.police.FilesActivity"));
			break;
		case R.id.setting:
			startActivity(new Intent("com.phoenix.setting.SettingActivity"));
			break;
	}
	}
}
