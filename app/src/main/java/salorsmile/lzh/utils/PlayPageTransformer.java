package salorsmile.lzh.utils;

import android.support.v4.view.ViewPager.PageTransformer;
import android.view.View;

import salorsmile.lzh.application.App;

/**
 * 功能：
 * 实现viewpager的界面的切换动画
 */
public class PlayPageTransformer implements PageTransformer {

	@Override
	public void transformPage(View view, float position) {
		if(position < -1) { // [-Infinity,-1) 左边看不见了
			view.setAlpha(0.0f);
		}else if(position <= 0) { // [-1,0]左边向中间 或 中间向左边
			view.setAlpha(1 + position);
			view.setTranslationX(App.sScreenWidth * (-position));
		}else if(position <= 1) { // (0,1] 右边向中间 或 中间向右边
			view.setAlpha(1 + position);
//			view.setTranslationX(mScreenWidth * -position);
		}else if(position > 1) { // (1,+Infinity] 右边看不见了
			view.setAlpha(1.0f);
		}
	}
}
