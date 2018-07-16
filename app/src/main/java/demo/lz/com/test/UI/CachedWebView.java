package demo.lz.com.test.UI;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import demo.lz.com.test.Cache.DiskWebResourseCache;
import demo.lz.com.test.MD5Utls;

/**
 * Created by Administrator on 2018/7/10.
 */

public class CachedWebView extends WebView {
    private static final String TAG = "CachedWebView";
    private WebSettings mWebSettings;
    private int    adpterPos = -1;
    private String mUrl      = "";
    private ResultCallback mCallback;
    private boolean isIdle         = true;
    private boolean shouldKillself = false;
    private long    idleTimeOut    = 60000;


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


        mWebSettings.setAppCachePath(cacheDir);
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

            @TargetApi(Build.VERSION_CODES.LOLLIPOP)
            @Nullable
            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
                String url = request.getUrl().toString();
                //获取本地的URL主域名
                String domain = request.getUrl().getHost();
                //取不到domain直接返回

                if (domain == null) {
                    return null;
                }
                Log.e(TAG, "shouldInterceptRequest: "+url );
                try {
                    //MD5
                    String resFileName = MD5Utls.stringToMD5(url);
                    if (TextUtils.isEmpty(resFileName)) {
                        return null;
                    }

                    return DiskWebResourseCache.getInstance().loadWebResource(url, resFileName,
                            request.getRequestHeaders().get("Accept"), "UTF-8", DiskWebResourseCache.DEFAULT_MaxCacheTime);
                } catch (Exception e) {
                    Log.e(TAG, "shouldInterceptRequest: 异常" + e.getMessage());
                }

                return super.shouldInterceptRequest(view, request);
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
        adpterPos = -1;
        mUrl = "";
        isIdle = true;
        if (shouldKillself) {
            postDelayed(new Runnable() {
                @Override
                public void run() {
                    release();
                }
            }, idleTimeOut);
        }

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

    public void setShouldKillself(boolean shouldKillself) {
        this.shouldKillself = shouldKillself;
    }

    public interface ResultCallback {
        void onPageFinshed(String url, int adapterPos);

        void onRelease(CachedWebView webView);
    }


}
