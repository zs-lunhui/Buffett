package com.buffett.pulltorefresh.refresh_view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.buffett.pulltorefresh.R;
import com.buffett.pulltorefresh.core.RefreshView;

/**
 * Created by lisao on 16/6/27.
 */
public class LoGo extends View implements RefreshView{

    private int measureHeight;//获取控件实际占用的高度
    private int measureWidth;//获取控件实际占用的宽度

    private int height;//绘图区域的高度
    private int width;//绘图区域的宽度

    private int radius;//圆点半径

    private Paint paint;

    private int padding;//绘图区域与实际的宽度

    private int duration = 0;//持续时间

    private static final int TOTAL_TIME = 80;

    private static final int LOADING_1 = TOTAL_TIME / 3;//加载第一圆和5个小圆所用的时间

    private static final int LOADING_2 = TOTAL_TIME / 3 * 2;

    private float offset;//坐标偏移量

    private float percent;
    private boolean loading;
    private boolean stop;

    public LoGo(Context context) {
        this(context, null);
    }

    public LoGo(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LoGo(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.LOGO);
        radius = array.getDimensionPixelSize(R.styleable.LOGO_point_radius, 10);
        padding = array.getDimensionPixelSize(R.styleable.LOGO_draw_padding, 10);
        array.recycle();
        offset = width / 4;
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
        initPaint();//初始化画笔工具
        //1.先把所有的点一次画出来
        if (!loading){
            drawFirstPoint(canvas);
        }else {
            drawAllPoint(canvas);//画出所有的点
            if (duration < TOTAL_TIME)
                duration++;
            else
                duration = 0;
        }
        invalidate();
    }

    /**
     * 初始化画笔
     */
    private void initPaint() {
        if (paint == null) {
            paint = new Paint();
        }
        paint.setAntiAlias(true);
    }

    /**
     * 绘制所有的点
     *
     * @param canvas
     */
    private void drawAllPoint(Canvas canvas) {
        //先画中心点
        if (duration < LOADING_1) {//第一阶段
            canvas.drawCircle(padding + width / 2, padding + width / 2, 1f * radius / LOADING_1 * duration, paint);
        } else {
            canvas.drawCircle(padding + width / 2, padding + width / 2, 1f * radius, paint);
        }
        if (duration > LOADING_1 / 2 && duration < LOADING_1) {
            canvas.drawCircle(padding, padding, 1f * radius / LOADING_1 * duration, paint);
            canvas.drawCircle((float) (padding + width / 4), (float) (padding + height / 4), 1f * radius / LOADING_1 * duration, paint);

            canvas.drawCircle((float) (padding + width / 4 * 3), (float) (padding + height / 4 * 3), 1f * radius / LOADING_1 * duration, paint);
            canvas.drawCircle((float) (padding + width), (float) (padding + height), 1f * radius / LOADING_1 * duration, paint);
        } else if (duration >= LOADING_1) {
            canvas.drawCircle(padding, padding, radius, paint);
            canvas.drawCircle((float) (padding + width / 4), (float) (padding + height / 4), radius, paint);

            canvas.drawCircle((float) (padding + width / 4 * 3), (float) (padding + height / 4 * 3), radius, paint);
            canvas.drawCircle((float) (padding + width), (float) (padding + height), radius, paint);
        }
        if (duration >= LOADING_1 && duration <= LOADING_2) {
            canvas.drawCircle((padding + width / 4) + (width / 4f) / (LOADING_2 - LOADING_1) * (duration - LOADING_1), (padding + width / 4) - (width / 4f) / (LOADING_2 - LOADING_1) * (duration - LOADING_1), 1f * radius / (LOADING_2 - LOADING_1) * (duration - LOADING_1), paint);
            canvas.drawCircle((padding + width / 2) + (width / 4f) / (LOADING_2 - LOADING_1) * (duration - LOADING_1), (padding + width / 2) - (width / 4f) / (LOADING_2 - LOADING_1) * (duration - LOADING_1), 1f * radius / (LOADING_2 - LOADING_1) * (duration - LOADING_1), paint);
            canvas.drawCircle((padding + width / 2) - (width / 4f) / (LOADING_2 - LOADING_1) * (duration - LOADING_1), (padding + width / 2) + (width / 4f) / (LOADING_2 - LOADING_1) * (duration - LOADING_1), 1f * radius / (LOADING_2 - LOADING_1) * (duration - LOADING_1), paint);
            canvas.drawCircle((padding + width / 4f * 3) - (width / 4f) / (LOADING_2 - LOADING_1) * (duration - LOADING_1), (padding + width / 4f * 3) + (width / 4f) / (LOADING_2 - LOADING_1) * (duration - LOADING_1), 1f * radius / (LOADING_2 - LOADING_1) * (duration - LOADING_1), paint);

        } else if (duration >= LOADING_2) {
            canvas.drawCircle(padding + width / 2, padding, radius, paint);
            canvas.drawCircle(padding + width / 4 * 3, padding + width / 4, radius, paint);
            canvas.drawCircle(padding + width / 4, padding + width / 4 * 3, radius, paint);
            canvas.drawCircle(padding + width / 4, padding + width / 4, radius, paint);
            canvas.drawCircle(padding + width / 2, padding + width, radius, paint);
        }
        if (duration >= LOADING_2) {
            canvas.drawCircle(padding + width / 4f * 3 + width / 4f / (TOTAL_TIME - LOADING_2) * (duration - LOADING_2), (padding + width / 4f) - width / 4f / (TOTAL_TIME - LOADING_2) * (duration - LOADING_2), 1f * radius / (TOTAL_TIME - LOADING_2) * (duration - LOADING_2), paint);
            canvas.drawCircle(padding + width / 4f - width / 4f / (TOTAL_TIME - LOADING_2) * (duration - LOADING_2), (width / 4f * 3 + padding) + (width / 4f) / (TOTAL_TIME - LOADING_2) * (duration - LOADING_2), 1f * radius / (TOTAL_TIME - LOADING_2) * (duration - LOADING_2), paint);
        }
    }

    private void drawFirstPoint(Canvas canvas){
            canvas.drawCircle(padding + width / 2, padding + width / 2, (float) (1f * width *Math.max(1-percent,0.2)/ 2), paint);
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        measureHeight = getHeight();
        measureWidth = getWidth();
        height = measureHeight - 2 * padding;
        width = measureWidth - 2 * padding;
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    public View getView() {
        return this;
    }

    @Override
    public void onShow(float percent) {
        this.percent = percent;
        if (percent<1){
            duration = 0;
        }
        Log.d("LoGo",percent+" , "+(float) (1f * width *Math.max(1-percent,0.2)/ 2));
    }

    @Override
    public void onClose(float percent) {
        if (percent ==0)duration = 0;
    }

    @Override
    public void onLoading() {
        loading = true;
        stop = false;
    }

    @Override
    public void onStop() {
        loading = false;
        stop = true;
    }
}