package demo.lz.com.test.UI;

import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.io.File;

/**
 * Created by Administrator on 2018/7/10.
 */

public class CachedWebView extends WebView {
    private static final String TAG = "CachedWebView";
    private WebSettings mWebSettings;
    private int    adpterPos = -1;
    private String mUrl      = "";
    private ResultCallback mCallback;
    private boolean isIdle      = true;
    private long    idleTimeOut = 60000;

    public CachedWebView(Context context) {
        this(context, null);
    }

    public CachedWebView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CachedWebView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mWebSettings = getSettings();
        //缓存模式   ：先查看缓存没命中则从网络获取  防止同一列表出现相同url重复加载资源的浪费
        mWebSettings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
        //缓存地址
        String cacheDir = context.getFilesDir().getAbsolutePath();

        if (!new File(cacheDir).exists()) {
            new File(cacheDir).mkdirs();
        }
        mWebSettings.setAppCachePath(cacheDir);
        //缓存最大容量
        mWebSettings.setAppCacheMaxSize(10 * 1024 * 1024);
        mWebSettings.setDomStorageEnabled(true);
        mWebSettings.setAllowFileAccess(true);
        mWebSettings.setAppCacheEnabled(true);
        mWebSettings.setDatabaseEnabled(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mWebSettings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        }

        mWebSettings.setJavaScriptEnabled(true);


        setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                /**
                 * 预加载完成   回调activity进行该项的打标记
                 */
                Log.e(TAG, "onPageFinished: ~~~~~~~~" + url + "     " + adpterPos);
                mCallback.onPageFinshed(mUrl, adpterPos);
                reset();
            }

        });

    }


    public void setCallback(ResultCallback callback) {
        mCallback = callback;
    }

    /**
     * webview开始预加载任务
     */
    public void load(String url, int adapterPos) {
        isIdle = false;
        loadUrl(url);
        this.adpterPos = adapterPos;
        mUrl = url;
    }

    /**
     * 重置webview
     */
    public void reset() {
//        removeAllViews();
        adpterPos = -1;
        mUrl = "";
        isIdle = true;
        postDelayed(new Runnable() {
            @Override
            public void run() {
                release();
            }
        }, idleTimeOut);
    }

    /**
     * 释放webview
     */
    public void release() {
        removeAllViews();
        destroy();
        mCallback.onRelease(this);
    }

    /**
     * webview是否空闲
     */
    public boolean isIdle() {
        return isIdle;
    }

    public interface ResultCallback {
        void onPageFinshed(String url, int adapterPos);

        void onRelease(CachedWebView webView);
    }


}
