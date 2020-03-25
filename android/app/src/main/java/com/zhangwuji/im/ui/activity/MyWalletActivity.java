package com.zhangwuji.im.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.qmuiteam.qmui.widget.dialog.QMUITipDialog;
import com.zhangwuji.im.R;
import com.zhangwuji.im.server.utils.json.JsonMananger;
import com.zhangwuji.im.ui.helper.LoginInfoSp;


public class MyWalletActivity extends AppCompatActivity {
    WebView mWebView;
    private QMUITipDialog tipDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_wallet);
        mWebView = (WebView) findViewById(R.id.webview);
        initWebView();

        tipDialog = new QMUITipDialog.Builder(this)
                .setIconType(QMUITipDialog.Builder.ICON_TYPE_LOADING)
                .setTipWord("努力加载中...")
                .create();

    }

    /**
     * 配置webview参数
     */
    private void initWebView() {


        // 设置WebView的客户端
        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return false;// 返回false
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                if (tipDialog != null) {
                    tipDialog.show();
                }

            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                if (tipDialog != null) {
                    tipDialog.dismiss();
                }
            }
        });

        WebSettings webSettings = mWebView.getSettings();
        // 让WebView能够执行javaScript
        webSettings.setJavaScriptEnabled(true);
        // 让JavaScript可以自动打开windows
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
        // 设置缓存
        webSettings.setAppCacheEnabled(false);
        // 设置缓存模式,一共有四种模式
        webSettings.setCacheMode(webSettings.LOAD_NO_CACHE);
        // 设置缓存路径
        //        webSettings.setAppCachePath("");
        // 支持缩放(适配到当前屏幕)
        webSettings.setSupportZoom(true);
        // 将图片调整到合适的大小
        webSettings.setUseWideViewPort(true);
        // 支持内容重新布局,一共有四种方式
        // 默认的是NARROW_COLUMNS
        webSettings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
        // 设置可以被显示的屏幕控制
        webSettings.setDisplayZoomControls(true);
        // 设置默认字体大小
        webSettings.setDefaultFontSize(12);


        mWebView.loadUrl("http://cnm82.com:8088/#/");
        mWebView.addJavascriptInterface(new PhoneAndJSInterface(), "Android");
    }

    // 设置回退监听
    // 5、覆盖Activity类的onKeyDown(int keyCoder,KeyEvent event)方法
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            if (mWebView.canGoBack()) {
                mWebView.goBack(); //goBack()表示返回WebView的上一页面
                return true;
            } else {
                finish();
                return true;
            }
        }
        return false;
    }

    class PhoneAndJSInterface {

        @JavascriptInterface
        public String getUserInfo() {
            String userInfo = "";
            LoginInfoSp.LoginInfoSpIdentity userBean = LoginInfoSp.instance().getLoginInfoIdentity();
            if (userBean != null) {
                userInfo = JsonMananger.beanToJson(userBean);
            }
            return userInfo;
        }

        @JavascriptInterface
        public void closeWindow() {
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        if (mWebView != null) {
            mWebView.destroy();
        }
        if (tipDialog != null && tipDialog.isShowing()) {
            tipDialog.dismiss();
        }
        super.onDestroy();
    }

    public static void launch(Context context) {
        Intent intent = new Intent(context, MyWalletActivity.class);
        context.startActivity(intent);
    }
}
