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
         * ������
         * @param context
         * @param url  ��Դ·��
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
         * ����PlayerView
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
         * ��ʼ��PlayerView
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
                
                //���ػ���ʾ�������������
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
         * ͨ����ʱ����Handler�����½�����
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
         * �״β���
         */
        public void playUrl()
        {
                try {
                        thumb.setVisibility(View.GONE);
                        mediaPlayer.reset();
                        mediaPlayer.setDataSource(url);
                        mediaPlayer.prepare();//prepare֮���Զ�����
                } catch (IllegalArgumentException e) {
                        e.printStackTrace();
                } catch (IllegalStateException e) {
                        e.printStackTrace();
                } catch (IOException e) {
                        e.printStackTrace();
                }
        }
        /**
         * ����
        */
        public void play()
        {
                mediaPlayer.start();
        }

        /**
         * ��ͣ
         */
        public void pause()
        {
                mediaPlayer.pause();
        }
        /**
         * ֹͣ
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
         * ͨ��onPrepared����
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
                context.finish();//��������˳���Activity
        }

        @Override
        public void onBufferingUpdate(MediaPlayer arg0, int bufferingProgress) {
                skbProgress.setSecondaryProgress(bufferingProgress);
                int currentProgress=skbProgress.getMax()*mediaPlayer.getCurrentPosition()/mediaPlayer.getDuration();
                Log.e(currentProgress+"% play", bufferingProgress + "% buffer");
                
        }
        
        
        //-----------------------------------------------------------------------------------------------
        
        /**
         * ��ȡָ��֡��ͼƬ(������SDcard��Ƶ)
         * @param paramContext ����
         * @param paramLong     ָ��ʱ��(mm)
         * @param paramUri        ��Դ·��
         * @return Bitmap
         */
	@SuppressLint("NewApi")
	private Bitmap getThumbnail(Context paramContext, long paramLong,
			Uri paramUri) {
		MediaMetadataRetriever localMediaMetadataRetriever = new MediaMetadataRetriever();
		Bitmap localBitmap1;
		try {
			localMediaMetadataRetriever.setDataSource(paramContext, paramUri);// ��ȡͼ��ǰ����������dataSource

			Bitmap localBitmap2 = localMediaMetadataRetriever
					.getFrameAtTime(1000L * paramLong);// ��ȡָ��ʱ����Ƶ�ļ�ͼ��
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
                 * �����ƶ�
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
                 * �����ƶ�
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
                
        //---------------------------------------------------------------״̬
        public static final int PLAYING=1;//������
        public static final int PAUSE=2;//��ͣ
        public static final int STOP=3;//ֹͣ
        public static int STATE=STOP;
}