package com.buffett.pulltorefresh.model;

import android.graphics.Canvas;
import android.graphics.Paint;

/**
 * Created by wscn20151202 on 16/6/27.
 */
public class DotView {
    private float x;
    private float y;
    private float r;
    private float alpha;
    private int color;


    public DotView(float x, float y, float r, float alpha, int color) {
        this.x = x;
        this.y = y;
        this.r = r;
        this.alpha = alpha;
        this.color = color;
    }

    public float getX() {
        return x;
    }

    public void setX(float x) {
        this.x = x;
    }

    public float getY() {
        return y;
    }

    public void setY(float y) {
        this.y = y;
    }

    public float getR() {
        return r;
    }

    public void setR(float r) {
        this.r = r;
    }

    public float getAlpha() {
        return alpha;
    }

    public void setAlpha(float alpha) {
        this.alpha = alpha;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public void onDraw(Canvas canvas,Paint paint){
        paint.setAlpha((int) (alpha*255));
        paint.setColor(color);
        canvas.drawCircle(x,y,r,paint);
    }

}
