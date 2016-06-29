package com.buffett.pulltorefresh.refresh_view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.LinearInterpolator;

import com.buffett.pulltorefresh.R;
import com.buffett.pulltorefresh.core.RefreshView;
import com.buffett.pulltorefresh.util.Logger;

/**
 * Created by lisao on 16/6/27.
 */
public class LoGo extends View implements RefreshView {

    private int measureWidth;//获取控件实际占用的宽度

    private int width;//绘图区域的宽度
    private int radius;//圆点半径


    private Paint pointPaint_S = new Paint();
    private Paint pointPaint_M = new Paint();
    private Paint pointPaint_L = new Paint();

    //加载时的画笔
    private Paint loadingPaint_1;

    private Paint loadingPaint_2;


    private int ladingAlpha;

    private int padding;//绘图区域与实际的宽度

    private int duration = 0;//持续时间

    private int color_S;//深色
    private int color_M;//中间色
    private int color_L;//亮色

    private static final int TOTAL_TIME = 100;

    private static final float LOADING_1 = TOTAL_TIME / 3f;//加载第一圆和5个小圆所用的时间

    private static final float LOADING_2 = TOTAL_TIME / 3f * 2;

    private Status drawStatus;

    private float offset_X0;//坐标偏移量0倍
    private float offset_X1;//坐标偏移量1倍
    private float offset_X2;//坐标偏移量2倍
    private float offset_X3;//坐标偏移量3倍
    private float offset_X4;//坐标偏移量4倍

    private float percent;
    private boolean loading;
    private boolean stop = true;

    private ValueAnimator startAnimation;//绘制各个点的动画
    private ValueAnimator drawingAnimation;//绘制loading状态动画
    private ValueAnimator endAnimation;//绘制结束动画

    public LoGo(Context context) {
        this(context, null);
    }

    public LoGo(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LoGo(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.LoGo);
        radius = array.getDimensionPixelSize(R.styleable.LoGo_point_radius, 10);
        padding = array.getDimensionPixelSize(R.styleable.LoGo_draw_padding, 10);
        array.recycle();
        initColor();
    }

    //初始化动画
    private void initAnimation() {
        //设置开始动画
        startAnimation = new ValueAnimator();
        startAnimation.setDuration(500);//设置持续时间
        startAnimation.setIntValues(0, TOTAL_TIME);//设置渐变
        startAnimation.setInterpolator(new LinearInterpolator());
        startAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                duration = (int) animation.getAnimatedValue();
                Logger.d("addUpdateListener: " + duration);
                postInvalidate();
            }
        });
        startAnimation.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                setStatus(Status.DRAWING);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                loading();
            }
        });
        //设置loading动画
        drawingAnimation = new ValueAnimator();
        drawingAnimation.setDuration(650);//设置持续时间
        drawingAnimation.setIntValues(0, 15);
        drawingAnimation.setInterpolator(new AccelerateDecelerateInterpolator());
        drawingAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                ladingAlpha = (int) animation.getAnimatedValue() * (int) animation.getAnimatedValue();
                invalidate();
            }
        });
        drawingAnimation.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                setStatus(Status.LOADING);
            }
        });
        drawingAnimation.setRepeatCount(ValueAnimator.INFINITE);//设置循环次数
        drawingAnimation.setRepeatMode(ValueAnimator.REVERSE);

        //设置结束动画
        endAnimation = new ValueAnimator();
        endAnimation.setDuration(500);//设置持续时间
        endAnimation.setIntValues(0, TOTAL_TIME);//设置渐变
        endAnimation.setInterpolator(new LinearInterpolator());
        endAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                duration = (int) animation.getAnimatedValue();
                invalidate();
            }
        });
        endAnimation.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                setStatus(Status.ENDING);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        stop = true;
                    }
                }, 100);
            }
        });

    }


    private void initColor() {
        color_S = Color.parseColor("#8E0A1D");//深
        color_M = Color.parseColor("#C1142D");//中
        color_L = Color.parseColor("#FC3030");//亮
    }

    @Override
    public void draw(Canvas canvas) {
        initPaint();//初始化画笔工具
        initAnimation();
        if (drawStatus == Status.START && !loading) {//开始状态
            drawFirstPoint(canvas);
        } else if (drawStatus == Status.DRAWING) {
            //1.先把所有的点一次画出来
            drawAllPoint(canvas);//画出所有的点
        } else if (drawStatus == Status.LOADING) {//加载的状态
            drawAllPoint(canvas);
            drawLoading(canvas);
        } else {
            drawEnding(canvas);
        }
        super.draw(canvas);
    }


    /**
     * 初始化画笔
     */
    private void initPaint() {
        if (loadingPaint_1 == null) {
            loadingPaint_1 = new Paint();
            loadingPaint_1.setColor(color_L);
            loadingPaint_1.setStrokeWidth(radius / 2);
        }
        loadingPaint_1.setAntiAlias(true);

        if (loadingPaint_2 == null) {
            loadingPaint_2 = new Paint();
            loadingPaint_2.setStrokeWidth(radius / 2);
            loadingPaint_2.setColor(color_M);
        }
        loadingPaint_2.setAntiAlias(true);

        pointPaint_S.setAntiAlias(true);
        pointPaint_M.setAntiAlias(true);
        pointPaint_L.setAntiAlias(true);

        pointPaint_S.setColor(color_S);
        pointPaint_M.setColor(color_M);
        pointPaint_L.setColor(color_L);
    }

    /**
     * 绘制所有的点
     *
     * @param canvas
     */
    private void drawAllPoint(Canvas canvas) {
        //先画中心点
        if (duration < LOADING_1) {//第一阶段
            pointPaint_L.setAlpha((int) (255f / LOADING_1 * duration));
            canvas.drawCircle(offset_X2, offset_X2, 1f * radius / LOADING_1 * duration, pointPaint_L);
        } else {
            pointPaint_L.setAlpha(255);
            canvas.drawCircle(offset_X2, offset_X2, 1f * radius, pointPaint_L);
        }
        if (duration > LOADING_1 / 2 && duration < LOADING_1) {
            pointPaint_S.setAlpha((int) (255f / LOADING_1 * duration));
            pointPaint_M.setAlpha((int) (255f / LOADING_1 * duration));
            canvas.drawCircle(offset_X0, offset_X0, 1f * radius / LOADING_1 * duration, pointPaint_S);
            canvas.drawCircle(offset_X1, offset_X1, 1f * radius / LOADING_1 * duration, pointPaint_M);

            canvas.drawCircle(offset_X3, offset_X3, 1f * radius / LOADING_1 * duration, pointPaint_M);
            canvas.drawCircle(offset_X4, offset_X4, 1f * radius / LOADING_1 * duration, pointPaint_S);
        } else if (duration >= LOADING_1) {
            pointPaint_S.setAlpha(255);
            pointPaint_M.setAlpha(255);
            canvas.drawCircle(offset_X0, offset_X0, radius, pointPaint_S);
            canvas.drawCircle(offset_X1, offset_X1, radius, pointPaint_M);

            canvas.drawCircle(offset_X3, offset_X3, radius, pointPaint_M);
            canvas.drawCircle(offset_X4, offset_X4, radius, pointPaint_S);
        }
        if (duration >= LOADING_1 && duration <= LOADING_2) {
            pointPaint_M.setAlpha((int) (255f / (LOADING_2 - LOADING_1) * (duration - LOADING_1)));
            pointPaint_L.setAlpha((int) (255f / (LOADING_2 - LOADING_1) * (duration - LOADING_1)));
            canvas.drawCircle(offset_X1 + (offset_X1 - padding) / (LOADING_2 - LOADING_1) * (duration - LOADING_1), offset_X1 - (offset_X1 - padding) / (LOADING_2 - LOADING_1) * (duration - LOADING_1), 1f * radius / (LOADING_2 - LOADING_1) * (duration - LOADING_1), pointPaint_M);
            canvas.drawCircle(offset_X2 + (offset_X1 - padding) / (LOADING_2 - LOADING_1) * (duration - LOADING_1), offset_X2 - (offset_X1 - padding) / (LOADING_2 - LOADING_1) * (duration - LOADING_1), 1f * radius / (LOADING_2 - LOADING_1) * (duration - LOADING_1), pointPaint_L);
            canvas.drawCircle(offset_X2 - (offset_X1 - padding) / (LOADING_2 - LOADING_1) * (duration - LOADING_1), offset_X2 + (offset_X1 - padding) / (LOADING_2 - LOADING_1) * (duration - LOADING_1), 1f * radius / (LOADING_2 - LOADING_1) * (duration - LOADING_1), pointPaint_L);
            canvas.drawCircle(offset_X3 - (offset_X1 - padding) / (LOADING_2 - LOADING_1) * (duration - LOADING_1), offset_X3 + (offset_X1 - padding) / (LOADING_2 - LOADING_1) * (duration - LOADING_1), 1f * radius / (LOADING_2 - LOADING_1) * (duration - LOADING_1), pointPaint_M);

        } else if (duration >= LOADING_2) {
            pointPaint_M.setAlpha(255);
            pointPaint_L.setAlpha(255);
            canvas.drawCircle(offset_X2, offset_X0, radius, pointPaint_M);
            canvas.drawCircle(offset_X3, offset_X1, radius, pointPaint_L);
            canvas.drawCircle(offset_X1, offset_X3, radius, pointPaint_L);
            canvas.drawCircle(offset_X1, offset_X1, radius, pointPaint_M);
            canvas.drawCircle(offset_X2, offset_X4, radius, pointPaint_M);
        }
        if (duration >= LOADING_2) {
            pointPaint_L.setAlpha((int) (255f / (TOTAL_TIME - LOADING_2) * (duration - LOADING_2)));
            canvas.drawCircle(padding + width / 4f * 3 + width / 4f / (TOTAL_TIME - LOADING_2) * (duration - LOADING_2), (padding + width / 4f) - width / 4f / (TOTAL_TIME - LOADING_2) * (duration - LOADING_2), 1f * radius / (TOTAL_TIME - LOADING_2) * (duration - LOADING_2), pointPaint_L);
            canvas.drawCircle(padding + width / 4f - width / 4f / (TOTAL_TIME - LOADING_2) * (duration - LOADING_2), (width / 4f * 3 + padding) + (width / 4f) / (TOTAL_TIME - LOADING_2) * (duration - LOADING_2), 1f * radius / (TOTAL_TIME - LOADING_2) * (duration - LOADING_2), pointPaint_L);
        }
    }

    /**
     * 绘制加载的状态
     *
     * @param canvas
     */
    private void drawLoading(Canvas canvas) {
        loadingPaint_1.setAlpha(ladingAlpha);
        loadingPaint_2.setAlpha(ladingAlpha);
        Path path = new Path();
        float centerX = (offset_X1 + offset_X2) / 2f;
        float centerY = (offset_X1 + offset_X0) / 2f;

        path.moveTo(offset_X1 - (float) (radius * Math.cos(45)), (float) (offset_X1 - radius * Math.sin(45)));//起点
        path.lineTo((float) (offset_X1 + radius * Math.cos(45)), (float) (offset_X1 + radius * Math.sin(45)));
        path.quadTo(centerX, centerY, (float) (offset_X2 + radius * Math.cos(45)), (float) (offset_X0 + radius * Math.sin(45)));
        path.lineTo((float) (offset_X2 - radius * Math.cos(45)), (float) (offset_X0 - radius * Math.sin(45)));
        path.quadTo(centerX, centerY, (float) (offset_X1 - radius * Math.cos(45)), (float) (offset_X1 - radius * Math.sin(45)));
        canvas.drawPath(path, loadingPaint_2);
        path.reset();

        centerX = (offset_X2 + offset_X3) / 2f;
        centerY = (offset_X2 + offset_X1) / 2f;
        path.moveTo(offset_X2 - (float) (radius * Math.cos(45)), (float) (offset_X2 - radius * Math.sin(45)));//起点
        path.lineTo((float) (offset_X2 + radius * Math.cos(45)), (float) (offset_X2 + radius * Math.sin(45)));
        path.quadTo(centerX, centerY, (float) (offset_X3 + radius * Math.cos(45)), (float) (offset_X1 + radius * Math.sin(45)));
        path.lineTo((float) (offset_X3 - radius * Math.cos(45)), (float) (offset_X1 - radius * Math.sin(45)));
        path.quadTo(centerX, centerY, (float) (offset_X2 - radius * Math.cos(45)), (float) (offset_X2 - radius * Math.sin(45)));
        canvas.drawPath(path, loadingPaint_1);
        path.reset();

        centerX = (offset_X3 + offset_X4) / 2f;
        centerY = (offset_X1 + offset_X0) / 2f;
        path.moveTo(offset_X3 - (float) (radius * Math.cos(45)), (float) (offset_X1 - radius * Math.sin(45)));//起点
        path.lineTo((float) (offset_X3 + radius * Math.cos(45)), (float) (offset_X1 + radius * Math.sin(45)));
        path.quadTo(centerX, centerY, (float) (offset_X4 + radius * Math.cos(45)), (float) (offset_X0 + radius * Math.sin(45)));
        path.lineTo((float) (offset_X4 - radius * Math.cos(45)), (float) (offset_X0 - radius * Math.sin(45)));
        path.quadTo(centerX, centerY, (float) (offset_X3 - radius * Math.cos(45)), (float) (offset_X1 - radius * Math.sin(45)));
        canvas.drawPath(path, loadingPaint_1);
        path.reset();

        centerX = (offset_X0 + offset_X1) / 2f;
        centerY = (offset_X4 + offset_X3) / 2f;
        path.moveTo(offset_X0 - (float) (radius * Math.cos(45)), (float) (offset_X4 - radius * Math.sin(45)));//起点
        path.lineTo((float) (offset_X0 + radius * Math.cos(45)), (float) (offset_X4 + radius * Math.sin(45)));
        path.quadTo(centerX, centerY, (float) (offset_X1 + radius * Math.cos(45)), (float) (offset_X3 + radius * Math.sin(45)));
        path.lineTo((float) (offset_X1 - radius * Math.cos(45)), (float) (offset_X3 - radius * Math.sin(45)));
        path.quadTo(centerX, centerY, (float) (offset_X0 - radius * Math.cos(45)), (float) (offset_X4 - radius * Math.sin(45)));
        canvas.drawPath(path, loadingPaint_1);
        path.reset();

        centerX = (offset_X1 + offset_X2) / 2f;
        centerY = (offset_X3 + offset_X2) / 2f;
        path.moveTo(offset_X1 - (float) (radius * Math.cos(45)), (float) (offset_X3 - radius * Math.sin(45)));//起点
        path.lineTo((float) (offset_X1 + radius * Math.cos(45)), (float) (offset_X3 + radius * Math.sin(45)));
        path.quadTo(centerX, centerY, (float) (offset_X2 + radius * Math.cos(45)), (float) (offset_X2 + radius * Math.sin(45)));
        path.lineTo((float) (offset_X2 - radius * Math.cos(45)), (float) (offset_X2 - radius * Math.sin(45)));
        path.quadTo(centerX, centerY, (float) (offset_X1 - radius * Math.cos(45)), (float) (offset_X3 - radius * Math.sin(45)));
        canvas.drawPath(path, loadingPaint_1);
        path.reset();

        centerX = (offset_X3 + offset_X2) / 2f;
        centerY = (offset_X3 + offset_X4) / 2f;
        path.moveTo(offset_X3 - (float) (radius * Math.cos(45)), (float) (offset_X3 - radius * Math.sin(45)));//起点
        path.lineTo((float) (offset_X3 + radius * Math.cos(45)), (float) (offset_X3 + radius * Math.sin(45)));
        path.quadTo(centerX, centerY, (float) (offset_X2 + radius * Math.cos(45)), (float) (offset_X4 + radius * Math.sin(45)));
        path.lineTo((float) (offset_X2 - radius * Math.cos(45)), (float) (offset_X4 - radius * Math.sin(45)));
        path.quadTo(centerX, centerY, (float) (offset_X3 - radius * Math.cos(45)), (float) (offset_X3 - radius * Math.sin(45)));
        canvas.drawPath(path, loadingPaint_2);
        path.reset();

    }

    private void drawEnding(Canvas canvas) {
        if (duration <= LOADING_1) {//第一阶段
            pointPaint_S.setAlpha((int) (255f * duration / LOADING_1));
            pointPaint_M.setAlpha((int) (255f * duration / LOADING_1));
            pointPaint_L.setAlpha((int) (255f * duration / LOADING_1));
            canvas.drawCircle(offset_X0 + (offset_X1 - padding) * duration / LOADING_1, offset_X0 + (offset_X1 - padding) * duration / LOADING_1, radius, pointPaint_S);
            canvas.drawCircle(offset_X2 - (offset_X1 - padding) * duration / LOADING_1, offset_X0 + (offset_X1 - padding) * duration / LOADING_1, radius, pointPaint_M);
            canvas.drawCircle(offset_X4 - (offset_X1 - padding) * duration / LOADING_1, offset_X0 + (offset_X1 - padding) * duration / LOADING_1, radius, pointPaint_L);

            canvas.drawCircle(offset_X0 + (offset_X1 - padding) * duration / LOADING_1, offset_X4 - (offset_X1 - padding) * duration / LOADING_1, radius, pointPaint_L);
            canvas.drawCircle(offset_X2 + (offset_X1 - padding) * duration / LOADING_1, offset_X4 - (offset_X1 - padding) * duration / LOADING_1, radius, pointPaint_M);
            canvas.drawCircle(offset_X4 - (offset_X1 - padding) * duration / LOADING_1, offset_X4 - (offset_X1 - padding) * duration / LOADING_1, radius, pointPaint_S);


            pointPaint_S.setAlpha(255);
            pointPaint_M.setAlpha(255);
            pointPaint_L.setAlpha(255);
            canvas.drawCircle(offset_X2, offset_X2, radius, pointPaint_L);
            canvas.drawCircle(offset_X1, offset_X1, radius, pointPaint_M);
            canvas.drawCircle(offset_X1, offset_X3, radius, pointPaint_M);
            canvas.drawCircle(offset_X3, offset_X1, radius, pointPaint_L);
            canvas.drawCircle(offset_X3, offset_X3, radius, pointPaint_M);
        } else if (duration >= LOADING_1 && duration <= LOADING_2) {

            pointPaint_S.setAlpha((int) (255f * (duration - LOADING_1) / (LOADING_2 - LOADING_1)));
            pointPaint_M.setAlpha((int) (255f * (duration - LOADING_1) / (LOADING_2 - LOADING_1)));
            pointPaint_L.setAlpha((int) (255f * (duration - LOADING_1) / (LOADING_2 - LOADING_1)));

            canvas.drawCircle(offset_X1 + (offset_X1 - padding) * (duration - LOADING_1) / (LOADING_2 - LOADING_1), offset_X1 + (offset_X1 - padding) * (duration - LOADING_1) / (LOADING_2 - LOADING_1), radius, pointPaint_M);
            canvas.drawCircle(offset_X1 + (offset_X1 - padding) * (duration - LOADING_1) / (LOADING_2 - LOADING_1), offset_X3 - (offset_X1 - padding) * (duration - LOADING_1) / (LOADING_2 - LOADING_1), radius, pointPaint_M);
            canvas.drawCircle(offset_X3 - (offset_X1 - padding) * (duration - LOADING_1) / (LOADING_2 - LOADING_1), offset_X1 + (offset_X1 - padding) * (duration - LOADING_1) / (LOADING_2 - LOADING_1), radius, pointPaint_L);
            canvas.drawCircle(offset_X3 - (offset_X1 - padding) * (duration - LOADING_1) / (LOADING_2 - LOADING_1), offset_X3 - (offset_X1 - padding) * (duration - LOADING_1) / (LOADING_2 - LOADING_1), radius, pointPaint_M);

            canvas.drawCircle(offset_X2, offset_X2, radius, pointPaint_L);

            pointPaint_S.setAlpha(255);
            pointPaint_M.setAlpha(255);
            pointPaint_L.setAlpha(255);

        } else if (duration > LOADING_2) {
            pointPaint_L.setAlpha((int) (255f * (TOTAL_TIME - duration) / (TOTAL_TIME - LOADING_2)));
            canvas.drawCircle(offset_X2, offset_X2, radius * (TOTAL_TIME - duration) / (TOTAL_TIME - LOADING_2), pointPaint_L);
            pointPaint_L.setAlpha(255);
        }
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, widthMeasureSpec);//设置成高度和宽度一样
        measureWidth = getWidth();
        width = measureWidth - 2 * padding;
        offset_X0 = padding;
        offset_X1 = width / 4 + padding;
        offset_X2 = width / 2 + padding;
        offset_X3 = width / 4 * 3 + padding;
        offset_X4 = width + padding;
    }

    public void startDraw() {
        if (!startAnimation.isRunning())
            startAnimation.start();
    }


    public void loading() {
        if (!drawingAnimation.isRunning())
            drawingAnimation.start();
    }

    public void finish() {
        if (!endAnimation.isRunning())
            endAnimation.start();
    }

    /**
     * 设置第一个点的位置
     *
     * @param canvas
     */
    private void drawFirstPoint(Canvas canvas) {
        canvas.drawCircle(offset_X2, offset_X2, (float) (1f * width * Math.max(1 - percent, 0.2) / 2), pointPaint_L);
    }

    /**
     * 设置绘制的阶段状态
     *
     * @param status 阶段状态
     */
    public void setStatus(Status status) {
        this.drawStatus = status;
    }

    @Override
    public View getView() {
        return this;
    }

    @Override
    public void onShow(float percent) {
        this.percent = percent;
        if (!loading && stop) {
            setStatus(Status.START);
            postInvalidate();
        }
    }

    @Override
    public void onClose(float percent) {
//        if (percent == 0) duration = 0;
    }

    @Override
    public void onLoading() {
        Logger.d("onLoading");
        startDraw();
        loading = true;
        stop = false;
    }

    @Override
    public void onStop() {
        finish();
        loading = false;
    }

    public enum Status {
        START,//开始状态
        DRAWING,//绘制状态
        LOADING,//加载状态
        ENDING//结束状态
    }

}