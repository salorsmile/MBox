package salorsmile.lzh.mbox;

import android.content.Intent;
import android.graphics.Bitmap;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import salorsmile.lzh.application.App;
import salorsmile.lzh.entity.Music;
import salorsmile.lzh.service.PlayService;
import salorsmile.lzh.utils.ImageTools;
import salorsmile.lzh.utils.MusicIconLoader;

public class RecentPlayActivity extends BaseActivity {
    private static List<Music> musics = null;
    private ListView lvRecentPlay;
    private RelativeLayout rlRecentBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recent_play);
        setupView();
        RecentPlayActivity.MyAdapter adapter = new RecentPlayActivity.MyAdapter(musics);
        lvRecentPlay.setAdapter(adapter);
    }

    @Override
    public void onPublish(int progress) {

    }

    @Override
    public void onChange(int position) {

    }

    private void setupView() {
        musics = new ArrayList<Music>();
        musics = PlayService.recentPlayMusics;
        rlRecentBack = (RelativeLayout) findViewById(R.id.rl_recent_back);
        lvRecentPlay = (ListView) findViewById(R.id.lv_recent_play);
    }

    @Override
    protected void onResume() {
        super.onResume();
        rlRecentBack.setBackgroundResource(App.getAppTheme());
    }

    class MyAdapter extends BaseAdapter {
        private List<Music> musicsList;

        public MyAdapter(List<Music> musics) {
            if (musics == null) {
                musics = new ArrayList<Music>();
            }
            this.musicsList = musics;
        }

        @Override
        public int getCount() {
            return this.musicsList.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder = null;
            if (convertView == null) {
                convertView = View.inflate(RecentPlayActivity.this, R.layout.music_list_item, null);
                holder = new ViewHolder();
                holder.title = (TextView) convertView.findViewById(R.id.tv_music_list_title);
                holder.artist = (TextView) convertView.findViewById(R.id.tv_music_list_artist);
                holder.icon = (ImageView) convertView.findViewById(R.id.music_list_icon);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            Music music = musicsList.get(position);

            Bitmap icon = MusicIconLoader.getInstance().load(music.getImage());

            holder.icon.setImageBitmap(icon == null ? ImageTools
                    .scaleBitmap(R.mipmap.playbg) : ImageTools
                    .scaleBitmap(icon));
            holder.title.setText(music.getTitle());
            holder.artist.setText(music.getArtist());
            return convertView;
        }
    }

    static class ViewHolder {
        ImageView icon;
        TextView title;
        TextView artist;
    }
    @Override
    public void onBackPressed() {
        Intent intent = new Intent(RecentPlayActivity.this,MainActivity.class);
        startActivity(intent);
    }
}
