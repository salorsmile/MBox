package salorsmile.lzh.mbox;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.ShapeDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.MarginLayoutParams;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import salorsmile.lzh.application.App;
import salorsmile.lzh.entity.Music;
import salorsmile.lzh.ui.CDView;
import salorsmile.lzh.ui.LrcView;
import salorsmile.lzh.ui.PagerIndicator;
import salorsmile.lzh.utils.Global;
import salorsmile.lzh.utils.ImageTools;
import salorsmile.lzh.utils.MusicIconLoader;
import salorsmile.lzh.utils.MusicUtils;
import salorsmile.lzh.utils.PlayBgShape;
import salorsmile.lzh.utils.PlayPageTransformer;

import static salorsmile.lzh.service.PlayService.ORDER_PLAY;
import static salorsmile.lzh.service.PlayService.RANDOM_PLAY;
import static salorsmile.lzh.service.PlayService.SINGLE_PLAY;

public class PlayActivity extends BaseActivity implements OnClickListener {

	private LinearLayout mPlayContainer;
	private ImageView mPlayBackImageView; // back button
	private TextView mMusicTitle; // music title
	private ViewPager mViewPager; // cd or lrc
	private CDView mCdView; // cd
	private SeekBar mPlaySeekBar; // seekbar
	private ImageButton mStartPlayButton; // start or pause
	private TextView mSingerTextView; // singer
	private LrcView mLrcViewOnFirstPage; // single line lrc
	private LrcView mLrcViewOnSecondPage; // 7 lines lrc
	private PagerIndicator mPagerIndicator; // indicator

	private ImageButton imbPlayModel; //播放模式按钮
	private TextView tvCurrentTime;//歌曲当前播放时间
	private TextView tvTotalTime;//歌曲总时间

	private ImageButton imbFavorite; // 收藏歌曲   智
	private BroadcastReceiver receiver; //广播接收器  智

	private static final int UPDATE_TIME = 0x1;

	/*=============*/

	private static MyHandler myHandler;

	/**/

	// cd view and lrc view
	private ArrayList<View> mViewPagerContent = new ArrayList<View>(2);

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.play_activity_layout);
		setupViews();

		imbPlayModel.setOnClickListener(this);


		myHandler=new MyHandler(this);

		receiver=new MyserviceReceiver();
		IntentFilter filter=new IntentFilter();
		filter.addAction("ModifyFavoriteIcon");
		registerReceiver(receiver,filter);
	}

	/**
	 * 初始化view
	 */
	private void setupViews() {
		tvTotalTime = (TextView) findViewById(R.id.tv_total_time);
		tvCurrentTime = (TextView) findViewById(R.id.tv_current_time);
		imbPlayModel = (ImageButton) findViewById(R.id.imb_play_model);
		mPlayContainer = (LinearLayout) findViewById(R.id.ll_play_container);
		mPlayBackImageView = (ImageView) findViewById(R.id.iv_play_back);
		mMusicTitle = (TextView) findViewById(R.id.tv_music_title);
		mViewPager = (ViewPager) findViewById(R.id.vp_play_container);
		mPlaySeekBar = (SeekBar) findViewById(R.id.sb_play_progress);
		mStartPlayButton = (ImageButton) findViewById(R.id.ib_play_start);
		mPagerIndicator = (PagerIndicator) findViewById(R.id.pi_play_indicator);

		imbFavorite=(ImageButton)findViewById(R.id.imb_favorite);  //智
		imbFavorite.setOnClickListener(this);//智

		// 动态设置seekbar的margin
		MarginLayoutParams p = (MarginLayoutParams) mPlaySeekBar
				.getLayoutParams();
		p.leftMargin = (int) (App.sScreenWidth * 0.1);
		p.rightMargin = (int) (App.sScreenWidth * 0.1);

		mPlaySeekBar.setOnSeekBarChangeListener(mSeekBarChangeListener);

		initViewPagerContent();
		// 设置viewpager的切换动画
		mViewPager.setPageTransformer(true, new PlayPageTransformer());
		mPagerIndicator.create(mViewPagerContent.size());
		mViewPager.setOnPageChangeListener(mPageChangeListener);
		mViewPager.setAdapter(mPagerAdapter);

		mPlayBackImageView.setOnClickListener(this);
	}

	@Override
	protected void onResume() {
		super.onResume();
		allowBindService();
	}
	@Override
	protected void onPause() {
		allowUnbindService();
		finish();
		super.onPause();
	}
	private OnPageChangeListener mPageChangeListener =
			new OnPageChangeListener() {
		@Override
		public void onPageSelected(int position) {
			if (position == 0) {
				if (mPlayService.isPlaying())
					mCdView.start();
			} else {
				mCdView.pause();
			}
			mPagerIndicator.current(position);
		}

		@Override
		public void onPageScrolled(int arg0, float arg1, int arg2) {
		}

		@Override
		public void onPageScrollStateChanged(int arg0) {
		}
	};

	/**
	 * 拖动进度条
	 */
	private SeekBar.OnSeekBarChangeListener mSeekBarChangeListener =
			new SeekBar.OnSeekBarChangeListener() {
		@Override
		public void onProgressChanged(SeekBar seekBar, int progress,
				boolean fromUser) {

		}

		@Override
		public void onStartTrackingTouch(SeekBar seekBar) {

		}

		@Override
		public void onStopTrackingTouch(SeekBar seekBar) {
			int progress = seekBar.getProgress();
			mPlayService.seek(progress);
			mLrcViewOnFirstPage.onDrag(progress);
			mLrcViewOnSecondPage.onDrag(progress);
		}
	};

	private PagerAdapter mPagerAdapter = new PagerAdapter() {
		@Override
		public int getCount() {
			return mViewPagerContent.size();
		}

		@Override
		public boolean isViewFromObject(View view, Object obj) {
			return view == obj;
		}

		/**
		 * 该方法是PagerAdapter的预加载方法，系统调用 当显示第一个界面时，
		 * 第二个界面已经预加载，此时调用的就是该方法。
		 */
		@Override
		public Object instantiateItem(ViewGroup container, int position) {
			container.addView(mViewPagerContent.get(position));
			return mViewPagerContent.get(position);
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			((ViewPager) container).removeView((View) object);
		}
	};

	/**
	 * 初始化viewpager的内容
	 */
	private void initViewPagerContent() {
		View cd = View.inflate(this, R.layout.play_pager_item_1, null);
		mCdView = (CDView) cd.findViewById(R.id.play_cdview);
		mSingerTextView = (TextView) cd.findViewById(R.id.play_singer);
		mLrcViewOnFirstPage = (LrcView) cd.findViewById(R.id.play_first_lrc);

		View lrcView = View.inflate(this, R.layout.play_pager_item_2, null);
		mLrcViewOnSecondPage = (LrcView) lrcView
				.findViewById(R.id.play_first_lrc_2);

		mViewPagerContent.add(cd);
		mViewPagerContent.add(lrcView);
	}

	@SuppressWarnings("deprecation")
	private void setBackground(int position) {
		if(position==0){
			return ;
		}
		Bitmap bgBitmap=null;
		if(MusicUtils.sMusicList.size()!=0){
			Music currentMusic = MusicUtils.sMusicList.get(position);
			bgBitmap = MusicIconLoader.getInstance().load(
					currentMusic.getImage());
		}
		if (bgBitmap == null) {
			bgBitmap = BitmapFactory.decodeResource(getResources(),
					R.mipmap.bit_decode);
		}
		mPlayContainer.setBackgroundDrawable(
				new ShapeDrawable(new PlayBgShape(bgBitmap)));
	}

	/**
	 * 上一曲
	 * 
	 * @param view
	 */
	public void pre(View view) {
		mPlayService.preByMode(); // 上一曲
	}

	/**
	 * 播放 or 暂停
	 * 
	 * @param view
	 */
	public void play(View view) {
		if (MusicUtils.sMusicList.isEmpty()) {
			Toast.makeText(PlayActivity.this, "当前手机没有MP3文件", Toast.LENGTH_LONG).show();
			return ;
		}
		if (mPlayService.isPlaying()) {
			mPlayService.pause(); // 暂停
			mCdView.pause();
			mStartPlayButton
					.setImageResource(R.mipmap.player_btn_play_normal);
		} else {
			onPlay(mPlayService.resume()); // 播放
		}
	}

	/**
	 * 上一曲
	 * 
	 * @param view
	 */
	public void next(View view) {
		mPlayService.nextByMode(); // 上一曲
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
			
			mMusicTitle.setText(music.getTitle());
			mSingerTextView.setText(music.getArtist());
			mPlaySeekBar.setMax(music.getLength());
			bmp = MusicIconLoader.getInstance().load(music.getImage());
		}
		if (bmp == null)
			bmp = BitmapFactory.decodeResource(getResources(),
					R.mipmap.playbg);
		mCdView.setImage(ImageTools.scaleBitmap(bmp,
				(int) (App.sScreenWidth * 0.8)));

		if (mPlayService.isPlaying()) {
			mCdView.start();
			mStartPlayButton
					.setImageResource(R.mipmap.player_btn_pause_normal);
		} else {
			mCdView.pause();
			mStartPlayButton
					.setImageResource(R.mipmap.player_btn_play_normal);
		}
	}

	private void setLrc(int position) {
		if(MusicUtils.sMusicList.size()!=0){
			Music music = MusicUtils.sMusicList.get(position);
			String lrcPath = MusicUtils.getLrcDir() + music.getTitle() + ".lrc";
			mLrcViewOnFirstPage.setLrcPath(lrcPath);
			mLrcViewOnSecondPage.setLrcPath(lrcPath);
		}
	}
	private void setModelImg() {
		switch (mPlayService.getPlay_mode()){
			case ORDER_PLAY://顺序播放
				imbPlayModel.setImageResource(R.mipmap.order_play);
				//imageView2_play_pause.setTag(ORDER_PLAY);
				break;
			case RANDOM_PLAY://随机播放
				imbPlayModel.setImageResource(R.mipmap.random_play);
				//imageView2_play_pause.setTag(RANDOM_PLAY);
				break;
			case SINGLE_PLAY://单曲循环
				imbPlayModel.setImageResource(R.mipmap.single_play);
				//imageView2_play_pause.setTag(SINGLE_PLAY);
				break;
			default:
				break;
		}

	}
	@Override
	public void onPublish(int progress) {
		mPlaySeekBar.setProgress(progress);
		if (mLrcViewOnFirstPage.hasLrc())
			mLrcViewOnFirstPage.changeCurrent(progress);
		if (mLrcViewOnSecondPage.hasLrc())
			mLrcViewOnSecondPage.changeCurrent(progress);

		Message msg = myHandler.obtainMessage(UPDATE_TIME);//用于更新已经播放时间
		msg.arg1 = progress;//用于更新已经播放时间
		myHandler.sendMessage(msg);//
	}

	@Override
	public void onChange(int position) {
        Music music = MusicUtils.sMusicList.get(position);
        tvTotalTime.setText(Global.setTimes(music.getLength()));
		setBackground(position);
		onPlay(position);
		setLrc(position);
		setModelImg();
	}
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.iv_play_back:
				finish();
				//startActivity(new Intent(PlayActivity.this,FavoriteActivity.class));
				break;
			case R.id.imb_play_model:
				switch (mPlayService.getPlay_mode()) {
					case ORDER_PLAY:
						imbPlayModel.setImageResource(R.mipmap.random_play);
						//imageView1_play_mode.setTag(RANDOM_PLAY);
						mPlayService.setPlay_mode(RANDOM_PLAY);
						Toast.makeText(App.sContext, "随机播放", Toast.LENGTH_SHORT).show();
						break;
					case RANDOM_PLAY:
						imbPlayModel.setImageResource(R.mipmap.single_play);
						//imageView1_play_mode.setTag(SINGLE_PLAY);
						mPlayService.setPlay_mode(SINGLE_PLAY);
						Toast.makeText(App.sContext, "单曲循环", Toast.LENGTH_SHORT).show();
						break;
					case SINGLE_PLAY:
						imbPlayModel.setImageResource(R.mipmap.order_play);
						//imageView1_play_mode.setTag(ORDER_PLAY);
						mPlayService.setPlay_mode(ORDER_PLAY);
						Toast.makeText(App.sContext, "顺序播放", Toast.LENGTH_SHORT).show();
						break;
				}
				break;
			case R.id.imb_favorite:
				//获取当前歌曲的位置
				int i=mPlayService.mPlayingPosition;
				//Log.i("TAG","activity里  当前位置"+i);
				if(!isFavorite(i)){  //如果当前歌曲没有被收藏
					//添加当前歌曲到收藏列表
					App.favoriteMusicList.add(MusicUtils.sMusicList.get(i));
					Log.v("TAG","添加成功"+App.favoriteMusicList.size());

					//Log.v("TAG","当前播放歌曲的ID为"+MusicUtils.sMusicList.get(i).getId()+"名称"+MusicUtils.sMusicList.get(i).getTitle());
					//MusicUtils.sMusicList.get(i).setIsFavorite(1);
					//修改收藏图标
					imbFavorite.setImageResource(R.mipmap.favroite_1);
					//Log.v("TAG","activity里 当前歌曲"+MusicUtils.sMusicList.get(i).getTitle()+"收藏状态："+MusicUtils.sMusicList.get(i).getIsFavorite());
					Toast.makeText(PlayActivity.this,"已收藏",Toast.LENGTH_SHORT).show();
				}else if(isFavorite(i)){
					imbFavorite.setImageResource(R.mipmap.favroite_0);
					Toast.makeText(PlayActivity.this,"取消收藏",Toast.LENGTH_SHORT).show();
					for(int i1=0;i1<App.favoriteMusicList.size();i1++){
						if(MusicUtils.sMusicList.get(i).getId()==App.favoriteMusicList.get(i1).getId()){
							App.favoriteMusicList.remove(i1);
						}
					}
				}
				break;
			default:
				break;
			}
	}
	//此方法判断 当前播放歌曲是否收藏   返回true代表收藏
	public boolean isFavorite(int i){
		if(App.favoriteMusicList.size()==0){
			return false;
		}
		int id=MusicUtils.sMusicList.get(i).getId(); //当前播放歌曲的ID
		Log.v("TAG1","当前歌曲id为"+id+"当前收藏列表个数"+App.favoriteMusicList.size());
		for(int j=0;j<App.favoriteMusicList.size();j++){
			if(App.favoriteMusicList.get(j).getId()==id){
				return true;
			}
		}
		return false;
	}
	static class MyHandler extends Handler {
		private PlayActivity playActivity;
		public MyHandler(PlayActivity playActivity){
			this.playActivity = playActivity;
		}
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			if (playActivity!=null){
				switch (msg.what){
					case UPDATE_TIME://更新时间(已经播放时间)
						playActivity.tvCurrentTime.setText(Global.setTimes(msg.arg1));
						break;
				}
			}
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		unregisterReceiver(receiver);
	}
	class MyserviceReceiver extends BroadcastReceiver{
		@Override
		public void onReceive(Context context, Intent intent) {
			String action=intent.getAction();
			if("ModifyFavoriteIcon".equals(action)){
				int mPlayingPosition=intent.getIntExtra("mPlayingPosition",0);
				if(isFavorite(mPlayingPosition)){
					imbFavorite.setImageResource(R.mipmap.favroite_1);
				}else{
					imbFavorite.setImageResource(R.mipmap.favroite_0);
				}
			}
		}
	}

	@Override
	public void onBackPressed() {
		Intent intent = new Intent(PlayActivity.this,MainActivity.class);
		startActivity(intent);
	}
}