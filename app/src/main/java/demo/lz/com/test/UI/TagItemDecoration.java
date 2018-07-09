package demo.lz.com.test.UI;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Picture;
import android.graphics.Rect;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Adapter;
import android.widget.LinearLayout;

/**
 * Created by Administrator on 2018/7/9.
 */

public class TagItemDecoration extends RecyclerView.ItemDecoration {
    private static final String TAG = "TagItemDecoration";
    private Context mContext;
    private Paint linePaint;
    private TextPaint mTextPaint;
    private String drawText = "6个6";

    public TagItemDecoration(Context context) {
        this.mContext = context;
        linePaint = new Paint();
        linePaint.setColor(0xffc6c6c6);
        linePaint.setDither(true);
        linePaint.setAntiAlias(true);
        linePaint.setStyle(Paint.Style.FILL);
        linePaint.setStrokeWidth(5);
        mTextPaint = new TextPaint();
        mTextPaint.setColor(Color.WHITE);
        mTextPaint.setTextSize(Math.round(context.getResources().getDisplayMetrics().density * 18 + 0.5f));
        mTextPaint.setDither(true);
        mTextPaint.setAntiAlias(true);
        mTextPaint.setStyle(Paint.Style.FILL);
    }

    @Override
    public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
        super.onDraw(c, parent, state);
        int count = parent.getChildCount();
        for (int i = 0; i < count; i++) {
            View child = parent.getChildAt(i);
            int left = parent.getLeft();
            int right = parent.getRight();
            int top = child.getBottom();
            int bottom = top + 5;
            c.drawRect(left, top, right, bottom, linePaint);
        }


    }

    @Override
    public void onDrawOver(Canvas c, RecyclerView parent, RecyclerView.State state) {
        super.onDrawOver(c, parent, state);
        int count = parent.getChildCount();
        int radius = 100;
        int marginRight = 50;
        for (int i = 0; i < count; i++) {
            View child = parent.getChildAt(i);
            if (!((boolean) child.getTag())) {
                continue;
            }
            int x = parent.getRight() - marginRight - radius;
            int y = child.getBottom() - child.getHeight() / 2;
            linePaint.setColor(Color.RED);
            c.drawCircle(x, y, radius, linePaint);

            Rect textRect = new Rect();

            mTextPaint.getTextBounds(drawText, 0, drawText.length(), textRect);
            c.drawText(drawText, x - textRect.width() / 2f, y + textRect.height() / 2f, mTextPaint);
        }
        linePaint.setColor(0xffc6c6c6);
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        super.getItemOffsets(outRect, view, parent, state);
        RecyclerView.LayoutManager layoutManager = parent.getLayoutManager();
        if (layoutManager instanceof LinearLayoutManager) {
            if (layoutManager.getLayoutDirection() == LinearLayoutManager.VERTICAL) {
                outRect.bottom = 5;
            }
        }
    }
}
