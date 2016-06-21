package com.buffett.pulltorefresh.ui;

import android.content.Context;
import android.view.View;

import com.buffett.pulltorefresh.refresh_view.CommonRefreshView;

/**
 * Created by wscn20151202 on 16/6/20.
 */
public class TextRefreshView extends CommonRefreshView{

    public TextRefreshView(Context context) {
        super(context);
        tv_header.setVisibility(View.GONE);
    }



}
