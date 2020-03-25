package com.zhangwuji.im.ui.fragment;

import android.annotation.SuppressLint;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.zhangwuji.im.R;

@SuppressLint("SetJavaScriptEnabled")
public class HomeFragment extends MainFragment  implements View.OnClickListener {
    private View curView = null;
    private static String url;
    private  WebView webView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        url="https://b56.cn/portal.php?mod=index&mobile=2";
        if (null != curView) {
            ((ViewGroup) curView.getParent()).removeView(curView);
            return curView;
        }
        curView = inflater.inflate(R.layout.tt_fragment_webview, topContentView);
        super.init(curView);
        showProgressBar();
        initRes();

        return curView;
    }

    private void initRes() {
        // 设置顶部标题栏
        setTopTitleBold("主页");

        setTopRightButton(R.drawable.rc_ext_locator);
        topRightBtn.setOnClickListener(this);

        webView = (WebView) curView.findViewById(R.id.webView1);

        WebSettings webSettings=webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setSupportZoom(false);
        // 设置出现缩放工具
        webSettings.setBuiltInZoomControls(false);
        //扩大比例的缩放
        webSettings.setUseWideViewPort(true);
        //自适应屏幕
        webSettings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
        webSettings.setLoadWithOverviewMode(true);

        webView.loadUrl(url);
        webView.setVerticalScrollBarEnabled(false);
        webView.setHorizontalScrollBarEnabled(false);
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                setTopTitle(view.getTitle());
                hideProgressBar();
            }
            @Override
            public void onReceivedError(WebView view, int errorCode,
                                        String description, String failingUrl) {
                // TODO Auto-generated method stub
                super.onReceivedError(view, errorCode, description, failingUrl);
                hideProgressBar();
            }

        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void initHandler() {
    }

    /**
     * @param str
     */
    public static void setUrl(String str) {
        url = str;
    }

    private static final String SCHEMA ="com.zhangwuji.im://message_private_url";
    private static final String PARAM_UID ="uid";
    private static final Uri PROFILE_URI = Uri.parse(SCHEMA);
    private void extractUidFromUri() {
        Uri uri = getActivity().getIntent().getData();
        if (uri !=null && PROFILE_URI.getScheme().equals(uri.getScheme())) {
            url = uri.getQueryParameter(PARAM_UID);
        }
        if(url.indexOf("www") == 0){
            url = "http://"+url;
        }else if(url.indexOf("https") == 0){
            String bUid = url.substring(5, url.length());
            url = "http"+bUid;
        }
    }

    @Override
    public void onClick(View view) {
        final int id = view.getId();
        switch (id) {
            case R.id.right_btn:
                webView.loadUrl(url);
                break;
        }
    }
}
