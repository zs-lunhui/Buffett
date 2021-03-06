package com.buffett.pulltorefresh.ui;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;

import com.buffett.pulltorefresh.PullToRefreshView;
import com.buffett.pulltorefresh.R;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Oleksii Shliama.
 */
public class ListViewFragment extends BaseRefreshFragment {

    private PullToRefreshView mPullToRefreshView;
    private int mVisibleLastIndex = 0;
    SampleAdapter mAdapter;
    ListView mListView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_list_view, container, false);

        mListView = (ListView) rootView.findViewById(R.id.list_view);
        mAdapter = new SampleAdapter(getActivity(), R.layout.list_item, mSampleList);
        mListView.setAdapter(mAdapter);

        mPullToRefreshView = (PullToRefreshView) rootView.findViewById(R.id.pull_to_refresh);
        mPullToRefreshView.setOnRefreshListener(new PullToRefreshView.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mPullToRefreshView.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mPullToRefreshView.setRefreshing(false);
                        refreshData();
                    }
                }, REFRESH_DELAY);
            }
        });

        mListView.setOnScrollListener(new AbsListView.OnScrollListener() {

            private int lastVisibleItemPosition;// 标记上次滑动位置

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem,
                                 int visibleItemCount, int totalItemCount) {
                mVisibleLastIndex = firstVisibleItem + visibleItemCount - 1;

                if (firstVisibleItem > lastVisibleItemPosition) {
                    Log.d("getOnScroll","UP");
                }

                if (firstVisibleItem < lastVisibleItemPosition) {
                    Log.d("getOnScroll","DOWN");
                }

                if (firstVisibleItem == lastVisibleItemPosition) {
                    return;
                }

                lastVisibleItemPosition = firstVisibleItem;
            }

            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                int itemsLastIndex = mAdapter.getCount() - 1; // 数据集最后一项的索引
                int lastIndex = itemsLastIndex
                        + mListView.getHeaderViewsCount() + mListView.getFooterViewsCount();

                if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE
                        && mVisibleLastIndex == lastIndex) {
                    addMoreData();

                } else {

                }
            }
        });

        return rootView;
    }

    class SampleAdapter extends ArrayAdapter<Map<String, Integer>> {

        public static final String KEY_ICON = "icon";
        public static final String KEY_COLOR = "color";

        private final LayoutInflater mInflater;
        private final List<Map<String, Integer>> mData;

        public SampleAdapter(Context context, int layoutResourceId, List<Map<String, Integer>> data) {
            super(context, layoutResourceId, data);
            mData = data;
            mInflater = LayoutInflater.from(context);
        }

        @Override
        public View getView(final int position, View convertView, @NonNull ViewGroup parent) {
            final ViewHolder viewHolder;
            if (convertView == null) {
                viewHolder = new ViewHolder();
                convertView = mInflater.inflate(R.layout.list_item, parent, false);
                viewHolder.imageViewIcon = (ImageView) convertView.findViewById(R.id.image_view_icon);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            viewHolder.imageViewIcon.setImageResource(mData.get(position).get(KEY_ICON));
            convertView.setBackgroundResource(mData.get(position).get(KEY_COLOR));

            return convertView;
        }

        class ViewHolder {
            ImageView imageViewIcon;
        }

    }

    public void addMoreData(){
        if (null == mAdapter) return;
        int[] icons = {
                R.drawable.icon_1,
                R.drawable.icon_2,
                R.drawable.icon_3};

        int[] colors = {
                R.color.saffron,
                R.color.eggplant,
                R.color.sienna};

        for (int i = 0; i < icons.length; i++) {
            Map<String, Integer> map = new HashMap<>();
            map.put(KEY_ICON, icons[i]);
            map.put(KEY_COLOR, colors[i]);
            mAdapter.mData.add(map);
        }
        mAdapter.notifyDataSetChanged();
    }

    public void refreshData(){
        if (null == mAdapter) return;
        mAdapter.clear();
        int[] icons = {
                R.drawable.icon_1,
                R.drawable.icon_2,
                R.drawable.icon_3};

        int[] colors = {
                R.color.saffron,
                R.color.eggplant,
                R.color.sienna};

        for (int i = 0; i < icons.length; i++) {
            Map<String, Integer> map = new HashMap<>();
            map.put(KEY_ICON, icons[i]);
            map.put(KEY_COLOR, colors[i]);
            mAdapter.mData.add(map);
        }
        mAdapter.notifyDataSetChanged();
    }
}
