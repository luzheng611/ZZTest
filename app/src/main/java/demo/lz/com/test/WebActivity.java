package demo.lz.com.test;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import demo.lz.com.test.Cache.DiskWebResourseCache;

/**
 * Created by Administrator on 2018/7/10.
 */

public class WebActivity extends AppCompatActivity {
    private static final String TAG = "WebActivity";
    private WebView mWebView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web);
        mWebView = findViewById(R.id.web);

        mWebView.getSettings().setJavaScriptEnabled(true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mWebView.getSettings().setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        }

        /**
         * 缓存策略：只加载缓存资源, 断网打开页面测试缓存
         */
        mWebView.getSettings().setCacheMode(WebSettings.LOAD_CACHE_ONLY);
//        mWebView.getSettings().setCacheMode(WebSettings.LOAD_DEFAULT);
        mWebView.loadUrl(getIntent().getStringExtra("url"));
        mWebView.setWebViewClient(new WebViewClient() {
            @TargetApi(Build.VERSION_CODES.LOLLIPOP)
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                view.loadUrl(request.getUrl().toString());
                return true;
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
//                mWebView.getSettings().setCacheMode(WebSettings.LOAD_DEFAULT);
            }

            @TargetApi(Build.VERSION_CODES.LOLLIPOP)
            @Nullable
            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
                String url = request.getUrl().toString();
                try {
                    //MD5
                    String resFileName = MD5Utls.stringToMD5(url);
                    if (TextUtils.isEmpty(resFileName)) {
                        return null;
                    }

                    return DiskWebResourseCache.getInstance().loadWebResource(url, resFileName,
                            request.getRequestHeaders().get("Accept"), "UTF-8", DiskWebResourseCache.DEFAULT_MaxCacheTime);
                } catch (Exception e) {
                    Log.e(TAG, "shouldInterceptRequest: 异常");
                }

                return super.shouldInterceptRequest(view, request);
            }

            @Nullable
            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, String url) {

                return super.shouldInterceptRequest(view, url);
            }
        });

    }

    @Override
    public void onBackPressed() {
        if (mWebView.canGoBack()) {
            mWebView.goBack();
        } else {
            super.onBackPressed();
        }

    }
}
