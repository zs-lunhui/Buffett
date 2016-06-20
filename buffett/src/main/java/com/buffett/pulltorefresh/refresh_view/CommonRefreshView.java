package com.buffett.pulltorefresh.refresh_view;

import android.content.Context;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.buffett.pulltorefresh.R;


/**
 * Created by wscn20151202 on 16/6/20.
 */
public class CommonRefreshView extends LinearLayout{

    private View convertView;
    private TextView tv_header;
    private ImageView img_header;

    public CommonRefreshView(Context context) {
        super(context);
        convertView = LayoutInflater.from(context).inflate(R.layout.header_layout,this);
        tv_header = (TextView) convertView.findViewById(R.id.tv_header);
        img_header = (ImageView) convertView.findViewById(R.id.img_header);
    }

    public void setHeaderStr(String str_header){
        tv_header.setText(str_header);
    }

    public void setHeaderImg(String url){

    }

    public void setHeaderImg(int resId){
        img_header.setImageResource(resId);
    }

    public void setPercent(float percent){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            img_header.setScaleY((float) Math.min(1,0.5+percent*0.5));
            img_header.setScaleX((float) Math.min(1,0.5+percent*0.5));
            tv_header.setAlpha((float) Math.min(1,0.5+percent*0.5));
            Log.d("setPercent",Math.min(1,0.5+percent*0.5)+"--------"+percent);

        }


    }


    @Override
    public void clearAnimation() {
        super.clearAnimation();
        img_header.clearAnimation();
        tv_header.clearAnimation();
    }
}
