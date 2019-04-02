package salorsmile.lzh.mbox;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.support.annotation.IdRes;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import salorsmile.lzh.application.App;
import salorsmile.lzh.entity.Music;
import salorsmile.lzh.fragment.MyMusicFragment;
import salorsmile.lzh.fragment.RecommendFragment;
import salorsmile.lzh.fragment.SearchFragment;
import salorsmile.lzh.fragment.TaoGeFragment;
import salorsmile.lzh.utils.ImageTools;
import salorsmile.lzh.utils.MusicIconLoader;
import salorsmile.lzh.utils.MusicUtils;

public class MainActivity extends BaseActivity implements View.OnClickListener{
    private ViewPager vpMain;
    private RadioGroup rgMain;
    private RelativeLayout rlMain;
    private Handler handler; //借助此对象发送消息
    private ImageView imgPlayIcon;
    private ImageView imgPlay;
    private ImageView imgPre;
    private ImageView imgNext;
    private TextView tvTitle;
    private TextView tvArtist;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //初始化
        setupView();
        //添加监听器
        addListener();

    }

    private void addListener() {
        imgPlayIcon.setOnClickListener(this);
        imgPlay.setOnClickListener(this);
        imgNext.setOnClickListener(this);
        imgPre.setOnClickListener(this);

        vpMain.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }
            @Override
            public void onPageSelected(int position) {
                switch (position){
                    case 0:
                        rgMain.check(R.id.rb_main);
                        break;
                    case 1:
                        rgMain.check(R.id.rb_taoge);
                        break;
                    case 2:
                        rgMain.check(R.id.rb_search);
                        break;
                    case 3:
                        rgMain.check(R.id.rb_recommend);
                        break;
                }
            }
            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        rgMain.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, @IdRes int checkedId) {
                switch (checkedId){
                    case R.id.rb_main:
                        vpMain.setCurrentItem(0);
                        break;
                    case R.id.rb_taoge:
                        vpMain.setCurrentItem(1);
                        break;
                    case R.id.rb_search:
                        vpMain.setCurrentItem(2);
                        break;
                    case R.id.rb_recommend:
                        vpMain.setCurrentItem(3);
                        break;
                }
            }
        });
    }
    private void setupView() {
        tvTitle = (TextView) findViewById(R.id.tv_play_main_title);
        tvArtist = (TextView) findViewById(R.id.tv_play_main_artist);
        imgPre = (ImageView) findViewById(R.id.iv_pre_main);
        imgNext = (ImageView) findViewById(R.id.iv_next_main);
        imgPlay = (ImageView) findViewById(R.id.iv_play_main);
        imgPlayIcon = (ImageView) findViewById(R.id.iv_play_main_icon);
        rlMain= (RelativeLayout) findViewById(R.id.rl_main);
        vpMain=(ViewPager)findViewById(R.id.vp_main);
        rgMain=(RadioGroup)findViewById(R.id.rg_main);

        //设置adapter适配器
        MyPagerAdapter adapter=new MyPagerAdapter(getSupportFragmentManager());
        vpMain.setAdapter(adapter);

        handler=new Handler();


    }



    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_play_main_icon:
                startActivity(new Intent(MainActivity.this, PlayActivity.class));
                break;
            case R.id.iv_play_main:
                if (MusicUtils.sMusicList.isEmpty()) {
                    Toast.makeText(MainActivity.this, "当前手机没有MP3文件", Toast.LENGTH_LONG).show();
                    return ;
                }
                if (mPlayService.isPlaying()) {
                    mPlayService.pause(); // 暂停
                    imgPlay
                            .setImageResource(android.R.drawable.ic_media_play);
                } else {
                    onPlay(mPlayService.resume()); // 播放
                }
                break;
            case R.id.iv_next_main:
                mPlayService.nextByMode(); // 上一曲
                break;
            case R.id.iv_pre_main:
                mPlayService.preByMode();
                break;
        }
    }

    /**
     * 播放时调用 主要设置显示当前播放音乐的信息
     *
     * @param position
     */
    private void onPlay(int position) {
        Bitmap bmp=null;
        if(!MusicUtils.sMusicList.isEmpty()){
            Music music = MusicUtils.sMusicList.get(position);

            tvTitle.setText(music.getTitle());
            tvArtist.setText(music.getArtist());
            Bitmap icon = MusicIconLoader.getInstance().load(music.getImage());
            imgPlayIcon.setImageBitmap(icon == null ? ImageTools
                    .scaleBitmap(R.mipmap.playbg) : ImageTools
                    .scaleBitmap(icon));
        }


        if (mPlayService.isPlaying()) {
            imgPlay
                    .setImageResource(android.R.drawable.ic_media_pause);
        } else {
            imgPlay
                    .setImageResource(android.R.drawable.ic_media_play);
        }
    }

    //Adapter适配器
    class MyPagerAdapter extends FragmentPagerAdapter{

        public MyPagerAdapter(FragmentManager fm) {
            super(fm);
        }
        //获取某一页
        @Override
        public Fragment getItem(int position) {
            Fragment fm=null;
            switch (position){
                case 0:
                    fm=new MyMusicFragment();
                    break;
                case 1:
                    fm=new TaoGeFragment();
                    break;
                case 2:
                    fm = new SearchFragment();
                    break;
                case 3:
                    fm = new RecommendFragment();
                    break;
            }
            return fm;
        }
        //获取item个数
        @Override
        public int getCount() {
            return 4;
        }
    }



    @Override
    protected void onDestroy() {
        super.onDestroy();

    }

    @Override
    public void onPublish(int progress) {

    }

    @Override
    public void onChange(int position) {
        onPlay(position);
    }


    boolean isExist;
    //回退键 触发此方法
    @Override
    public void onBackPressed() {
        if(!isExist){
            Toast.makeText(this,"再点击一次退出",Toast.LENGTH_SHORT).show();
            isExist=true;
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    isExist=false;
                }
            },5000);
        }else{
            finish();
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        rlMain.setBackgroundResource(App.getAppTheme());
        allowBindService();
    }
    @Override
    protected void onPause() {
        allowUnbindService();
        finish();
        super.onPause();
    }

    /*更新背景图片*/
    public  void setBackgroudTheme(int id){
        rlMain.setBackgroundResource(id);
    }


    /**
     * 播放音乐item
     *
     * @param position
     */
   /* private void play(int position) {
        int pos = this.mPlayService.play(position);
        onPlay(pos);
    }*/

    /**
     * 播放时，更新控制面板
     *
     * @param position
     */
    /*public void onPlay(int position) {
        if (MusicUtils.sMusicList.isEmpty() || position < 0){
            Toast.makeText(MainActivity.this, "当前手机没有MP3文件", Toast.LENGTH_LONG).show();
            return;
        }
        //设置进度条的总长度
       // mMusicProgress.setMax(mActivity.getPlayService().getDuration());
       // onItemPlay(position);

        Music music = MusicUtils.sMusicList.get(position);
        Bitmap icon = MusicIconLoader.getInstance().load(music.getImage());
        imgPlayIcon.setImageBitmap(icon == null ? ImageTools
                .scaleBitmap(R.mipmap.playbg) : ImageTools
                .scaleBitmap(icon));
        tvTitle.setText(music.getTitle());
        tvArtist.setText(music.getArtist());

        if (mPlayService.isPlaying()) {
            imgPlay.setImageResource(android.R.drawable.ic_media_pause);
        } else {
            imgPlay.setImageResource(android.R.drawable.ic_media_play);
        }
        //新启动一个线程更新通知栏，防止更新时间过长，导致界面卡顿！
        new Thread(){
            @Override
            public void run() {
                super.run();
                mPlayService.setRemoteViews();
            }
        }.start();
    }
*/
}









