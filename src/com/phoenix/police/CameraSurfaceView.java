package com.phoenix.police;

import java.io.IOException;
import java.util.List;

import com.phoenix.data.Constants;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.Size;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class CameraSurfaceView extends SurfaceView implements SurfaceHolder.Callback {
	
	private static final String LOG_TAG = CameraSurfaceView.class.getSimpleName();
	private static final boolean LOG_SWITCH = Constants.LOG_SWITCH;
	
	public final static int FLASH_MODE_AUTO = 0;
	public final static int FLASH_MODE_ON = 1;
	public final static int FLASH_MODE_OFF = 2;
	public final static int FLASH_MODE_TORCH = 3;
	
	
	SurfaceHolder holder;
	Camera myCamera;
	boolean bIfPreview = false;
	int mPreviewHeight = 0;
	int mPreviewWidth = 0;
	
	public CameraSurfaceView(Context context) {
		super(context);
		init(context);
	}
	public CameraSurfaceView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}
	public CameraSurfaceView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}

	private void init(Context context){
		holder = getHolder();// ���surfaceHolder����
		holder.addCallback(this); 
//		holder.setFixedSize(176, 144);
		holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);// set display device typeb
	}
	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		new Thread(new thread()).start();
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		mPreviewHeight = height;
		mPreviewWidth = width;
		Log.d("qiqi", "height:" + height + " width:" + width);

	}
	
	private class thread implements Runnable{

		@Override
		public void run() {
			if (myCamera == null) {
				myCamera = Camera.open();// �������,���ܷ��ڹ��캯���У���Ȼ������ʾ����.
				if(null == myCamera){
					if (LOG_SWITCH)
						Log.d(LOG_TAG, "ERROR: Camera == null!");
				}
				try {
					myCamera.setPreviewDisplay(holder);//set the surface to used for live preview
				} catch (IOException e) {
					e.printStackTrace();
					if(null != myCamera){
						myCamera.release();
						myCamera = null;
					}
				}
			}
			initCamera();
		}
		
	}
	
	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		if(null != myCamera){
			myCamera.setPreviewCallback(null);
			myCamera.stopPreview();
			bIfPreview = false;
			myCamera.release();
			myCamera = null;
		}
	}
	private void initCamera(){
		if(bIfPreview){
			myCamera.stopPreview();
		}
		if(null != myCamera){
			try {
				Camera.Parameters parameters = myCamera.getParameters();
				// parameters.setFlashMode("off");
				parameters.setPictureFormat(PixelFormat.JPEG);// Sets the image format for picture �趨��Ƭ��ʽΪJPEG��Ĭ��ΪNV21
				parameters.setPreviewFormat(PixelFormat.YCbCr_420_SP);// Sets the image format for preview picture��Ĭ��ΪNV21
				/*
				 * ��ImageFormat��JPEG/NV16(YCrCb format��used for
				 * Video)/NV21(YCrCb format��used for Image)/RGB_565/YUY2/YU12
				 */

				// �����ԡ���ȡcaera֧�ֵ�PictrueSize�������ܷ����ã���
				List<Size> pictureSizes = myCamera.getParameters()
						.getSupportedPictureSizes();
				List<Size> previewSizes = myCamera.getParameters()
						.getSupportedPreviewSizes();
				List<Integer> previewFormats = myCamera.getParameters()
						.getSupportedPreviewFormats();
				List<Integer> previewFrameRates = myCamera.getParameters()
						.getSupportedPreviewFrameRates();
				Log.i(LOG_TAG + "initCamera", "cyy support parameters is ");
				Size psize = null;
				for (int i = 0; i < pictureSizes.size(); i++) {
					psize = pictureSizes.get(i);
					Log.i(LOG_TAG + "initCamera", "PictrueSize,width: "
							+ psize.width + " height" + psize.height);
				}
				for (int i = 0; i < previewSizes.size(); i++) {
					psize = previewSizes.get(i);
					Log.i(LOG_TAG + "initCamera", "PreviewSize,width: "
							+ psize.width + " height" + psize.height);
				}
				Integer pf = null;
				for (int i = 0; i < previewFormats.size(); i++) {
					pf = previewFormats.get(i);
					Log.i(LOG_TAG + "initCamera", "previewformates:" + pf);
				}
				//Set camera flash mode.
				parameters.setFlashMode(Camera.Parameters.FLASH_MODE_AUTO);
				//Set zoom 
				if(parameters.isSmoothZoomSupported()){
					Log.d("qiqi", "" + parameters.getMaxZoom());
					
				}
				// �������պ�Ԥ��ͼƬ��С
				parameters.setPictureSize(2048, 1536); // ָ������ͼƬ�Ĵ�С
				parameters.setPreviewSize(640, 480); // ָ��preview�Ĵ�С
				// ���������� ����������������õĺ���ʵ�ֻ��Ĳ�һ��ʱ���ͻᱨ��

				// ��������ͷ�Զ�����
				if (this.getResources().getConfiguration().orientation != Configuration.ORIENTATION_LANDSCAPE) {
					parameters.set("orientation", "portrait"); //
					parameters.set("rotation", 90); // ��ͷ�Ƕ�ת90�ȣ�Ĭ������ͷ�Ǻ��ģ�
					myCamera.setDisplayOrientation(90); // ��2.2���Ͽ���ʹ��
				} else// ����Ǻ���
				{
					parameters.set("orientation", "landscape"); //
					myCamera.setDisplayOrientation(0); // ��2.2���Ͽ���ʹ��
				}

				/* ��Ƶ�����봦�� */
				// ��Ӷ���Ƶ��������

				// �趨���ò���������Ԥ��
				myCamera.setParameters(parameters); // ��Camera.Parameters�趨��Camera
				myCamera.startPreview(); // ��Ԥ������
				bIfPreview = true;

				// �����ԡ����ú��ͼƬ��С��Ԥ����С�Լ�֡��
				Camera.Size csize = myCamera.getParameters().getPreviewSize();
				mPreviewHeight = csize.height; //
				mPreviewWidth = csize.width;
				Log.i(LOG_TAG + "initCamera", "after setting, previewSize:width: "
						+ csize.width + " height: " + csize.height);
				csize = myCamera.getParameters().getPictureSize();
				Log.i(LOG_TAG + "initCamera", "after setting, pictruesize:width: "
						+ csize.width + " height: " + csize.height);
				Log.i(LOG_TAG + "initCamera", "after setting, previewformate is "
						+ myCamera.getParameters().getPreviewFormat());
				Log.i(LOG_TAG + "initCamera", "after setting, previewframetate is "
						+ myCamera.getParameters().getPreviewFrameRate());
			} catch (Exception e)
			    { 
			     e.printStackTrace();
			    }
		}
	}
	public Camera getCamera(){
		return myCamera;
	}
	public void resumePreview(){
		myCamera.startPreview();
	}
	public void setFlashMode(int flashMode){
		Parameters parameters = myCamera.getParameters();
		switch(flashMode){
		case FLASH_MODE_AUTO:
			parameters.setFlashMode(Camera.Parameters.FLASH_MODE_AUTO);
			break;
		case FLASH_MODE_ON:
			parameters.setFlashMode(Camera.Parameters.FLASH_MODE_ON);
			break;
		case FLASH_MODE_OFF:
			parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
			break;
		case FLASH_MODE_TORCH:
			parameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
		}
		myCamera.setParameters(parameters);
	}
}