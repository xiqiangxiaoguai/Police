package com.phoenix.police;

import java.util.ArrayList;
import java.util.zip.Inflater;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView.LayoutParams;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageLoadingListener;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.phoenix.data.Constants;

public class CameraBrowseActivity extends Activity{

	private static final String LOG_TAG = CameraBrowseActivity.class.getSimpleName();
	private static final boolean LOG_SWITCH = Constants.LOG_SWITCH;
	ArrayList<String> imageUrls ;
	DisplayImageOptions options =  new DisplayImageOptions.Builder()
//	.resetViewBeforeLoading()
	.showImageForEmptyUri(R.drawable.image_loading)
	.showStubImage(R.drawable.image_loading)
	.imageScaleType(ImageScaleType.IN_SAMPLE_POWER_OF_2)
    .cacheInMemory().cacheOnDisc().build(); 
	
	ImageLoader imageloader;
	int curPic;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		imageUrls = getIntent().getExtras().getStringArrayList("cameraPaths");
		curPic = getIntent().getExtras().getInt("currentPic");
		setContentView(R.layout.camera_browse);
		ViewPager imagePager = (ViewPager) findViewById(R.id.imagePager);
		imageloader = ImageLoader.getInstance();
		imagePager.setAdapter(new ImageAdapter(this, imageUrls));
		imagePager.setCurrentItem(curPic);
	}
	class ImageAdapter extends PagerAdapter{
		Context mContext;
		ArrayList<String> mImageUrls;
		LayoutInflater inflater;
		public ImageAdapter(Context context, ArrayList<String> imageUrls) {
			mContext = context;
			mImageUrls = imageUrls;
			inflater = getLayoutInflater();
		}
		@Override
		public int getCount() {
			return mImageUrls.size();
		}
		@Override
		public boolean isViewFromObject(View arg0, Object arg1) {
			return arg0 == (arg1);
		}
		@Override
		public void destroyItem(View container, int position, Object object) {
			((ViewPager) container).removeView((View)object);
		}
		@Override
		public Object instantiateItem(View view, int position) {
			if (LOG_SWITCH)
				Log.d(LOG_TAG, "instantiate item:" + position);
			RelativeLayout relative = (RelativeLayout) inflater.inflate(R.layout.browse_item, null);
			ImageView imageView = (ImageView) relative.findViewById(R.id.image);
 			imageloader.displayImage("file:/" + mImageUrls.get(position), imageView , new ImageLoadingListener() {
				
				@Override
				public void onLoadingStarted(String arg0, View arg1) {
					if (LOG_SWITCH)
						Log.d(LOG_TAG, "start");
				}
				
				@Override
				public void onLoadingFailed(String arg0, View arg1, FailReason arg2) {
					if (LOG_SWITCH)
						Log.d(LOG_TAG, "failed");
				}
				
				@Override
				public void onLoadingComplete(String arg0, View arg1, Bitmap arg2) {
					if (LOG_SWITCH)
						Log.d(LOG_TAG, "complete");
				}
				
				@Override
				public void onLoadingCancelled(String arg0, View arg1) {
					if (LOG_SWITCH)
						Log.d(LOG_TAG, "canceled");
				}
			});
//			container.removeAllViews();
//			container.addView(imageView);
 			((ViewPager) view).addView(relative, 0);
			return relative;
		}
		
	}
}

