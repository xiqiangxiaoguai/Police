package com.phoenix.setting;

import android.content.Context;
import android.preference.Preference;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.phoenix.data.Constants;
import com.phoenix.police.R;

public class AccessPoint extends Preference{
	
	private final String LOG_TAG = AccessPoint.class.getSimpleName();
	private final boolean LOG_SWITCH = Constants.LOG_SWITCH;
	private TextView textView;
	private ImageView imageView;
	private String mDisplayText;
	private int resId;
	public AccessPoint(Context context) {
		super(context);
		if (LOG_SWITCH)
			Log.d(LOG_TAG, "accesspoint()");
		setWidgetLayoutResource(R.layout.wifi_preference);
	}
	
	
	@Override
	protected void onBindView(View view) {
		super.onBindView(view);
		((ImageView)view.findViewById(R.id.wifi_icon)).setImageResource(resId);
	}
	
	public void setText(String str){
		mDisplayText = str;
	}
	
	public void setIcon(int res){
		resId = res;
	}
}
