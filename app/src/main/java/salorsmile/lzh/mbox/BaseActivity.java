package salorsmile.lzh.mbox;


import salorsmile.lzh.service.DownloadService;
import salorsmile.lzh.service.PlayService;
import salorsmile.lzh.utils.L;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.FragmentActivity;

public abstract class BaseActivity extends FragmentActivity {
    /*服务*/
    protected PlayService mPlayService;
    protected DownloadService mDownloadService;
    private final String TAG = BaseActivity.class.getSimpleName();

    /*服务连接管理*/
    private ServiceConnection mPlayServiceConnection = new ServiceConnection() {
        /*释放资源*/
        @Override
        public void onServiceDisconnected(ComponentName name) {
            L.l(TAG, "play--->onServiceDisconnected");
            mPlayService = null;
        }

        /*获得歌曲播放服务的链接*/
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            /*获得链接服务对象*/
            mPlayService = ((PlayService.PlayBinder) service).getService();
            /*设置回调监听事件*/
            mPlayService.setOnMusicEventListener(mMusicEventListener);
            /*切换歌曲（获取当前播放歌曲位置）*/
            onChange(mPlayService.getPlayingPosition());
        }
    };

    /*获得下载服务的链接*/

    private ServiceConnection mDownloadServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceDisconnected(ComponentName name) {
           // L.l(TAG, "download--->onServiceDisconnected");
            mDownloadService = null;
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mDownloadService = ((DownloadService.DownloadBinder) service).getService();
        }
    };

    /**
     * 音乐播放服务回调接口的实现类
     * 调用音乐播放服务
     */
    private PlayService.OnMusicEventListener mMusicEventListener =
            new PlayService.OnMusicEventListener() {
                @Override
                public void onPublish(int progress) {
                    BaseActivity.this.onPublish(progress);
                }

                @Override
                public void onChange(int position) {
                    BaseActivity.this.onChange(position);
                }
            };

    /**
     * Fragment的view加载完成后回调
     *
     * 注意：
     * allowBindService()使用绑定的方式启动歌曲播放的服务
     * allowUnbindService()方法解除绑定
     *
     * 在SplashActivity.java中使用startService()方法启动过该音乐播放服务了
     * 那么大家需要注意的事，该服务不会因为调用allowUnbindService()方法解除绑定
     * 而停止。
     */
    public void allowBindService() {
        getApplicationContext().bindService(new Intent(this, PlayService.class),
                mPlayServiceConnection,
                Context.BIND_AUTO_CREATE);
    }

    /**
     * fragment的view消失后回调
     */
    public void allowUnbindService() {
        getApplicationContext().unbindService(mPlayServiceConnection);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //绑定下载服务
        bindService(new Intent(this, DownloadService.class),
                mDownloadServiceConnection,
                Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onDestroy() {
        unbindService(mDownloadServiceConnection);
        super.onDestroy();
    }

    public DownloadService getDownloadService() {
        return mDownloadService;
    }

    /**
     * 更新进度
     * 抽象方法由子类实现
     * 实现service与主界面通信
     * @param progress 进度
     */
    public abstract void onPublish(int progress);
    /**
     * 切换歌曲
     * 抽象方法由子类实现
     * 实现service与主界面通信
     * @param position 歌曲在list中的位置
     */
    public abstract void onChange(int position);
}

