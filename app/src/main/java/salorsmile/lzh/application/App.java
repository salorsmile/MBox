package salorsmile.lzh.application;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowManager;

import java.util.ArrayList;
import java.util.Random;

import salorsmile.lzh.entity.Music;
import salorsmile.lzh.mbox.R;
import salorsmile.lzh.service.DownloadService;
import salorsmile.lzh.service.PlayService;

public class App extends Application {
    public static Context sContext;
    public static int sScreenWidth;
    public static int sScreenHeight;

    private static int appTheme = R.mipmap.p015;

    private static int[] imgsAppTheme = {
            R.mipmap.p001,
            R.mipmap.p002,
            R.mipmap.p003,
            R.mipmap.p004,
            R.mipmap.p005,
            R.mipmap.p006,
            R.mipmap.p007,
            R.mipmap.p008,
            R.mipmap.p009,
            R.mipmap.p010,
            R.mipmap.p011,
            R.mipmap.p012,
            R.mipmap.p013,
            R.mipmap.p014,
            R.mipmap.p015,
            R.mipmap.p016,
            R.mipmap.p017
    };


    public static int getThemeArrayLength() {
        return imgsAppTheme.length;
    }

    public static int getAppTheme() {
        return appTheme;
    }
    public static int getAppThemeByIndex(int index) {
        return imgsAppTheme[index];
    }

    public static void setAppTheme(int appTheme) {
        App.appTheme = appTheme;
    }

    //自定义方法随机获取图片资源中某一张图片的下标
    public int getThemeBgIndex() {
        //获取随机数的工具Random
        Random random = new Random();
        appTheme = imgsAppTheme[random.nextInt(imgsAppTheme.length)];
        return appTheme;
    }

    //  存放收藏歌曲列表
    public static ArrayList<Music> favoriteMusicList = new ArrayList<Music>();

    @Override
    public void onCreate() {
        super.onCreate();
        sContext = getApplicationContext();
        Log.i("TAG1", "-->" + sContext);

        startService(new Intent(this, PlayService.class));
        startService(new Intent(this, DownloadService.class));
        WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics dm = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(dm);
        sScreenWidth = dm.widthPixels;
        sScreenHeight = dm.heightPixels;
    }


}
