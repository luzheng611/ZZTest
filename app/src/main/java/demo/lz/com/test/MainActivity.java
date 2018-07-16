package demo.lz.com.test;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

import demo.lz.com.test.Bean.ItemBean;
import demo.lz.com.test.UI.CachedWebView;
import demo.lz.com.test.UI.TagItemDecoration;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "Test~~~~~!!!!~~~~~~";
    private RecyclerView             mRecyclerView;
    private ItemAdapter              mItemAdapter;
    private ArrayList<CachedWebView> mCachedWebViews;
    private boolean hasDoIntervalFirst = false;
    private String[] permissions=new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.READ_EXTERNAL_STORAGE};
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
    }

    private boolean applyForPermission() {
        if(Build.VERSION.SDK_INT>=23){
            for(int i=0;i<permissions.length;i++){
                int granted=checkSelfPermission(permissions[i]);
                if(granted!= PackageManager.PERMISSION_GRANTED){
                    requestPermissions(permissions,666);
                    return false;
                }
            }
        }
        return true;
    }

    private void initView() {
        mCachedWebViews = new ArrayList<>();

        mRecyclerView = findViewById(R.id.recycle);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.addItemDecoration(new TagItemDecoration(this));

        mItemAdapter = new ItemAdapter(this, makeDatas());
        mRecyclerView.setAdapter(mItemAdapter);

        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                switch (newState) {
                    case RecyclerView.SCROLL_STATE_IDLE:
                        /**
                         * 静止   开始延时任务
                         */
                        doInterval();
                        break;
                    case RecyclerView.SCROLL_STATE_DRAGGING:
                    case RecyclerView.SCROLL_STATE_SETTLING:
                        /**
                         * 正在拖动或者fling 移除延时任务
                         */
                        mRecyclerView.removeCallbacks(mainRunnable);
                        break;
                }
            }
        });
    }

    /**
     * 第一次进入页面时没有触发idle状态  手动放入3秒的延时任务
     */
    @Override
    protected void onResume() {
        super.onResume();
        if (!hasDoIntervalFirst&& applyForPermission()) {
            doInterval();
        }
    }

    /**
     * 找到完全显示的item,循环，如果没有完成预加载则预加载
     */
    Runnable mainRunnable = new Runnable() {
        @Override
        public void run() {

            int startPos = ((LinearLayoutManager) mRecyclerView.getLayoutManager()).findFirstCompletelyVisibleItemPosition();
            int endPos   = ((LinearLayoutManager) mRecyclerView.getLayoutManager()).findLastCompletelyVisibleItemPosition();

            for (int i = startPos; i <= endPos; i++) {
                final ItemBean bean = mItemAdapter.datas.get(i);
                if (bean.getState()) {
                    //已完成预加载并且已打上标记
                    continue;
                }
                CachedWebView cachedWebView = null;

                //复用
                cachedWebView = checkRemainWebView(cachedWebView);


                if (cachedWebView == null) {
                    cachedWebView = new CachedWebView(MainActivity.this);
                    cachedWebView.setShouldKillself(true);
                }


                cachedWebView.load(bean.getUrl(), i);
                cachedWebView.setCallback(new CachedWebView.ResultCallback() {
                    @Override
                    public void onPageFinshed(String url, int adapterPos) {
                        bean.setState(true);//标记的开关
                        mItemAdapter.notifyItemChanged(adapterPos);//只刷新完成预加载的item项
                    }

                    @Override
                    public void onRelease(CachedWebView view) {
                        mCachedWebViews.remove(view);
                        view = null;
                    }
                });
                mCachedWebViews.add(cachedWebView);
            }
        }
    };

    /**
     * @param cachedWebView 如果webview复用池中有处于idle状态的则赋值
     * @return
     */
    private CachedWebView checkRemainWebView(CachedWebView cachedWebView) {
        //循环缓存的webview 查看是否有处于idle状态的webview  复用

        for (CachedWebView webView : mCachedWebViews) {
            if (webView.isIdle()) {
                Log.e(TAG, "checkRemainWebView:复用");
                cachedWebView = webView;
            }
        }
        return cachedWebView;
    }

    /**
     * 将静止三秒后的任务放入队列
     */
    private void doInterval() {
        hasDoIntervalFirst=true;
        mRecyclerView.postDelayed(mainRunnable, 3000);
    }

    /**
     * @return 数据源
     */
    private ArrayList<ItemBean> makeDatas() {
        ArrayList<ItemBean> datas = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            ItemBean itemBean = new ItemBean();
            itemBean.setUrl("https://m.baidu.com");
            datas.add(itemBean);
        }
        return datas;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode==666){
            if(!hasDoIntervalFirst){
                doInterval();
            }
        }
    }

    /**
     * 列表适配器
     */
    public static class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.IViewHolder> {
        private ArrayList<ItemBean> datas;
        private Context             mContext;

        public ItemAdapter(Context context, ArrayList<ItemBean> beans) {
            this.datas = beans;
            mContext = context;
        }

        @NonNull
        @Override
        public IViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, final int i) {
            TextView textView = new TextView(mContext);
            textView.setLayoutParams(new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 400));
            textView.setGravity(Gravity.CENTER);
            textView.setTextSize(25);
            textView.setTextColor(Color.BLACK);
            return new IViewHolder(textView);
        }

        @Override
        public void onBindViewHolder(@NonNull final IViewHolder iViewHolder, int i) {
            Log.e(TAG, "onBindViewHolder: " + i);
            final ItemBean itemBean = datas.get(i);
            StringBuilder  content  = new StringBuilder("Item");
            content.append(i);
            iViewHolder.mTextView.setText(content);
            iViewHolder.mTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(mContext, WebActivity.class);
                    intent.putExtra("url", itemBean.getUrl());
                    mContext.startActivity(intent);
                }
            });
            iViewHolder.mTextView.setTag(itemBean.getState());


        }

        @Override
        public int getItemCount() {
            return datas == null ? 0 : datas.size();
        }

        public class IViewHolder extends RecyclerView.ViewHolder {
            private TextView mTextView;

            public IViewHolder(@NonNull View itemView) {
                super(itemView);
                mTextView = (TextView) itemView;
            }
        }
    }
}
