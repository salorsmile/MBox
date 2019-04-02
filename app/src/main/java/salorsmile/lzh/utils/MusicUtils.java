package salorsmile.lzh.utils;

import android.os.Environment;
import salorsmile.lzh.application.App;
import salorsmile.lzh.entity.Music;
import java.io.File;
import java.util.ArrayList;

public class MusicUtils {
	// 存放歌曲列表
	public static ArrayList<Music> sMusicList = new ArrayList<Music>();

	public static void initMusicList() {
		// 获取歌曲列表
		sMusicList.clear();
		sMusicList.addAll(LocalMusicUtils.queryMusic(getBaseDir()));
		//App.favoriteMusicList = sMusicList;
	}

	/**
	 * 获取内存卡根
	 * @return
	 */
	public static String getBaseDir() {
		String dir = null;
		if (!Environment.getExternalStorageState().equals(Environment.MEDIA_UNMOUNTED)) {
			dir = Environment.getExternalStorageDirectory() + File.separator;
		} else {
			dir = App.sContext.getFilesDir() + File.separator;
		}

		return dir;
	}

	/**
	 * 获取应用程序使用的本地目录
	 * @return
	 */
	public static String getAppLocalDir() {
		String dir = null;

		if (!Environment.getExternalStorageState().equals(Environment.MEDIA_UNMOUNTED)) {
			dir = Environment.getExternalStorageDirectory() + File.separator
					+ "mbox" + File.separator;
		} else {
			dir = App.sContext.getFilesDir() + File.separator + "mbox" + File.separator;
		}

		return mkdir(dir);
	}

	/**
	 * 获取音乐存放目录
	 * @return
	 */
	public static String getMusicDir() {
		String musicDir = getAppLocalDir() + "music" + File.separator;
		return mkdir(musicDir);
	}

	/**
	 * 获取歌词存放目录
	 * 
	 * @return
	 */
	public static String getLrcDir() {
		String lrcDir = getAppLocalDir() + "lrc" + File.separator;
		return mkdir(lrcDir);
	}

	/**
	 * 创建文件夹
	 * @param dir
	 * @return
	 */
	public static String mkdir(String dir) {
		File f = new File(dir);
		if (!f.exists()) {
			for (int i = 0; i < 5; i++) {
				if(f.mkdirs()) return dir;
			}
			return null;
		}
		
		return dir;
	}
}
