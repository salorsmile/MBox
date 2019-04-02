package salorsmile.lzh.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import salorsmile.lzh.mbox.R;

public class TaoGeFragment extends Fragment {
    private WebView webView;
    private View view;

    public TaoGeFragment() {
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view =inflater.inflate(R.layout.fragment_tao_ge, container, false);
        //初始化
        setupView();

        return view;
    }

    private void setupView() {
        webView= (WebView) view.findViewById(R.id.webview);
        //设置控件显示网页
        webView.getSettings().setJavaScriptEnabled(true);
        //把要显示的网页设置到控件上   以控件形式进行下载，要不 就得以浏览器页面加载
        webView.setWebViewClient(new WebViewClient(){
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }
        });
        webView.loadUrl("http://music.baidu.com/");
    }

}
