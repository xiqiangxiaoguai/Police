package com.phoenix.police;

import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.TextView;

public class AudioActivity extends Activity {
	/** Called when the activity is first created. */

	private static final int STATE_IDLE = 0;
	private static final int STATE_RECORDING = 1;
	private int mState = STATE_IDLE;
	private ImageButton btnRecord;
	private int cSecs =0;
	private TextView timeCount;
	Handler mHandler = new Handler(){
		public void handleMessage(android.os.Message msg) {
			switch(msg.what){
			case 0:
				if(null != btnRecord){
					if(mState == STATE_IDLE){
						btnRecord.setBackgroundResource(R.drawable.start);
					}else if( mState == STATE_RECORDING){
						btnRecord.setBackgroundResource(R.drawable.stop);
					}
				}
				break;
			case 1:
				cSecs ++;
				int hour = cSecs/3600;
				int min = (cSecs%3600)/60;
				int sec = cSecs%60;
				timeCount.setText(String.format("%1$02d:%2$02d:%3$02d",hour, min, sec));
				break;
			}
			
		};
	};
	
	private Timer timer = null;
	private TimerTask task = null;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.audio_activity);
		btnRecord = (ImageButton) findViewById(R.id.record);
		btnRecord.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(mState == STATE_IDLE){
					startRecord();
					mState = STATE_RECORDING;
					startTimer();
					mHandler.sendEmptyMessage(0);
				}else if(mState == STATE_RECORDING){ 
					stopRecord();
					mState = STATE_IDLE;
					mHandler.sendEmptyMessage(0);
					stopTimer();
				}
			}
		});
		timeCount = (TextView) findViewById(R.id.timeCount);
	}
	
	private void startTimer(){
		cSecs = 0;
		if(null == timer){
			if(null == task){
				task = new TimerTask() {
					@Override
					public void run() {
						mHandler.sendEmptyMessage(1);
					}
				};
			}
		}
		timer = new Timer(true);
		timer.schedule(task, 1000, 1000);
	}
	private void stopTimer(){
		if(null != timer){
			task.cancel();
			task = null;
			timer.cancel();
			timer.purge();
			timer = null;
			mHandler.removeMessages(1);
		}
	}
	
	private void startRecord(){
		AudioRecordFunc func = AudioRecordFunc.getInstance();
		func.startRecordAndFile();
	}
	private void stopRecord(){
		AudioRecordFunc func = AudioRecordFunc.getInstance();
		func.stopRecordAndFile();
	}
}