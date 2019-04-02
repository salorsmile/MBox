package salorsmile.lzh.fragment;


import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;


import salorsmile.lzh.application.App;
import salorsmile.lzh.mbox.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class RecommendFragment extends Fragment {

    private View view ;
    private Activity mActivity;
    private GridView gvSkin;

    public RecommendFragment() {

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_recommend, container, false);
        setupViews();
        return view;
    }

    private void setupViews() {
        mActivity = getActivity();
        gvSkin = (GridView) view.findViewById(R.id.gv_skin);
        MyGridAdapter adapter = new MyGridAdapter();
        gvSkin.setAdapter(adapter);

        gvSkin.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                App.setAppTheme(App.getAppThemeByIndex(position));
                /**/
            }
        });
    }


    class MyGridAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return App.getThemeArrayLength()-11;
        }

        @Override
        public Object getItem(int i) {
            return null;
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        /*获取某一个item*/
        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            ViewHolder holder = null;
            if(view == null){
                view = View.inflate(mActivity,R.layout.item_skin_layout,null);
                holder = new ViewHolder();
                holder.viewItem = (ImageView) view.findViewById(R.id.img_item);
                view.setTag(holder);
            }else {
                holder = (ViewHolder) view.getTag();
            }
            holder.viewItem.setImageResource(App.getAppThemeByIndex(i));
            int width = App.sScreenWidth/3;
            int height = App.sScreenHeight/3;
            ViewGroup.LayoutParams lp = holder.viewItem.getLayoutParams();
            lp.width = width;
            lp.height = height;
            return view;
        }

        class ViewHolder {
            private ImageView viewItem;
        }
    }

}
