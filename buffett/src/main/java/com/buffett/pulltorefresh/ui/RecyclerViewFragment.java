package com.buffett.pulltorefresh.ui;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.buffett.pulltorefresh.core.PullToRefreshView;
import com.buffett.pulltorefresh.R;
import com.buffett.pulltorefresh.core.RefreshView;
import com.buffett.pulltorefresh.refresh_view.LoGo;
import com.buffett.pulltorefresh.util.Logger;

import java.util.Map;

/**
 * Created by Oleksii Shliama.
 */
public class RecyclerViewFragment extends BaseRefreshFragment {

    private PullToRefreshView mPullToRefreshView;

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_recycler_view, container, false);

        RecyclerView recyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        recyclerView.setAdapter(new SampleAdapter());

        mPullToRefreshView = (PullToRefreshView) rootView.findViewById(R.id.pull_to_refresh);
        mPullToRefreshView.setOnRefreshListener(new PullToRefreshView.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mPullToRefreshView.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mPullToRefreshView.setRefreshing(false);
                    }
                }, REFRESH_DELAY);
            }
        });
//        mPullToRefreshView.setRefreshView(new MyRefreshView(getActivity()));
        MyRefreshView refreshView = new MyRefreshView(getActivity());
        refreshView.setRefreshView(new RefreshView() {
            View view = inflater.inflate(R.layout.logo_layout, null);
            LoGo logo = (LoGo) view.findViewById(R.id.logo);

            @Override
            public View getView() {
//                RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
//                params.addRule(RelativeLayout.CENTER_IN_PARENT);
//                view.setLayoutParams(params);
                return view;
            }

            @Override
            public void onShow(float percent) {
//                view.setScaleY((float) Math.min(1, 0.9+percent*0.1));
//                view.setScaleX((float) Math.min(1, 0.9+percent*0.1));
//                view.setAlpha((float) Math.min(1, percent));

                logo.onShow(percent);
                Logger.d("pull:onShow");
            }


            @Override
            public void onClose(float percent) {
//                percent+=1;
//                view.setScaleY((float) Math.min(1, percent));
//                view.setScaleX((float) Math.min(1, percent));
//                view.setAlpha((float) Math.min(1, percent));
            }

            @Override
            public void onLoading() {
                Logger.d("pull:onLoading");
                logo.onLoading();
            }

            @Override
            public void onStop() {
                Logger.d("pull:onStop");
                logo.onStop();
            }
        });
        mPullToRefreshView.setRefreshView(refreshView);
        return rootView;
    }

    private class SampleAdapter extends RecyclerView.Adapter<SampleHolder> {

        @Override
        public SampleHolder onCreateViewHolder(ViewGroup parent, int pos) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.list_item, parent, false);
            return new SampleHolder(view);
        }

        @Override
        public void onBindViewHolder(SampleHolder holder, int pos) {
            Map<String, Integer> data = mSampleList.get(pos);
            holder.bindData(data);
        }

        @Override
        public int getItemCount() {
            return mSampleList.size();
        }
    }

    private class SampleHolder extends RecyclerView.ViewHolder {

        private View mRootView;
        private ImageView mImageViewIcon;

        private Map<String, Integer> mData;

        public SampleHolder(View itemView) {
            super(itemView);

            mRootView = itemView;
            mImageViewIcon = (ImageView) itemView.findViewById(R.id.image_view_icon);
        }

        public void bindData(Map<String, Integer> data) {
            mData = data;

            mRootView.setBackgroundResource(mData.get(KEY_COLOR));
            mImageViewIcon.setImageResource(mData.get(KEY_ICON));
        }
    }

}
