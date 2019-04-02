package salorsmile.lzh.fragment;


import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import salorsmile.lzh.adapter.SearchResultAdapter;
import salorsmile.lzh.entity.Music;
import salorsmile.lzh.entity.SearchResult;
import salorsmile.lzh.mbox.R;
import salorsmile.lzh.utils.MusicUtils;

/**
 * A simple {@link Fragment} subclass.
 */

public class SearchFragment extends Fragment {

private LinearLayout mSearchShowLinearLayout;
private LinearLayout mSearchLinearLayout;
private View view;
private EditText editText;
private ListView listView;
private ListView musicResult;
private Set<String> historySet=null;
private String content;
private List<String> lists=new ArrayList<String>();
private ArrayList<SearchResult> musicSearchResult;
private ImageButton searchBtn;
private Button deleteBtn;
private RelativeLayout rlMiddle;
public SearchFragment() {
        // Required empty public constructor
        }


@Override
public View onCreateView(LayoutInflater inflater, ViewGroup container,
        Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view=inflater.inflate(R.layout.fragment_search, container, false);
        setupView();

        return view;
        }

private void setupView() {
        mSearchShowLinearLayout = (LinearLayout) view.
        findViewById(R.id.ll_search_btn_container);
        mSearchLinearLayout = (LinearLayout) view.
        findViewById(R.id.ll_search_container);
        deleteBtn= (Button) view.findViewById(R.id.deleteBtn);
        rlMiddle= (RelativeLayout) view.findViewById(R.id.rl_middle);
        historySet=new HashSet<String>();
        searchBtn= (ImageButton) view.findViewById(R.id.searchBtn);
        editText= (EditText) view.findViewById(R.id.search);
        musicSearchResult=new ArrayList<SearchResult>();
        listView= (ListView) view.findViewById(R.id.historyList);
        musicResult= (ListView) view.findViewById(R.id.musicResult);
        ShowHistoryList();
        mSearchShowLinearLayout.setOnClickListener(new View.OnClickListener() {
@Override
public void onClick(View view) {
        mSearchShowLinearLayout.setVisibility(View.GONE);
        mSearchLinearLayout.setVisibility(View.VISIBLE);
        }
        });
        mSearchLinearLayout.setOnClickListener(new View.OnClickListener() {
@Override
public void onClick(View view) {
        mSearchShowLinearLayout.setVisibility(View.VISIBLE);
        mSearchLinearLayout.setVisibility(View.GONE);
        }
        });
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
@Override
public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        content=lists.get(i);
        searchMusic();
        }
        });
        searchBtn.setOnClickListener(new View.OnClickListener() {
@Override
public void onClick(View view) {
        content=editText.getText().toString();
        if(content!="") {
        searchMusic();
        storeHistoryList();
        }
        editText.setText("");
        }
        });
        deleteBtn.setOnClickListener(new View.OnClickListener() {
@Override
public void onClick(View view) {
        clearHistoryList();
        listView.setAdapter(null);
        historySet.clear();
        }
        });


        }

private void clearHistoryList() {
        SharedPreferences spf= view.getContext().getSharedPreferences("history",view.getContext().MODE_PRIVATE);
        SharedPreferences.Editor editor = spf.edit();
        editor.remove("historys");
        editor.commit();
        }

private void searchMusic()
        {
        musicSearchResult.clear();
        MusicUtils.initMusicList();
        List<Music> musics= MusicUtils.sMusicList;
        for(int i=0;i<musics.size();i++)
        {
        Music music=musics.get(i);
        SearchResult sr=new SearchResult();
        if(music.getTitle().contains(content) || music.getArtist().contains(content) || music.getArtist().contains(content.toLowerCase()) || music.getArtist().contains(content.toLowerCase())
        || music.getTitle().contains(content.toUpperCase()) || music.getArtist().contains(content.toUpperCase())      )
        {
        sr.setAlbum(music.getImage());
        sr.setArtist(music.getArtist());
        sr.setMusicName(music.getTitle());
        sr.setUrl(music.getUri());
        musicSearchResult.add(sr);
        }
        }
        SearchResultAdapter myadapter=new SearchResultAdapter(musicSearchResult);
        listView.setVisibility(View.GONE);
        rlMiddle.setVisibility(View.GONE);
        musicResult.setVisibility(View.VISIBLE);
        musicResult.setAdapter(myadapter);
        }
private void ShowHistoryList()
        {
        lists.clear();
        SharedPreferences spf= view.getContext().getSharedPreferences("history",view.getContext().MODE_PRIVATE);
        historySet=spf.getStringSet("historys",historySet);
        if(historySet!=null) {
        if (historySet.size() > 0) {
        Iterator it = historySet.iterator();
        while (it.hasNext()) {
        String text = (String) it.next();
        lists.add(text);
        }
        }
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(view.getContext(), android.R.layout.simple_list_item_1, lists);
        musicResult.setVisibility(View.GONE);
        rlMiddle.setVisibility(View.VISIBLE);
        listView.setVisibility(View.VISIBLE);
        listView.setAdapter(adapter);
        }
private void storeHistoryList()
        {
        SharedPreferences spf= view.getContext().getSharedPreferences("history",view.getContext().MODE_PRIVATE);
        SharedPreferences.Editor editor = spf.edit();
        historySet=spf.getStringSet("historys",historySet);
        historySet.add(content);
        editor.putStringSet("historys",historySet);
        editor.commit();
        }

        }
