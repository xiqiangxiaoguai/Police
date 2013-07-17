package com.phoenix.police;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnBufferingUpdateListener;
import android.media.MediaPlayer.OnCompletionListener;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class PlayerView implements OnBufferingUpdateListener,
                OnCompletionListener, MediaPlayer.OnPreparedListener,
                SurfaceHolder.Callback {


        private int videoWidth;
        private int videoHeight;
        
        public MediaPlayer mediaPlayer;
        private Activity context;
        
        private SurfaceHolder surfaceHolder;
        private ProgressBar skbProgress;
        private TextView curTime,totalTime;
        
        private ImageView thumb;
        private SurfaceView surfaceView;
        private ImageView  btnPlay;
        private TextView title;
        
        private RelativeLayout layout_control;
        private LinearLayout layout_title;
        private String titleStr="";
        private String url;
        private Timer mTimer=new Timer();


        LayoutInflater inflater;
        View view=null;
        
        boolean isAnim;
        /**
         * 构造体
         * @param context
         * @param url  资源路径
         */
        public PlayerView(Activity context,String url,String titleStr)
        {
                this.inflater=LayoutInflater.from(context);
                this.context=context;
                this.url=url;        
                this.titleStr=titleStr;
                
                initPlayerView();
                Bitmap thumbImg=getThumbnail(context, 2000, Uri.parse(url));
                if(thumbImg!=null){
                        thumb.setImageBitmap(thumbImg);
                }
        }


        /**
         * 返回PlayerView
         * 
         * @return View
         */
        public View getPlayerView(){
                if(view!=null){
                        return view;
                }
                return null;
        }
        
        
        /***
         * 初始化PlayerView
         */
        public void initPlayerView(){                
                view=inflater.inflate(R.layout.videoplayer, null);                
                thumb=(ImageView) view.findViewById(R.id.thumb);
                surfaceView = (SurfaceView) view.findViewById(R.id.surfaceView);
                skbProgress = (ProgressBar) view.findViewById(R.id.palyer_control_progress);
                title = (TextView) view.findViewById(R.id.player_title_name);
        
                curTime = (TextView) view.findViewById(R.id.cur_time);
                totalTime = (TextView) view.findViewById(R.id.total_time);
                
                layout_title=(LinearLayout) view.findViewById(R.id.palyer_title_layout);
                layout_control=(RelativeLayout) view.findViewById(R.id.palyer_control_layout);
                

                btnPlay = (ImageView) view.findViewById(R.id.palyer_control_paly);
                btnPlay.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                                if(PlayerView.STATE ==PlayerView.STOP){
                                        btnPlay.setImageResource(R.drawable.stop);        
                                        playUrl();
                                        
                                        layout_title.startAnimation(getAnimUp());
                                        layout_control.startAnimation(getAnimDown());
                                        isAnim = true;
                                        btnPlay.setClickable(false);
                                        
                                        PlayerView.STATE=PlayerView.PLAYING;
                                }else
                                if(PlayerView.STATE==PlayerView.PLAYING){
                                        btnPlay.setImageResource(R.drawable.end);
                                        pause();
                                        PlayerView.STATE=PlayerView.PAUSE;
                                        
                                }else if(PlayerView.STATE==PlayerView.PAUSE){
                                        btnPlay.setImageResource(R.drawable.end);
                                        play();
                                        
                                        layout_title.startAnimation(getAnimUp());
                                        layout_control.startAnimation(getAnimDown());
                                        isAnim = true;
                                        btnPlay.setClickable(false);
                                        PlayerView.STATE=PlayerView.PLAYING;
                                }
                        }
                });
                this.title.setText(titleStr);
                surfaceHolder=surfaceView.getHolder();
                surfaceHolder.addCallback(this);
                surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
                mTimer.schedule(mTimerTask, 0, 1000);
                
                //隐藏或显示标题栏与控制栏
                isAnim=false;
                surfaceView.setOnTouchListener(new OnTouchListener() {
                        @Override
                        public boolean onTouch(View v, MotionEvent event) {
                                if (event.getAction() == MotionEvent.ACTION_UP) {
                                        if (isAnim == false) {
                                                layout_title.startAnimation(getAnimUp());
                                                layout_control.startAnimation(getAnimDown());
                                                isAnim = true;
                                                btnPlay.setClickable(false);
                                        } else {
                                                layout_title.clearAnimation();
                                                layout_control.clearAnimation();
                                                isAnim = false;
                                                btnPlay.setClickable(true);
                                        }
                                }
                                return true;
                        }
                });
        }


        /*******************************************************
         * 通过定时器和Handler来更新进度条
         ******************************************************/
        TimerTask mTimerTask = new TimerTask() {
                @Override
                public void run() {
                        if(mediaPlayer==null)
                                return;
                        if (mediaPlayer.isPlaying() && skbProgress.isPressed() == false) {
                                handleProgress.sendEmptyMessage(0);
                        }
                }
        };

        Handler handleProgress = new Handler() {
                public void handleMessage(Message msg) {

                        int position = mediaPlayer.getCurrentPosition();
                        int duration = mediaPlayer.getDuration();
                
                        if (duration > 0) {
                                long pos = skbProgress.getMax() * position / duration;
                                skbProgress.setProgress((int) pos);                                
                                curTime.setText(""+DateFormat.format("mm:ss", position).toString() );
                                totalTime.setText("/"+DateFormat.format("mm:ss", duration).toString() );
                        }
                };
        };
        //********************************************************************
        /**
         * 首次播放
         */
        public void playUrl()
        {
                try {
                        thumb.setVisibility(View.GONE);
                        mediaPlayer.reset();
                        mediaPlayer.setDataSource(url);
                        mediaPlayer.prepare();//prepare之后自动播放
                } catch (IllegalArgumentException e) {
                        e.printStackTrace();
                } catch (IllegalStateException e) {
                        e.printStackTrace();
                } catch (IOException e) {
                        e.printStackTrace();
                }
        }
        /**
         * 播放
        */
        public void play()
        {
                mediaPlayer.start();
        }

        /**
         * 暂停
         */
        public void pause()
        {
                mediaPlayer.pause();
        }
        /**
         * 停止
         */
        public void stop()
        {
                if (mediaPlayer != null) { 
                        mediaPlayer.stop();
            mediaPlayer.release(); 
            mediaPlayer = null; 

            skbProgress.setProgress(0);
            thumb.setVisibility(View.VISIBLE);
            PlayerView.STATE =PlayerView.STOP;
                    btnPlay.setImageResource(R.drawable.end);
        } 
        }
        
        
        
        //********************************************************************
        @Override
        public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3) {
                Log.e("mediaPlayer", "surface changed");
        }

        @Override
        public void surfaceCreated(SurfaceHolder arg0) {
                try {
                        if(mediaPlayer==null){
                                mediaPlayer = new MediaPlayer();
                                mediaPlayer.setDisplay(surfaceHolder);
                                mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                                mediaPlayer.setOnBufferingUpdateListener(this);
                                mediaPlayer.setOnPreparedListener(this);
                                mediaPlayer.setOnCompletionListener(this);
                        }
                } catch (Exception e) {
                        Log.e("mediaPlayer", "error", e);
                }
                Log.e("mediaPlayer", "surface created");
        }
        @Override
        public void surfaceDestroyed(SurfaceHolder arg0) {
                Log.e("mediaPlayer", "surface destroyed");
                
        }
        

        //********************************************************************
        @Override
        /**
         * 通过onPrepared播放
         */
        public void onPrepared(MediaPlayer mediaplayer) {
                videoWidth = mediaPlayer.getVideoWidth();
                videoHeight = mediaPlayer.getVideoHeight();
                if (videoHeight != 0 && videoWidth != 0) {
                        mediaplayer.start();
                }
                Log.e("mediaPlayer", "onPrepared");
        }

        @Override
        public void onCompletion(MediaPlayer mediaplayer) {
                this.stop();
                context.finish();//播放完后退出此Activity
        }

        @Override
        public void onBufferingUpdate(MediaPlayer arg0, int bufferingProgress) {
                skbProgress.setSecondaryProgress(bufferingProgress);
                int currentProgress=skbProgress.getMax()*mediaPlayer.getCurrentPosition()/mediaPlayer.getDuration();
                Log.e(currentProgress+"% play", bufferingProgress + "% buffer");
                
        }
        
        
        //-----------------------------------------------------------------------------------------------
        
        /**
         * 获取指定帧的图片(局限于SDcard视频)
         * @param paramContext 内容
         * @param paramLong     指定时间(mm)
         * @param paramUri        资源路径
         * @return Bitmap
         */
	@SuppressLint("NewApi")
	private Bitmap getThumbnail(Context paramContext, long paramLong,
			Uri paramUri) {
		MediaMetadataRetriever localMediaMetadataRetriever = new MediaMetadataRetriever();
		Bitmap localBitmap1;
		try {
			localMediaMetadataRetriever.setDataSource(paramContext, paramUri);// 获取图像前必须先设置dataSource

			Bitmap localBitmap2 = localMediaMetadataRetriever
					.getFrameAtTime(1000L * paramLong);// 获取指定时间视频文件图像
			localBitmap1 = localBitmap2;
			if (localBitmap1 == null) {
				localBitmap1 = BitmapFactory.decodeResource(paramContext.getResources(), R.drawable.background);
			}
			return localBitmap1;
		} catch (RuntimeException localRuntimeException)

		{
			Log.d("SecVideoWidgetProvider",
					"getThumbnail localRuntimeException");
			return null;
		}

	}
        
        
        
          //-------------------------------------------------------------Animation
                public static final int Time=200;
                /**
                 * 向上移动
                 * @return Animation
                 */
                public Animation getAnimUp(){
                        Animation anim = new TranslateAnimation(
                                        TranslateAnimation.RELATIVE_TO_SELF, 0,
                                        TranslateAnimation.RELATIVE_TO_SELF, 0f,
                                        TranslateAnimation.RELATIVE_TO_SELF, 0f,
                                        TranslateAnimation.RELATIVE_TO_SELF, -1f);
                        LinearInterpolator inter=new LinearInterpolator();
                anim.setInterpolator(inter);
                        anim.setDuration(Time);
                        anim.setFillBefore(false);
                        anim.setFillAfter(true);
                        return anim;
                }
                /**
                 * 向下移动
                 * @return Animation
                 */
                public Animation getAnimDown(){
                        Animation anim = new TranslateAnimation(
                                        TranslateAnimation.RELATIVE_TO_SELF, 0,
                                        TranslateAnimation.RELATIVE_TO_SELF, 0f,
                                        TranslateAnimation.RELATIVE_TO_SELF, 0f,
                                        TranslateAnimation.RELATIVE_TO_SELF, 1f);
                        LinearInterpolator inter=new LinearInterpolator();
                anim.setInterpolator(inter);
                        anim.setDuration(Time);
                        anim.setFillBefore(false);
                        anim.setFillAfter(true);
                        return anim;
                }
                
        //---------------------------------------------------------------状态
        public static final int PLAYING=1;//播放中
        public static final int PAUSE=2;//暂停
        public static final int STOP=3;//停止
        public static int STATE=STOP;
}