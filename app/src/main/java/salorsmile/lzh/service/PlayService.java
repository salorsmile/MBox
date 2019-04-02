package salorsmile.lzh.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import salorsmile.lzh.entity.Music;
import salorsmile.lzh.mbox.R;
import salorsmile.lzh.mbox.PlayActivity;
import salorsmile.lzh.utils.Constants;
import salorsmile.lzh.utils.ImageTools;
import salorsmile.lzh.utils.L;
import salorsmile.lzh.utils.MusicIconLoader;
import salorsmile.lzh.utils.MusicUtils;
import salorsmile.lzh.utils.SpUtils;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.SystemClock;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

/**
 * 音乐播放服务 服务中启动了硬件加速器感应器
 * 实现功能：摇动手机自动播放下一首歌曲
 */



public class PlayService extends Service implements
        MediaPlayer.OnCompletionListener {

    //播放模式
    public static final int ORDER_PLAY = 1;//顺序播放
    public static final int RANDOM_PLAY = 2;//随机播放
    public static final int SINGLE_PLAY = 3;//单曲循环
    private int play_mode = ORDER_PLAY;//播放模式,默认为顺序播放


    private static final String TAG =
            PlayService.class.getSimpleName();

    /*传感器管理器*/
    private SensorManager mSensorManager;
    private MediaPlayer mPlayer;
    private OnMusicEventListener mListener;
    public int mPlayingPosition; // 当前正在播放
    private WakeLock mWakeLock = null;//获取设备电源锁，防止锁屏后服务被停止
    private boolean isShaking;
    private Notification notification;//通知栏
    private RemoteViews remoteViews;//通知栏布局
    private NotificationManager notificationManager;

    public static List<Music> recentPlayMusics; //最近播放 智
    // 单线程池
    private ExecutorService mProgressUpdatedListener = Executors
            .newSingleThreadExecutor();

    public class PlayBinder extends Binder {
        public PlayService getService() {
            return PlayService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        mSensorManager.registerListener(mSensorEventListener,
                mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_GAME);
        return new PlayBinder();
    }

    //set方法
    public void setPlay_mode(int play_mode) {
        this.play_mode = play_mode;
    }

    //get方法
    public int getPlay_mode() {
        return play_mode;
    }

    private Random random = new Random();//创建随机对象

    @SuppressWarnings("deprecation")
    @Override
    public void onCreate() {
        super.onCreate();
        acquireWakeLock();//获取设备电源锁
        /*获取系统传感器服务*/
        mSensorManager = (SensorManager)
                getSystemService(Context.SENSOR_SERVICE);
        /*初始化歌曲列表*/
        MusicUtils.initMusicList();
        /*获取上一次存储的歌曲播放位置*/
        mPlayingPosition = (Integer)
                SpUtils.get(this, Constants.PLAY_POS, 0);

        /*检测当前播放列表是否为空*/
        if(MusicUtils.sMusicList.size()<=0){
            Toast.makeText(getApplicationContext(),
                    "当前手机没有MP3文件", Toast.LENGTH_LONG).show();
        }else{
            /**/
            if(getPlayingPosition()<0){
                mPlayingPosition=0;
            }
            Uri uri = Uri.parse(MusicUtils.sMusicList.get(
                    getPlayingPosition()).getUri());
            mPlayer = MediaPlayer.create(PlayService.this,uri);
            mPlayer.setOnCompletionListener(this);
        }
        recentPlayMusics=new ArrayList<Music>();//初始化最近播放的list
        // 开始更新进度的线程
        mProgressUpdatedListener.execute(mPublishProgressRunnable);
/*---------------------------------------通知栏定义-------------------------------------------------------------------------*/
        /**
         * 该方法虽然被抛弃过时，但是通用！
         */
        PendingIntent pendingIntent = PendingIntent
                .getActivity(PlayService.this,
                        0, new Intent(PlayService.this, PlayActivity.class), 0);
        remoteViews = new RemoteViews(getPackageName(),
                R.layout.play_notification);
        notification = new Notification(R.mipmap.play_item_icon,
                "歌曲正在播放", System.currentTimeMillis());
        notification.contentIntent = pendingIntent;
        notification.contentView = remoteViews;
        //标记位，设置通知栏一直存在
        notification.flags =Notification.FLAG_ONGOING_EVENT;

        Intent intent = new Intent(PlayService.class.getSimpleName());
        intent.putExtra("BUTTON_NOTI", 1);
        PendingIntent preIntent = PendingIntent.getBroadcast(
                PlayService.this,
                1, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        remoteViews.setOnClickPendingIntent(
                R.id.music_play_pre, preIntent);

        intent.putExtra("BUTTON_NOTI", 2);
        PendingIntent pauseIntent = PendingIntent.getBroadcast(
                PlayService.this,
                2, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        remoteViews.setOnClickPendingIntent(
                R.id.music_play_pause, pauseIntent);

        intent.putExtra("BUTTON_NOTI", 3);
        PendingIntent nextIntent = PendingIntent.getBroadcast
                (PlayService.this,
                        3, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        remoteViews.setOnClickPendingIntent(
                R.id.music_play_next, nextIntent);

        intent.putExtra("BUTTON_NOTI", 4);
        PendingIntent exit = PendingIntent.getBroadcast(PlayService.this,
                4, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        remoteViews.setOnClickPendingIntent(
                R.id.music_play_notifi_exit, exit);

        notificationManager = (NotificationManager)
                getSystemService(NOTIFICATION_SERVICE);
        setRemoteViews();
/*---------------------------------------通知栏定义-------------------------------------------------------------------------*/


        /**
         * 注册广播接收者
         * 功能：
         * 监听通知栏按钮点击事件
         */
        IntentFilter filter = new IntentFilter(
                PlayService.class.getSimpleName());
        MyBroadCastReceiver receiver = new MyBroadCastReceiver();
        registerReceiver(receiver, filter);
    }

    public void setRemoteViews(){
        L.l(TAG, "进入——》setRemoteViews()");
        if(getPlayingPosition()>0){
            remoteViews.setTextViewText(R.id.music_name,
                    MusicUtils.sMusicList.get(
                            getPlayingPosition()).getTitle());
            remoteViews.setTextViewText(R.id.music_author,
                    MusicUtils.sMusicList.get(
                            getPlayingPosition()).getArtist());
            Bitmap icon = MusicIconLoader.getInstance().load(
                    MusicUtils.sMusicList.get(
                            getPlayingPosition()).getImage());
            remoteViews.setImageViewBitmap(R.id.music_icon,icon == null
                    ? ImageTools.scaleBitmap(R.mipmap.play_item_icon)
                    : ImageTools
                    .scaleBitmap(icon));

        }
        if (isPlaying()) {
            remoteViews.setImageViewResource(R.id.music_play_pause,
                    R.mipmap.btn_notification_player_stop_normal);
        }else {
            remoteViews.setImageViewResource(R.id.music_play_pause,
                    R.mipmap.btn_notification_player_play_normal);
        }
        //通知栏更新
        notificationManager.notify(5, notification);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startForeground(0, notification);//让服务前台运行
        return Service.START_STICKY;
    }

    /**
     * 感应器的时间监听器
     */
    private SensorEventListener mSensorEventListener =
            new SensorEventListener() {
                @Override
                public void onSensorChanged(SensorEvent event) {
                    if (isShaking)
                        return;

                    if (Sensor.TYPE_ACCELEROMETER == event.sensor.getType()) {
                        float[] values = event.values;
                        /**
                         * 监听三个方向上的变化，数据变化剧烈，next()方法播放下一首歌曲
                         */
                        if (Math.abs(values[0]) > 8 && Math.abs(values[1]) > 8
                                && Math.abs(values[2]) > 8) {
                            isShaking = true;
                            nextByMode();
                            // 延迟200毫秒 防止抖动
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    isShaking = false;
                                }
                            }, 200);
                        }
                    }
                }

                @Override
                public void onAccuracyChanged(Sensor sensor, int accuracy) {

                }
            };

    /**
     * 更新进度的线程
     */
    private Runnable mPublishProgressRunnable = new Runnable() {
        @Override
        public void run() {
            while (true) {
                if (mPlayer != null && mPlayer.isPlaying()
                        && mListener != null) {
                    mListener.onPublish(mPlayer.getCurrentPosition());
                }
			/*
			 * SystemClock.sleep(millis) is a utility function very similar
			 * to Thread.sleep(millis), but it ignores InterruptedException.
			 * Use this function for delays if you do not use
			 * Thread.interrupt(), as it will preserve the interrupted state
			 * of the thread. 这种sleep方式不会被Thread.interrupt()所打断
			 */SystemClock.sleep(200);
            }
        }
    };

    /**
     * 设置回调
     *
     * @param l
     */
    public void setOnMusicEventListener(OnMusicEventListener l) {
        mListener = l;
    }

    /**
     * 播放
     *
     * @param position
     *            音乐列表播放的位置
     * @return 当前播放的位置
     */
    public int play(int position) {
        if(MusicUtils.sMusicList.size()<=0){
            Toast.makeText(getApplicationContext(),
                    "当前手机没有MP3文件", Toast.LENGTH_LONG).show();
            return -1;
        }
        if (position < 0)
            position = 0;
        if (position >= MusicUtils.sMusicList.size())
            position = MusicUtils.sMusicList.size() - 1;
        try {
            mPlayer.reset();
            mPlayer.setDataSource(MusicUtils
                    .sMusicList.get(position).getUri());
            mPlayer.prepare();
            start();
            if (mListener != null)
                mListener.onChange(position);
        } catch (Exception e) {
            e.printStackTrace();
        }
        mPlayingPosition = position;

        //当歌曲开始播放时，先判断当前歌曲是否收藏 然后发送广播，通知activity改变图标状态
        Intent intent=new Intent("ModifyFavoriteIcon");
        intent.putExtra("mPlayingPosition",mPlayingPosition);
        sendBroadcast(intent);


        //添加到最近播放列表  智
        recentPlayMusics.add(MusicUtils.sMusicList.get(mPlayingPosition));
        SpUtils.put(Constants.PLAY_POS, mPlayingPosition);
        /*更新通知栏*/
        setRemoteViews();
        return mPlayingPosition;
    }
    /**
     * 继续播放
     * @return 当前播放的位置 默认为0
     */
    public int resume() {
        if(mPlayer==null){
            return -1;
        }
        if (isPlaying())
            return -1;
        mPlayer.start();
        setRemoteViews();
        return mPlayingPosition;
    }
    /**
     * 暂停播放
     * @return 当前播放的位置
     */
    public int pause() {
        if(MusicUtils.sMusicList.size()==0){
            Toast.makeText(getApplicationContext(),
                    "当前手机没有MP3文件", Toast.LENGTH_LONG).show();
            return -1;
        }
        if (!isPlaying())
            return -1;
        mPlayer.pause();
        setRemoteViews();
        return mPlayingPosition;
    }
    /**
     * 下一曲
     *
     * @return 当前播放的位置
     */
    public int next() {
        if (mPlayingPosition >= MusicUtils.sMusicList.size() - 1) {
            return play(0);
        }
        return play(mPlayingPosition + 1);
    }

    public int nextByMode() {
        if (play_mode == ORDER_PLAY){
            return next();

        }else if (play_mode == SINGLE_PLAY){
            play(mPlayingPosition);
            return mPlayingPosition;
        }else if (play_mode == RANDOM_PLAY){
            int i = random.nextInt(MusicUtils.sMusicList.size());
            play(i);

        }
        return 0;
    }

    /**
     * 上一曲
     * @return 当前播放的位置
     */
    public int pre() {
        if (mPlayingPosition <= 0) {
            return play(MusicUtils.sMusicList.size() - 1);
        }
        return play(mPlayingPosition - 1);
    }

    public int preByMode() {
        if (play_mode == ORDER_PLAY){
            return pre();

        }else if (play_mode == SINGLE_PLAY){
            play(mPlayingPosition);
            return mPlayingPosition;
        }else if (play_mode == RANDOM_PLAY){
            int i = random.nextInt(MusicUtils.sMusicList.size());
            play(i);

        }
        return 0;
    }

    /**
     * 是否正在播放
     *
     * @return
     */
    public boolean isPlaying() {
        return null != mPlayer && mPlayer.isPlaying();
    }

    /**
     * 获取正在播放的歌曲在歌曲列表的位置
     *
     * @return
     */
    public int getPlayingPosition() {
        return mPlayingPosition;
    }

    /**
     * 获取当前正在播放音乐的总时长
     *
     * @return
     */
    public int getDuration() {
        if (!isPlaying())
            return 0;
        return mPlayer.getDuration();
    }

    /**
     * 拖放到指定位置进行播放
     *
     * @param msec
     */
    public void seek(int msec) {
        if (!isPlaying())
            return;
        mPlayer.seekTo(msec);
    }

    /**
     * 开始播放
     */
    private void start() {
        mPlayer.start();
    }

    /**
     * 音乐播放完毕 自动下一曲
     */
    @Override
    public void onCompletion(MediaPlayer mp) {
        switch (play_mode){

            case ORDER_PLAY://顺序播放
                next();//下一首
                break;
            case RANDOM_PLAY://随机播放
                //currentPosition = random.nextInt(mp3Infos.size());//随机下标为mp3Infos.size()
                //play(currentPosition);

                play(random.nextInt(MusicUtils.sMusicList.size()));
                break;
            case SINGLE_PLAY://单曲循环
                play(mPlayingPosition);
                break;
            default:
                Log.i("TAG",random.nextInt(MusicUtils.sMusicList.size())+"");
                break;
        }
    }

    @Override
    public boolean onUnbind(Intent intent) {
        L.l("play service", "unbind");
        mSensorManager.unregisterListener(mSensorEventListener);
        return true;
    }

    @Override
    public void onRebind(Intent intent) {
        super.onRebind(intent);
        if (mListener != null)
            mListener.onChange(mPlayingPosition);
    }

    @Override
    public void onDestroy() {
        L.l(TAG, "PlayService.java的onDestroy()方法调用");
        release();
        stopForeground(true);
        mSensorManager.unregisterListener(mSensorEventListener);
        super.onDestroy();
    }

    /**
     * 服务销毁时，释放各种控件
     */
    private void release() {
        if (!mProgressUpdatedListener.isShutdown())
            mProgressUpdatedListener.shutdownNow();
        mProgressUpdatedListener = null;
        //释放设备电源锁
        releaseWakeLock();
        if (mPlayer != null)
            mPlayer.release();
        mPlayer = null;
    }

    // 申请设备电源锁
    private void acquireWakeLock() {
        L.l(TAG, "正在申请电源锁");
        if (null == mWakeLock) {
            PowerManager pm = (PowerManager) this
                    .getSystemService(Context.POWER_SERVICE);
            mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK
                    | PowerManager.ON_AFTER_RELEASE, "");
            if (null != mWakeLock) {
                mWakeLock.acquire();
                L.l(TAG, "电源锁申请成功");
            }
        }
    }

    // 释放设备电源锁
    private void releaseWakeLock() {
        L.l(TAG, "正在释放电源锁");
        if (null != mWakeLock) {
            mWakeLock.release();
            mWakeLock = null;
            L.l(TAG, "电源锁释放成功");
        }
    }

    private class MyBroadCastReceiver extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(
                    PlayService.class.getSimpleName())) {
                L.l(TAG, "MyBroadCastReceiver类——》onReceive（）");
                L.l(TAG, "button_noti-->"
                        +intent.getIntExtra("BUTTON_NOTI", 0));
                switch (intent.getIntExtra("BUTTON_NOTI", 0)) {
                    case 1:
                        preByMode();
                        break;
                    case 2:
                        if (isPlaying()) {
                            pause(); // 暂停
                        } else {
                            resume(); // 播放
                        }
                        break;
                    case 3:
                        nextByMode();
                        break;
                    case 4:
                        if (isPlaying()) {
                            pause();
                        }
                        //取消通知栏
                        notificationManager.cancel(5);
                        break;
                    default:
                        break;
                }
            }
            if (mListener != null) {
                mListener.onChange(getPlayingPosition());
            }
        }
    }

    /**
     * 音乐播放回调接口
     */
    public interface OnMusicEventListener {
        public void onPublish(int percent);

        public void onChange(int position);
    }
}