package demo.lz.com.test.UI;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

/**
 * Created by Administrator on 2018/7/10.
 */

public class IWebView extends WebView {
    private static final String TAG = "IWebView";
    private WebSettings mWebSettings;
    private int adpterPos = -1;
    private String mUrl = "";
    private ResultCallback mCallback;
    private boolean isIdle = true;
    private long idleTimeOut = 60000;

    public IWebView(Context context) {
        this(context, null);
    }

    public IWebView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public IWebView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mWebSettings = getSettings();
        //缓存模式
        mWebSettings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
        mWebSettings.setJavaScriptEnabled(true);

        setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                mCallback.onPageFinshed(mUrl, adpterPos);
                reset();
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                Log.e(TAG, "shouldOverrideUrlLoading: " + getUrl());
                return super.shouldOverrideUrlLoading(view, request);
            }


        });
    }

    public void setCallback(ResultCallback callback) {
        mCallback = callback;
    }

    public void load(String url, int adapterPos) {
        loadUrl(url);
        this.adpterPos = adapterPos;
        mUrl = url;
    }

    public void reset() {
        removeAllViews();
        adpterPos = -1;
        mUrl = "";
        postDelayed(new Runnable() {
            @Override
            public void run() {
                release();
            }
        }, idleTimeOut);
    }

    public void release() {
        Log.e(TAG, "release: 销毁"+this );
        mCallback.onRelease();
        removeAllViews();
        destroy();


    }

    public interface ResultCallback {
        void onPageFinshed(String url, int adapterPos);
        void onRelease();
    }


}
