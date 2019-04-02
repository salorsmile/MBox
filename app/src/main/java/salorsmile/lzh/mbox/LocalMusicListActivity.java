package salorsmile.lzh.mbox;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.PopupWindow.OnDismissListener;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import salorsmile.lzh.application.App;
import salorsmile.lzh.fragment.LocalFragment;
import salorsmile.lzh.fragment.NetSearchFragment;
import salorsmile.lzh.service.DownloadService;
import salorsmile.lzh.service.PlayService;
import salorsmile.lzh.ui.Indicator;
import salorsmile.lzh.ui.ScrollRelativeLayout;
import salorsmile.lzh.utils.L;
import salorsmile.lzh.utils.MusicUtils;

public class LocalMusicListActivity extends BaseActivity implements OnClickListener {

	private static final String TAG = LocalMusicListActivity.class.getSimpleName();
	private ScrollRelativeLayout mMainContainer;
	private Indicator mIndicator;
	
	private TextView mLocalTextView;
	private TextView mSearchTextView;
	private ViewPager mViewPager;
	private View mPopshownView;
	/*弹窗*/
	private PopupWindow mPopupWindow;
	
	private ArrayList<Fragment> mFragments = new ArrayList<Fragment>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_localmusiclist);
		registerReceiver();
		initFragments();
		setupViews();
	}
	/**
	 * 注册广播接收器
	 * 在下载歌曲完成或删除歌曲时，更新歌曲列表
	 */
	private void registerReceiver() {
		IntentFilter filter = new IntentFilter( Intent.ACTION_MEDIA_SCANNER_STARTED);
		filter.addAction(Intent.ACTION_MEDIA_SCANNER_FINISHED);
		filter.addDataScheme("file");
		registerReceiver(mScanSDCardReceiver, filter);
	}
	
	private void setupViews() {
		mMainContainer = (ScrollRelativeLayout) findViewById(R.id.rl_main_container);
		mIndicator = (Indicator) findViewById(R.id.main_indicator);
		mLocalTextView = (TextView) findViewById(R.id.tv_main_local);
		mSearchTextView = (TextView) findViewById(R.id.tv_main_remote);
		mViewPager = (ViewPager) findViewById(R.id.vp_main_container);
		mPopshownView = findViewById(R.id.view_pop_show);
		
		mViewPager.setAdapter(mPagerAdapter);
		mViewPager.setOnPageChangeListener(mPageChangeListener);
		
		mLocalTextView.setOnClickListener(this);
		mSearchTextView.setOnClickListener(this);
		
		selectTab(0);
	}
	
	private OnPageChangeListener mPageChangeListener = new OnPageChangeListener() {
		@Override
		public void onPageSelected(int position) {
			selectTab(position);
			mMainContainer.showIndicator();
		}
		
		@Override
		public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
			mIndicator.scroll(position, positionOffset);
		}
		
		@Override
		public void onPageScrollStateChanged(int position) {
			
		}
	};
	
	private FragmentPagerAdapter mPagerAdapter =
			new FragmentPagerAdapter(getSupportFragmentManager()) {
		@Override
		public int getCount() {
			return mFragments.size();
		}
		
		@Override
		public Fragment getItem(int position) {
			return mFragments.get(position);
		}
	};
	
	/**
	 * 切换导航indicator
	 * @param index
	 */
	private void selectTab(int index) {
		switch (index) {
		case 0:
			mLocalTextView.setTextColor(getResources().getColor(R.color.main));
			mSearchTextView.setTextColor(getResources().getColor(R.color.main_dark));
			break;
		case 1:
			mLocalTextView.setTextColor(getResources().getColor(R.color.main_dark));
			mSearchTextView.setTextColor(getResources().getColor(R.color.main));
			break;
		}
	}
	
	private void initFragments() {
		/*初始化页面碎片、本地音乐和网络搜索*/
		LocalFragment localFragment = new LocalFragment();
		NetSearchFragment netSearchFragment = new NetSearchFragment();
		
		mFragments.add(localFragment);
		mFragments.add(netSearchFragment);
	}
	
	/**
	 * 获取音乐播放服务
	 * @return
	 */
	public PlayService getPlayService() {
		return mPlayService;
	}
	
	public void hideIndicator() {
		mMainContainer.hideIndicator();
	}
	
	public void showIndicator() {
		mMainContainer.showIndicator();
	}
	
	public void onPopupWindowShown() {
		mPopshownView.startAnimation(AnimationUtils.loadAnimation(this, R.anim.layer_show_anim));
		mPopshownView.setVisibility(View.VISIBLE);
	}
	
	public void onPopupWindowDismiss() {
		mPopshownView.startAnimation(AnimationUtils.loadAnimation(this, R.anim.layer_gone_anim));
		mPopshownView.setVisibility(View.GONE);
	}
	
	@Override
	public void onPublish(int progress) {
		// 如果当前显示的fragment是音乐列表fragment
		// 则调用fragment的setProgress设置进度
		if(mViewPager.getCurrentItem() == 0) {
			((LocalFragment)mFragments.get(0)).setProgress(progress);
		}
	}

	@Override
	public void onChange(int position) {
		// 如果当前显示的fragment是音乐列表fragment
		// 则调用fragment的setProgress切换歌曲
		if(mViewPager.getCurrentItem() == 0) {
			((LocalFragment)mFragments.get(0)).onPlay(position);
		}
	}
	
	private void onShowMenu() {
		onPopupWindowShown();
		if(mPopupWindow == null) {
			View view = View.inflate(this, R.layout.exit_pop_layout, null);
			View shutdown = view.findViewById(R.id.tv_pop_shutdown);
			View exit = view.findViewById(R.id.tv_pop_exit);
			View cancel = view.findViewById(R.id.tv_pop_cancel);
			
			// 不需要共享变量， 所以放这没事
			shutdown.setOnClickListener(this);
			exit.setOnClickListener(this);
			cancel.setOnClickListener(this);
			
			mPopupWindow = new PopupWindow(view,
					LinearLayout.LayoutParams.MATCH_PARENT,
					LinearLayout.LayoutParams.WRAP_CONTENT, true);
			mPopupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
			mPopupWindow.setAnimationStyle(R.style.popwin_anim);
			mPopupWindow.setFocusable(true);
			mPopupWindow.setOnDismissListener(new OnDismissListener() {
				@Override
				public void onDismiss() {
					onPopupWindowDismiss();
				}
			});
		}
		
		mPopupWindow.showAtLocation(getWindow().getDecorView(), 
				Gravity.BOTTOM|Gravity.CENTER_HORIZONTAL, 0, 0);
	}
	
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.tv_main_local:
			mViewPager.setCurrentItem(0);
			break;
		case R.id.tv_main_remote:
			mViewPager.setCurrentItem(1);
			break;
		case R.id.tv_pop_exit:
			stopService(new Intent(this, PlayService.class));
			stopService(new Intent(this, DownloadService.class));
		case R.id.tv_pop_shutdown:
			finish();
		case R.id.tv_pop_cancel:
			if(mPopupWindow != null && mPopupWindow.isShowing()) mPopupWindow.dismiss();
			onPopupWindowDismiss();
	 		break;
		}
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(keyCode == KeyEvent.KEYCODE_MENU) {
			onShowMenu();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}
	
	@Override
	protected void onDestroy() {
		unregisterReceiver(mScanSDCardReceiver);
		super.onDestroy();
	}
	
	private BroadcastReceiver mScanSDCardReceiver = new BroadcastReceiver() {
		public void onReceive(Context context, Intent intent) {
			L.l(TAG, "mScanSDCardReceiver---->onReceive()");
			if(intent.getAction().equals(Intent.ACTION_MEDIA_SCANNER_FINISHED)) {
				MusicUtils.initMusicList();
				((LocalFragment)mFragments.get(0)).onMusicListChanged();
			}
		}
	};

	@Override
	protected void onResume() {
		super.onResume();
		mMainContainer.setBackgroundResource(App.getAppTheme());
	}

	@Override
	public void onBackPressed() {
		Intent intent = new Intent(LocalMusicListActivity.this,MainActivity.class);
		startActivity(intent);
	}
}