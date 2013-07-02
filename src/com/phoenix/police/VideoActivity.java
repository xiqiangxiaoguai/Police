package com.phoenix.police;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.Activity;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.ShutterCallback;
import android.media.AudioManager;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.media.ToneGenerator;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.StatFs;
import android.view.Gravity;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;

public class VideoActivity extends Activity {
	/** Called when the activity is first created. */
	CameraSurfaceView mySurface;
	private ToneGenerator tone;
	private boolean cameraBusy = false;
	private int cFlashMode = CameraSurfaceView.FLASH_MODE_OFF;
	private ImageView lastView;
	private String videoPath = "/sdcard/police/video/";
	public MediaRecorder mrec;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.camera_activity);
		mySurface = new CameraSurfaceView(this);
		RelativeLayout cameraLayout = ( RelativeLayout) findViewById(R.id.camera);
		cameraLayout.setGravity(Gravity.CENTER);
		mySurface.setLayoutParams(new LayoutParams(360, 600));
		cameraLayout.addView(mySurface);
		ImageButton button = (ImageButton) findViewById(R.id.qiezi);
		button.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if(cameraBusy == false){
					cameraBusy = true;
					try {
		                startRecording();
		            } catch (Exception e) {
		                mrec.release();
		            }
				}else{
					cameraBusy = false;
					mrec.stop();
		            mrec.release();
		            mrec = null;
				}
			}
		});
		final ImageButton flashBtn = (ImageButton) findViewById(R.id.flashmode);
		flashBtn.setBackgroundResource(R.drawable.ic_flash_off_holo_light);
		flashBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				switch (cFlashMode) {
				case CameraSurfaceView.FLASH_MODE_TORCH:
					cFlashMode = CameraSurfaceView.FLASH_MODE_OFF;
					flashBtn.setBackground(getResources().getDrawable(R.drawable.ic_flash_off_holo_light));
					mySurface.setFlashMode(cFlashMode);
					break;
				case CameraSurfaceView.FLASH_MODE_OFF:
					cFlashMode = CameraSurfaceView.FLASH_MODE_TORCH;
					flashBtn.setBackground(getResources().getDrawable(R.drawable.ic_flash_on_holo_light));
					mySurface.setFlashMode(cFlashMode);
					break;
				default:
					break;
				}
			}
		});
	}
	
	private void startRecording() throws IOException 
    {
		mrec = new MediaRecorder();
		File folderFile = new File(videoPath);
		if(!folderFile.exists()){
			folderFile.mkdirs();
		}
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String path = videoPath+format.format(new Date())+".3gp";
		
		Camera mCamera = mySurface.getCamera();
		SurfaceHolder surfaceHolder = mySurface.getHolder();
        mrec = new MediaRecorder();  // Works well
        mCamera.unlock();
        mrec.setCamera(mCamera);
        mrec.setPreviewDisplay(surfaceHolder.getSurface());
        mrec.setVideoSource(MediaRecorder.VideoSource.CAMERA);
        mrec.setAudioSource(MediaRecorder.AudioSource.MIC); 

        mrec.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH));
        mrec.setPreviewDisplay(surfaceHolder.getSurface());
        mrec.setOutputFile(path); 

        mrec.prepare();
        mrec.start();
    }

}