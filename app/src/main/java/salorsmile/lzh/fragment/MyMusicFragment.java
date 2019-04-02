package salorsmile.lzh.fragment;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.RelativeLayout;

import salorsmile.lzh.mbox.FavoriteActivity;
import salorsmile.lzh.mbox.LocalMusicListActivity;
import salorsmile.lzh.mbox.PlayActivity;
import salorsmile.lzh.mbox.R;
import salorsmile.lzh.mbox.RecentPlayActivity;

/**
 * A simple {@link Fragment} subclass.
 */
public class MyMusicFragment extends Fragment {
    //声明控件对象
    //private Activity mActivity = getActivity();
    private RelativeLayout rlOneOne,rlOneTwo,rlOneThree;
    private RelativeLayout rlTwoOne,rlTwoTwo,rlTwoThree;
    private RelativeLayout rlThreeOne,rlThreeTwo,rlThreeThree;
    private RelativeLayout rlFourOne,rlFourTwo,rlFourThree;
    private View view;

    public MyMusicFragment() {
        // Required empty public constructor
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view=inflater.inflate(R.layout.fragment_my_music, container, false);
        setupView();
        //事件监听的事件
        addListener();
        //获取手机屏幕的宽度   getActivity()相当于MainActivity.this
        WindowManager wm=getActivity().getWindowManager();
        int width=wm.getDefaultDisplay().getWidth()-10;
        //给每一个Relative设置宽度和高度
        ViewGroup.LayoutParams lpOneOne=rlOneOne.getLayoutParams();
        lpOneOne.width=width/3;
        lpOneOne.height=width/3;

        ViewGroup.LayoutParams lpOneTwo=rlOneTwo.getLayoutParams();
        lpOneTwo.width=width/3;
        lpOneTwo.height=width/3;

        ViewGroup.LayoutParams lpOneThree=rlOneThree.getLayoutParams();
        lpOneThree.width=width/3;
        lpOneThree.height=width/3;
        //第2行
        ViewGroup.LayoutParams lpTwoOne=rlTwoOne.getLayoutParams();
        lpTwoOne.width=width/3;
        lpTwoOne.height=width/3;

        ViewGroup.LayoutParams lpTwoTwo=rlTwoTwo.getLayoutParams();
        lpTwoTwo.width=width/3;
        lpTwoTwo.height=width/3;

        ViewGroup.LayoutParams lpTwoThree=rlTwoThree.getLayoutParams();
        lpTwoThree.width=width/3;
        lpTwoThree.height=width/3;
        //第3行
        ViewGroup.LayoutParams lpThreeOne=rlThreeOne.getLayoutParams();
        lpThreeOne.width=width/3;
        lpThreeOne.height=width/3;

        ViewGroup.LayoutParams lpThreeTwo=rlThreeTwo.getLayoutParams();
        lpThreeTwo.width=width/3;
        lpThreeTwo.height=width/3;

        ViewGroup.LayoutParams lpThreeThree=rlThreeThree.getLayoutParams();
        lpThreeThree.width=width/3;
        lpThreeThree.height=width/3;
        //第4行
        ViewGroup.LayoutParams lpFourOne=rlFourOne.getLayoutParams();
        lpFourOne.width=width/3;
        lpFourOne.height=width/3;

        ViewGroup.LayoutParams lpFourTwo=rlFourTwo.getLayoutParams();
        lpFourTwo.width=width/3;
        lpFourTwo.height=width/3;

        ViewGroup.LayoutParams lpFourThree=rlFourThree.getLayoutParams();
        lpFourThree.width=width/3;
        lpFourThree.height=width/3;

        return view;
    }

    private void addListener() {
        rlOneOne.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(getActivity(), LocalMusicListActivity.class);
                startActivity(intent);

            }
        });
        rlOneTwo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), FavoriteActivity.class));
            }
        });
        rlTwoThree.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(),RecentPlayActivity.class));
            }
        });
    }

    private void setupView() {
        rlOneOne=(RelativeLayout)view.findViewById(R.id.rl_one_one);
        rlOneTwo=(RelativeLayout)view.findViewById(R.id.rl_one_two);
        rlOneThree=(RelativeLayout)view.findViewById(R.id.rl_one_three);

        rlTwoOne=(RelativeLayout)view.findViewById(R.id.rl_two_one);
        rlTwoTwo=(RelativeLayout)view.findViewById(R.id.rl_two_two);
        rlTwoThree=(RelativeLayout)view.findViewById(R.id.rl_two_three);

        rlThreeOne=(RelativeLayout)view.findViewById(R.id.rl_three_one);
        rlThreeTwo=(RelativeLayout)view.findViewById(R.id.rl_three_two);
        rlThreeThree=(RelativeLayout)view.findViewById(R.id.rl_three_three);

        rlFourOne=(RelativeLayout)view.findViewById(R.id.rl_four_one);
        rlFourTwo=(RelativeLayout)view.findViewById(R.id.rl_four_two);
        rlFourThree=(RelativeLayout)view.findViewById(R.id.rl_four_three);

    }

}
