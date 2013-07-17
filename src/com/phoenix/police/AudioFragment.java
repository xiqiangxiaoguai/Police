package com.phoenix.police;

import java.io.File;
import java.util.ArrayList;

import android.app.Activity;
import android.app.Fragment;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.phoenix.data.Constants;

public class AudioFragment extends Fragment{
	private static final boolean LOG_SWITCH = Constants.LOG_SWITCH;
	private static final String LOG_TAG = AudioFragment.class.getSimpleName();
	Activity mActivity;
	ArrayList<String> audios = new ArrayList<String>();
	ArrayList<String> audiosPath = new ArrayList<String>();
	Handler ownLooperHandler;
	MediaPlayer mediaPlayer;
	SeekBar mSeekBar;
	TextView mPastedTime;
	TextView mRemaingTime;
	private Handler uiHandler = new Handler(){
		public void handleMessage(Message msg) {
			ListView list = (ListView) getView().findViewById(R.id.audios);
			list.setAdapter(new AudioAdapter());
			list.setOnItemClickListener(new OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> arg0, View arg1,
						int arg2, long arg3) {
					uiHandler.removeCallbacks(updateSeekbar);
					if(mediaPlayer != null){
						mediaPlayer.stop();
						mediaPlayer.release();
					}
					mediaPlayer = new MediaPlayer();
					try {
						mediaPlayer.setDataSource(audiosPath.get(arg2));
						mediaPlayer.prepare();
						mediaPlayer.start();
						mSeekBar.setMax(mediaPlayer.getDuration()/1000);
						uiHandler.post(updateSeekbar);
					} catch (Exception e) {
						e.printStackTrace();
					} 
				}
			});
		};
	};
	Runnable run = new Runnable() {
		@Override
		public void run() {
			if(null != ownLooperHandler){
				getAudios();
				uiHandler.sendEmptyMessage(0);
			}
		}
	};
	Runnable updateSeekbar = new Runnable(){
		public void run() {
			int curPosition = mediaPlayer.getCurrentPosition()/1000;
			int remaining = mSeekBar.getMax() - curPosition;
			mSeekBar.setProgress(curPosition);
			mPastedTime.setText(String.format("%1$02d:%2$02d", curPosition/60,curPosition%60));
			mRemaingTime.setText(String.format("%1$02d:%2$02d", remaining/60,remaining%60));
			uiHandler.postDelayed(updateSeekbar,1000);
		};
	};
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		mActivity = activity;
	};
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		HandlerThread hThread = new HandlerThread(AudioFragment.class.getSimpleName());
		hThread.start();
		ownLooperHandler = new Handler(hThread.getLooper()){
		};
		ownLooperHandler.post(run);
	}
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.audio_fragment, container,false);
		mSeekBar = (SeekBar) view.findViewById(R.id.seekbar);
		mPastedTime = (TextView) view.findViewById(R.id.pasted_time);
		mRemaingTime = (TextView) view.findViewById(R.id.remaining_time);
		return view;
	}
	
	private void getAudios(){
		File[] files = new File(Constants.AUDIO_PATH).listFiles();
		for(File file : files){
			audios.add(file.getName());
			audiosPath.add(file.getAbsolutePath());
		}
	}
	class AudioAdapter extends BaseAdapter{

		@Override
		public int getCount() {
			return audios.size();
		}

		@Override
		public Object getItem(int arg0) {
			return null;
		}

		@Override
		public long getItemId(int arg0) {
			return 0;
		}

		@Override
		public View getView(int position, View convert, ViewGroup group) {
			LinearLayout layout;
			if(null == convert){   
				layout = (LinearLayout) mActivity.getLayoutInflater().inflate(R.layout.audio_content, null);
			}else{
				layout = (LinearLayout) convert;
			}
			((TextView)layout.findViewById(R.id.audioname)).setText(audios.get(position));
			return layout;
		}
		
	}
	@Override
	public void onPause() {
		super.onPause();
		if(mediaPlayer!= null && mediaPlayer.isPlaying()){
			mediaPlayer.stop();
			mediaPlayer.release();
		}
		uiHandler.removeCallbacks(updateSeekbar);
	}
}
