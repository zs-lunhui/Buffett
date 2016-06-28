package com.buffett.pulltorefresh.refresh_view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import com.buffett.pulltorefresh.R;
import com.buffett.pulltorefresh.core.RefreshView;
import com.buffett.pulltorefresh.model.DotView;

import java.util.ArrayList;

/**
 * Created by wscn20151202 on 16/6/27.
 */
public class DotAnimationView extends View implements RefreshView{

    private ArrayList<DotView> dots;
    private int height;
    private int width;
    private boolean isFinished;
    Paint paint;

    public DotAnimationView(Context context) {
        super(context);
        dots = new ArrayList<>();
        DotView dotView = new DotView(width/2.0f,height/2.0f,height/2.2f,1.0f, R.color.sienna);
        dots.add(dotView);
    }

    public DotAnimationView(Context context, AttributeSet attrs) {
        super(context, attrs);
        dots = new ArrayList<>();
        DotView dotView = new DotView(width/2.0f,height/2.0f,height/2.2f,1.0f, R.color.sienna);
        dots.add(dotView);
    }

    public DotAnimationView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        dots = new ArrayList<>();
        DotView dotView = new DotView(width/2.0f,height/2.0f,height/2.2f,1.0f, R.color.sienna);
        dots.add(dotView);
    }

    @Override
    public void onLoading(){

    }

    @Override
    public void onStop() {

    }

    public void onFinish(){

    }

    public void onPull(float percent){
        if (percent<=0) return;
        float r = dots.get(0).getR();
        r = Math.min(r*0.1f,r*(1-percent));
        dots.get(0).setR(r);
        invalidate();
    }


    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
        if (null==paint)paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.FILL);
        while (!isFinished){
            for (DotView dot : dots){
                dot.onDraw(canvas,paint);
            }
        }
        invalidate();

    }

    @Override
    public View getView() {
        return this;
    }

    @Override
    public void onShow(float percent) {
//            onPull(percent);
    }

    @Override
    public void onClose(float percent) {

    }
}
