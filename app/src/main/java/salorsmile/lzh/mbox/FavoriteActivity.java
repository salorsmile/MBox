package salorsmile.lzh.mbox;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import salorsmile.lzh.adapter.MusicListAdapter;
import salorsmile.lzh.application.App;
import salorsmile.lzh.entity.Music;
import salorsmile.lzh.utils.ImageTools;
import salorsmile.lzh.utils.MusicIconLoader;
import salorsmile.lzh.utils.MusicUtils;

public class FavoriteActivity extends Activity {
    private static List<Music> musics=null;
    private ListView lvFavorite;
    private TextView tvNomusicTip;
    private ImageButton imbFavoriteBack;
    private RelativeLayout rlFavoriteBack;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorite);
        setupView();

        MyAdapter adapter=new MyAdapter(musics);
        lvFavorite.setAdapter(adapter);

        lvFavorite.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                App.favoriteMusicList.remove(position);
                startActivity(new Intent(FavoriteActivity.this,FavoriteActivity.class));
                finish();
                //Toast.makeText(FavoriteActivity.this,App.favoriteMusicList.get(position).getTitle()+"-"+position,Toast.LENGTH_SHORT).show();
                return false;
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        rlFavoriteBack.setBackgroundResource(App.getAppTheme());
    }

    private void setupView() {
        rlFavoriteBack = (RelativeLayout) findViewById(R.id.rl_favorite_back);
        imbFavoriteBack= (ImageButton) findViewById(R.id.imb_favorite_back);
        tvNomusicTip= (TextView) findViewById(R.id.tv_nomusic_tip);
        musics=new ArrayList<Music>();
        musics=App.favoriteMusicList;
        //getFavoriteMusics();
        lvFavorite= (ListView) findViewById(R.id.lv_favorite);
        if(musics.size()==0){
            tvNomusicTip.setVisibility(View.VISIBLE);
        }
        imbFavoriteBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
    public void getFavoriteMusics(){
        List<Music> lists=MusicUtils.sMusicList;
        for(int i=0;i<lists.size();i++){
            Log.v("TAG","收藏界面  此歌曲状态"+lists.get(i).getIsFavorite());
            if(lists.get(i).getIsFavorite()==1){
                Music music=new Music();
                //Log.v("TAG","添加歌曲");
                music=lists.get(i);
                musics.add(music);
                //Log.v("TAG","添加成功");
            }
        }
        Log.v("TAG","收藏界面 获取歌曲完毕"+musics.size());
    }
    class MyAdapter extends BaseAdapter{
        private List<Music> musicsList;
        public MyAdapter(List<Music> musics) {
            if(musics==null){
                Log.v("TAG","当前没有收藏歌曲");
                musics=new ArrayList<Music>();
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
            final ViewHolder holder ;
            if(convertView == null) {
                convertView = View.inflate(FavoriteActivity.this, R.layout.music_list_item, null);
                holder = new ViewHolder();
                holder.title = (TextView) convertView.findViewById(R.id.tv_music_list_title);
                holder.artist = (TextView) convertView.findViewById(R.id.tv_music_list_artist);
                holder.icon = (ImageView) convertView.findViewById(R.id.music_list_icon);
                convertView.setTag(holder);
            }else {
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
        Intent intent = new Intent(FavoriteActivity.this,MainActivity.class);
        startActivity(intent);
    }
}
