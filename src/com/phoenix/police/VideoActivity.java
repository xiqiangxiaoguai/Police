package com.phoenix.police;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.phoenix.data.Constants;

import android.app.Activity;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.media.ThumbnailUtils;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.provider.MediaStore.Video.Thumbnails;
import android.view.Gravity;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;

public class VideoActivity extends Activity {
	/** Called when the activity is first created. */
	CameraSurfaceView mySurface;
	private boolean cameraBusy = false;
	private int cFlashMode = CameraSurfaceView.FLASH_MODE_OFF;
	public MediaRecorder mrec;
	private String cPath = null;

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
		            
		            File myCaptureFile = new File( Constants.VIDEO_THUMBNAIL_PATH + cPath.substring(cPath.lastIndexOf('/'),cPath.lastIndexOf('.')) + ".jpg");
					BufferedOutputStream bos;
					try {
						bos = new BufferedOutputStream(new FileOutputStream(
								myCaptureFile));
						ThumbnailUtils.createVideoThumbnail(cPath,
								Thumbnails.MINI_KIND).compress(
								Bitmap.CompressFormat.JPEG, 80, bos);
						try {
							bos.flush();
							bos.close();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					} catch (FileNotFoundException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
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
		File folderFile = new File(Constants.VIDEO_PATH);
		if(!folderFile.exists()){
			folderFile.mkdirs();
		}
		File thumbnailFile = new File(Constants.VIDEO_THUMBNAIL_PATH);
		if(!thumbnailFile.exists()){
			thumbnailFile.mkdirs();
		}
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss");
		cPath = Constants.VIDEO_PATH + format.format(new Date())+".3gp";
		
		Camera mCamera = mySurface.getCamera();
		SurfaceHolder surfaceHolder = mySurface.getHolder();
        mrec = new MediaRecorder();  // Works well
        mCamera.unlock();
        mrec.setOrientationHint(90);
        mrec.setCamera(mCamera);
        mrec.setPreviewDisplay(surfaceHolder.getSurface());
        mrec.setVideoSource(MediaRecorder.VideoSource.CAMERA);
        mrec.setAudioSource(MediaRecorder.AudioSource.MIC); 
        mrec.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH));
        mrec.setPreviewDisplay(surfaceHolder.getSurface());
        mrec.setOutputFile(cPath); 

        mrec.prepare();
        mrec.start();
    }

}