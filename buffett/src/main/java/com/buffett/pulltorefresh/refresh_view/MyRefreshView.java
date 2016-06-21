package com.buffett.pulltorefresh.refresh_view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Animatable;

import com.buffett.pulltorefresh.core.PullToRefreshView;


/**
 * Created by wscn20151202 on 16/6/17.
 */
public class MyRefreshView extends BaseRefreshView implements Animatable {


    public MyRefreshView(Context context, PullToRefreshView layout) {
        super(context, layout);
    }

    @Override
    public void setPercent(float percent, boolean invalidate) {

    }

    @Override
    public void offsetTopAndBottom(int offset) {

    }

    @Override
    public void start() {

    }

    @Override
    public void stop() {

    }

    @Override
    public boolean isRunning() {
        return false;
    }

    @Override
    public void draw(Canvas canvas) {

    }
}
