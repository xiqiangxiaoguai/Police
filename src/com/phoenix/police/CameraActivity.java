package com.phoenix.police;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.zip.DataFormatException;

import com.phoenix.data.Constants;

import android.app.Activity;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.ShutterCallback;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.StatFs;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

public class CameraActivity extends Activity {
	/** Called when the activity is first created. */
	private static final String LOG_TAG = CameraActivity.class.getSimpleName();
	private static final boolean LOG_SWITCH = Constants.LOG_SWITCH;
	
	CameraSurfaceView mySurface;
	private ToneGenerator tone;
	private boolean cameraBusy = false;
	private int cFlashMode = CameraSurfaceView.FLASH_MODE_AUTO;
	private String cameraPath = "/sdcard/police/camera/";
	
	private AutoFocusCallback autoFocus = new AutoFocusCallback() {
		
		@Override
		public void onAutoFocus(boolean success, Camera camera) {
			camera.takePicture(shutterCallback, null, jpegCallback);
		}
	};
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.camera_activity);
		mySurface = new CameraSurfaceView(this);
		RelativeLayout cameraLayout = ( RelativeLayout) findViewById(R.id.camera);
		cameraLayout.setGravity(Gravity.CENTER);
		mySurface.setLayoutParams(new LayoutParams(480, 640));
		cameraLayout.addView(mySurface);
		ImageButton button = (ImageButton) findViewById(R.id.qiezi);
		button.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if(cameraBusy == false){
					cameraBusy = true;
					Camera camera = mySurface.getCamera();
					camera.autoFocus(autoFocus);
				}
			}
		});
		final ImageButton flashBtn = (ImageButton) findViewById(R.id.flashmode);
		flashBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				switch (cFlashMode) {
				case CameraSurfaceView.FLASH_MODE_AUTO:
					cFlashMode = CameraSurfaceView.FLASH_MODE_ON;
					flashBtn.setBackground(getResources().getDrawable(R.drawable.ic_flash_on_holo_light));
					mySurface.setFlashMode(cFlashMode);
					break;
				case CameraSurfaceView.FLASH_MODE_ON:
					cFlashMode = CameraSurfaceView.FLASH_MODE_OFF;
					flashBtn.setBackground(getResources().getDrawable(R.drawable.ic_flash_off_holo_light));
					mySurface.setFlashMode(cFlashMode);
					break;
				case CameraSurfaceView.FLASH_MODE_OFF:
					cFlashMode = CameraSurfaceView.FLASH_MODE_AUTO;
					flashBtn.setBackground(getResources().getDrawable(R.drawable.ic_flash_auto_holo_light));
					mySurface.setFlashMode(cFlashMode);
					break;
				default:
					break;
				}
			}
		});
//		lastView = (ImageView) findViewById(R.id.lastphoto);
//		updateLastPhoto(filePath);
	}
	//返回照片的JPEG格式的数据
	private PictureCallback jpegCallback = new PictureCallback(){
		public void onPictureTaken(byte[] data, Camera camera) {
//			Parameters ps = camera.getParameters();
//			if(ps.getPictureFormat() == PixelFormat.JPEG){
//			    //存储拍照获得的图片
			    String path = save(data);
//			    //将图片交给Image程序处理
//			    Uri uri = Uri.fromFile(new File(path));
//   			    Intent intent = new Intent();
//   			    intent.setAction("android.intent.action.VIEW");
//   			    intent.setDataAndType(uri, "image/jpeg");
//   			    startActivity(intent);
//			}
//			lastView.setImageURI(Uri.fromFile(new File(path)));
			mySurface.resumePreview();
			cameraBusy = false;
		}
	};
	
	//When the shutter is pressed ,onShutter() called.
	private ShutterCallback shutterCallback = new ShutterCallback(){
		public void onShutter() {
			if(tone == null)
				//发出提示用户的声音
				tone = new ToneGenerator(AudioManager.STREAM_MUSIC,
						ToneGenerator.MAX_VOLUME);
			tone.startTone(ToneGenerator.TONE_PROP_BEEP2);
		}
	};
	
	private String save(byte[] data){
		if (LOG_SWITCH)
			Log.d(LOG_TAG, "Start to save the bitmap.");
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		File floderPath = new File(cameraPath);
		if(!floderPath.exists()){
			floderPath.mkdirs();
		}
		String path = cameraPath + format.format(new Date())+".jpg";
		try {
			//if there is a sdcard
			if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
				if (LOG_SWITCH)
					Log.d(LOG_TAG, "There is a sdcard.");
				//if there is enough storage in the sdcard
				String storage = Environment.getExternalStorageDirectory().toString();
				StatFs fs = new StatFs(storage);
				long available = Math.abs(fs.getAvailableBlocks()*fs.getBlockSize());
				if(available<data.length){
					if (LOG_SWITCH)
						Log.d(LOG_TAG, "The available storage is not enough. Available:" + available + "( " + data.length + " required)");
					return null;
				}
				
				File file = new File(path);
				if(!file.exists())
					file.createNewFile();
				FileOutputStream fos = new FileOutputStream(file);
				fos.write(data);
				fos.close();
				if (LOG_SWITCH)
					Log.d(LOG_TAG, "Image captured successfully!");
			}else{
				Toast.makeText(this, R.string.storage_no_enough, 500).show();
				if (LOG_SWITCH)
					Log.d(LOG_TAG, "Image captured failed.Cause:Storage not enough.");
			}
		} catch (Exception e) {
			e.printStackTrace();
			if (LOG_SWITCH)
				Log.d(LOG_TAG, "Image capture failed.Cause:" + e);
			return null;
		}
		return path;
	}

	
//	private void updateLastPhoto(String filePath) {
//		long lastModified = 0;
//		String lastPath = null ;
//		File root = new File(filePath);
//		File[] files = root.listFiles();
//		for (File file : files) {
//			if(file.lastModified() > lastModified){
//				lastModified = file.lastModified();
//				lastPath = file.getAbsolutePath();
//			}
//		}
//		if(lastPath != null){
//			lastView.setImageURI(Uri.fromFile(new File(lastPath)));
//		}
//	}
}
