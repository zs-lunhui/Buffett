package com.buffett.pulltorefresh.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;

import com.buffett.pulltorefresh.R;
import com.buffett.pulltorefresh.core.RefreshView;

/**
 * Created by wscn20151202 on 16/6/21.
 */
public class MyRefreshView extends RelativeLayout implements RefreshView{
    public MyRefreshView(Context context) {
        this(context,null);
    }

    public MyRefreshView(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }

    public MyRefreshView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        inflate(context,R.layout.header_layout,this);
    }

    @Override
    public View getView() {
        return this;
    }

    @Override
    public void onPercent(float percent) {
        getView().setScaleY((float) Math.min(1,0.5+percent*0.5));
        getView().setScaleX((float) Math.min(1,0.5+percent*0.5));
        Log.d("onPercentChange",percent+"");
    }


}
