package com.phoenix.police;

import java.io.File;
import java.util.ArrayList;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AbsListView.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.assist.SimpleImageLoadingListener;
import com.phoenix.data.Constants;

public class CameraFragment extends Fragment implements OnItemClickListener{

	private static final boolean LOG_SWITCH = Constants.LOG_SWITCH;
	private static final String LOG_TAG = CameraFragment.class.getSimpleName();
	
	ArrayList<String> imageNames = new ArrayList<String>();
	ArrayList<String> imageUrls = new ArrayList<String>();
	
	private Handler mHandler;
	private ImageLoader imageloader;
	private Handler handler = new Handler(){
		public void handleMessage(Message msg) {
			GridView grid = (GridView) getView().findViewById(R.id.images);
			grid.setAdapter(new ImageAdapter(getActivity()));
			grid.setOnItemClickListener(CameraFragment.this);
		};
	};
	DisplayImageOptions options =  new DisplayImageOptions.Builder()
	.resetViewBeforeLoading()
	.showImageForEmptyUri(R.drawable.image_loading)
	.showStubImage(R.drawable.image_loading)
	.imageScaleType(ImageScaleType.EXACTLY_STRETCHED)
    .cacheInMemory().cacheOnDisc().build(); 
	
	Runnable run = new Runnable() {
		@Override
		public void run() {
			getImages();
			handler.sendEmptyMessage(0);
		}
		
	};
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		HandlerThread hThread = new HandlerThread(CameraFragment.class.getSimpleName());
		hThread.start();
		mHandler = new Handler(hThread.getLooper()){
		};
		imageloader = ImageLoader.getInstance();
		mHandler.post(run);
	}
	
	private void getImages(){
		File[] files = new File(Constants.CAMERA_PATH).listFiles();
		for(int i=0; i <files.length; i++){
			imageNames.add(files[i].getName());
			imageUrls.add(files[i].getAbsolutePath());
		}
	}
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
//		getImages();
		View view = inflater.inflate(R.layout.camera_fragment, container,false);
		
		return view;
	}
	@Override
	public void onDestroy() {
		super.onDestroy();
	}
	
	@Override
	public void onStop() {
		super.onStop();
		mHandler.removeCallbacks(run);
	}
	
	class ImageAdapter extends BaseAdapter{

		private Context mContext;
		public ImageAdapter(Context context) {
			mContext = context;
		}
		@Override
		public int getCount() {
			return imageNames.size();
		}

		@Override
		public Object getItem(int arg0) {
			return null;
		}

		@Override
		public long getItemId(int arg0) {
			return arg0;
		}
		@Override
		public View getView(int arg0, View convertView, ViewGroup arg2) {
			final ImageView imageView;
			if(convertView == null){
				imageView = new ImageView(mContext);
			}else{
				imageView = (ImageView)convertView;
			}
			imageView.setAdjustViewBounds(true);
			imageView.setMaxHeight(LayoutParams.MATCH_PARENT);
			imageView.setMaxWidth(LayoutParams.MATCH_PARENT);
			LayoutParams p = new LayoutParams(LayoutParams.MATCH_PARENT, 300);
			imageView.setLayoutParams(p);
				imageloader.displayImage("file://" + imageUrls.get(arg0), imageView,options,new SimpleImageLoadingListener()  
	            {  
					@Override
					public void onLoadingComplete(String imageUri, View view,
							Bitmap loadedImage) {
								Animation anim = AnimationUtils.loadAnimation(
										mContext, android.R.anim.fade_in);
								imageView.setAnimation(anim);
								anim.start();
					}
	            });
			return imageView;
		}
		
	}

	@Override
	public void onItemClick(AdapterView<?> adapter, View view, int pos, long id) {
		Intent intent = new Intent("com.phoenix.police.CameraBrowseActivity");
		Bundle bundle = new Bundle();
		bundle.putStringArrayList("cameraPaths", imageUrls);
		bundle.putInt("currentPic", pos);
		intent.putExtras(bundle);
		startActivity(intent);
	}
}
