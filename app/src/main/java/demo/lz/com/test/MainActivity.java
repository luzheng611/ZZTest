package demo.lz.com.test;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import demo.lz.com.test.Bean.ItemBean;
import demo.lz.com.test.UI.IWebView;
import demo.lz.com.test.UI.TagItemDecoration;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "Test~~~~~!!!!~~~~~~";
    private RecyclerView mRecyclerView;
    private ItemAdapter mItemAdapter;
//    private ExecutorService mExecutorService;
    private ArrayList<IWebView> mIWebViews;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mIWebViews=new ArrayList<>();

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
                        doInterval();
                        break;
                    case RecyclerView.SCROLL_STATE_DRAGGING:
                    case RecyclerView.SCROLL_STATE_SETTLING:
//                        mExecutorService.shutdownNow();
                        mRecyclerView.removeCallbacks(mRunnable);
                        break;
                }
            }
        });


    }

    @Override
    protected void onPause() {
        super.onPause();
//        if (mExecutorService != null && !mExecutorService.isShutdown()) {
//            mExecutorService.shutdownNow();
//        }
    }

    @Override
    protected void onResume() {
        super.onResume();
//        if (mExecutorService == null) {
//            mExecutorService = Executors.newCachedThreadPool();
//        }
        doInterval();
    }
    Runnable mRunnable=new Runnable() {
        @Override
        public void run() {
//            mExecutorService.submit(new Runnable() {
            //                    @Override
//                    public void run() {
            int startPos = ((LinearLayoutManager) mRecyclerView.getLayoutManager()).findFirstCompletelyVisibleItemPosition();
            int endPos = ((LinearLayoutManager) mRecyclerView.getLayoutManager()).findLastCompletelyVisibleItemPosition();
            for (int i = startPos; i <= endPos; i++) {
                final ItemBean bean = mItemAdapter.datas.get(i);
                if (bean.getState()) {
                    continue;
                }
                final IWebView iWebView = new IWebView(MainActivity.this);
                iWebView.load(bean.getUrl(), i);
                iWebView.setCallback(new IWebView.ResultCallback() {
                    @Override
                    public void onPageFinshed(String url, int adapterPos) {
                        bean.setState(true);
                        mItemAdapter.notifyItemChanged(adapterPos);
                        Log.e(TAG, "onPageFinshed: 完成预加载" + url + "   位置：：" + adapterPos
                                + "    bean：：：：" + bean.toString());
                    }

                    @Override
                    public void onRelease() {
                        mIWebViews.remove(iWebView);
                    }
                });
                mIWebViews.add(iWebView);
            }
        }
    };
    private void doInterval() {
        mRecyclerView.postDelayed(mRunnable, 3000);
    }

    private ArrayList<ItemBean> makeDatas() {
        ArrayList<ItemBean> datas = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            ItemBean itemBean = new ItemBean();
            if (i % 3 == 0) {
                itemBean.setUrl("https://www.baidu.com");
            } else if (i % 2 == 0) {
                itemBean.setUrl("https://www.sogou.com");
            } else {
                itemBean.setUrl("https://login.tmall.com");
            }

            datas.add(itemBean);
        }
        return datas;
    }


    public static class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.IViewHolder> {
        private ArrayList<ItemBean> datas;
        private Context mContext;

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
            StringBuilder content = new StringBuilder("Item");
            content.append(i);
            iViewHolder.mTextView.setText(content);
            iViewHolder.mTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent=new Intent(mContext,WebActivity.class);
                    intent.putExtra("url",itemBean.getUrl());
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
