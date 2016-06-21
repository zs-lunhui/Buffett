package com.buffett.pulltorefresh.core;

import android.view.View;

/**
 * Created by wscn20151202 on 16/6/21.
 */
public interface  RefreshView{

    View getView();

    void onPercent(float percent);



}
