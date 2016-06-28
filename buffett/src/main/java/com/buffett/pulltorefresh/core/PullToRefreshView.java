package com.buffett.pulltorefresh.core;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Animatable;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.Transformation;
import android.widget.AbsListView;
import android.widget.FrameLayout;

import com.buffett.pulltorefresh.R;
import com.buffett.pulltorefresh.util.Utils;

public class PullToRefreshView extends ViewGroup implements Animatable {

    private int drag_max_distance = 100; //阻力最大距离

    public void setDrag_max_distance(int drag_max_distance) {
        this.drag_max_distance = drag_max_distance;
    }

    private float drag_rate = 0.5f; //阻率

    public void setDrag_rate(float drag_rate) {
        this.drag_rate = drag_rate;
    }

    private float decelerate_interpolation_factor = 2f;//减速差值因子

    public void setDecelerate_interpolation_factor(float decelerate_interpolation_factor) {
        this.decelerate_interpolation_factor = decelerate_interpolation_factor;
    }

    public static final int STYLE_SUN = 0;

    public int max_offset_animation_duration = 1000; //最大偏移动画持续时间

    public void setMax_offset_animation_duration(int max_offset_animation_duration) {
        this.max_offset_animation_duration = max_offset_animation_duration;
    }

    private static final int INVALID_POINTER = -1;//无效的点

    private Context mContext;
    private View mTarget;
    private Interpolator mDecelerateInterpolator;
    private int mTouchSlop;
    private int mTotalDragDistance;
    private float mCurrentDragPercent;
    private int mCurrentOffsetTop;
    private boolean mRefreshing;
    private int mActivePointerId;
    private boolean mIsBeingDragged;
    private float mInitialMotionY;
    private int mFrom;
    private float mFromDragPercent;
    private boolean mNotify;
    private OnRefreshListener mListener;

    private int mTargetPaddingTop;
    private int mTargetPaddingBottom;
    private int mTargetPaddingRight;
    private int mTargetPaddingLeft;

    private FrameLayout refreshContainer;
    private RefreshView refreshView;
//    private RefreshView refreshView;

    private boolean cleckRefresh;

    public PullToRefreshView(Context context) {
        super(context);
        mContext = context;

    }

    public PullToRefreshView(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.RefreshView);
        a.recycle();
        mContext = context;
        refreshContainer = new FrameLayout(context);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, drag_max_distance);
        refreshContainer.setLayoutParams(params);

        mDecelerateInterpolator = new DecelerateInterpolator(decelerate_interpolation_factor);
        mTouchSlop = ViewConfiguration.get(mContext).getScaledTouchSlop();
        mTotalDragDistance = Utils.convertDpToPixel(mContext, drag_max_distance);
        addView(refreshContainer);
        setWillNotDraw(false);
        ViewCompat.setChildrenDrawingOrderEnabled(this, true);
    }

    public void setRefreshView(RefreshView refreshView) {
        this.refreshView = refreshView;
        if (this != null) {
            refreshContainer.removeAllViews();
            refreshContainer.addView(this.refreshView.getView());
        }
    }

    public int getTotalDragDistance() {
        return mTotalDragDistance;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        ensureTarget();
        if (mTarget == null)
            return;

        widthMeasureSpec = MeasureSpec.makeMeasureSpec(getMeasuredWidth() - getPaddingRight() - getPaddingLeft(), MeasureSpec.EXACTLY);
        heightMeasureSpec = MeasureSpec.makeMeasureSpec(getMeasuredHeight() - getPaddingTop() - getPaddingBottom(), MeasureSpec.EXACTLY);
        mTarget.measure(widthMeasureSpec, heightMeasureSpec);
        refreshContainer.measure(widthMeasureSpec, heightMeasureSpec);
    }

    private void ensureTarget() {
        if (mTarget != null)
            return;
        if (getChildCount() > 0) {
            for (int i = 0; i < getChildCount(); i++) {
                View child = getChildAt(i);
                if (child != refreshContainer) {
                    mTarget = child;
                    mTargetPaddingBottom = mTarget.getPaddingBottom();
                    mTargetPaddingLeft = mTarget.getPaddingLeft();
                    mTargetPaddingRight = mTarget.getPaddingRight();
                    mTargetPaddingTop = mTarget.getPaddingTop();
                }
            }
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (mRefreshing) {
            final int action = MotionEventCompat.getActionMasked(ev);
            switch (action) {
                case MotionEvent.ACTION_DOWN:
                    mActivePointerId = MotionEventCompat.getPointerId(ev, 0);
                    mIsBeingDragged = false;
                    final float initialMotionY = getMotionEventY(ev, mActivePointerId);
                    if (initialMotionY == -1) {
                        return false;
                    }
                    mInitialMotionY = initialMotionY;
                    break;
                case MotionEvent.ACTION_MOVE: {

                    final int pointerIndex = MotionEventCompat.findPointerIndex(ev, mActivePointerId);
                    final float y = MotionEventCompat.getY(ev, pointerIndex);
                    final float yDiff = y - mInitialMotionY;
                    final float scrollTop = yDiff * drag_rate;
                    float percent = Math.max(scrollTop / mTotalDragDistance, -1);
                    mCurrentDragPercent = mCurrentOffsetTop / mTotalDragDistance;
                    if (percent > 0) break;
                    Log.d("setTargetOffsetTop", percent + " , " + mCurrentOffsetTop * 1.0 / mTotalDragDistance + " , " + (percent - 1) * mTotalDragDistance + mCurrentOffsetTop + " , " + mCurrentOffsetTop);
                    int targetY = (int) (mTotalDragDistance * (1 + percent));
                    setTargetOffsetTop((int) (targetY - mCurrentOffsetTop), true);

                }
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL: {
                    Log.d("ACTION_CANCEL", "ACTION_CANCEL");
                    if (mCurrentOffsetTop < mTotalDragDistance / 2) {
                        animateOffsetToStartPosition();
                    } else {
                        animateOffsetToCorrectPosition();
                    }
                    break;
                }
            }
            Log.d("MotionEvent", "dispatchTouchEvent true");
            return true;
        }
        Log.d("MotionEvent", "dispatchTouchEvent false");
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {

        if (!isEnabled() || canChildScrollUp() || mRefreshing) {
            return false;
        }

        final int action = MotionEventCompat.getActionMasked(ev);
        Log.d("MotionEvent", "onInterceptTouchEvent");
        if (mRefreshing) return true;
        Log.d("事件拦截", "onTouchEvent");
        switch (action) {
            case MotionEvent.ACTION_DOWN:
//                setTargetOffsetTop(0, true);
                mActivePointerId = MotionEventCompat.getPointerId(ev, 0);
                mIsBeingDragged = false;
                final float initialMotionY = getMotionEventY(ev, mActivePointerId);
                if (initialMotionY == -1) {
                    return false;
                }
                mInitialMotionY = initialMotionY;
                break;
            case MotionEvent.ACTION_MOVE:
                if (mActivePointerId == INVALID_POINTER) {
                    return false;
                }
                final float y = getMotionEventY(ev, mActivePointerId);
                if (y == -1) {
                    return false;
                }
                final float yDiff = y - mInitialMotionY;
                if (yDiff > mTouchSlop && !mIsBeingDragged) {
                    mIsBeingDragged = true;
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                mIsBeingDragged = false;
                mActivePointerId = INVALID_POINTER;
                break;
            case MotionEventCompat.ACTION_POINTER_UP:
                onSecondaryPointerUp(ev);
                break;
        }

        return mIsBeingDragged;
    }

    @Override
    public boolean onTouchEvent(@NonNull MotionEvent ev) {

        if (!mIsBeingDragged) {
            return super.onTouchEvent(ev);
        }
        if (mRefreshing) return true;
        Log.d("事件拦截", "onTouchEvent");
        final int action = MotionEventCompat.getActionMasked(ev);

        switch (action) {
            case MotionEvent.ACTION_MOVE: {
                final int pointerIndex = MotionEventCompat.findPointerIndex(ev, mActivePointerId);
                if (pointerIndex < 0) {
                    return false;
                }
                final float y = MotionEventCompat.getY(ev, pointerIndex);
                final float yDiff = y - mInitialMotionY;
                final float scrollTop = yDiff * drag_rate;
                mCurrentDragPercent = scrollTop / mTotalDragDistance;

//                    float boundedDragPercent = Math.max(-1, mCurrentDragPercent);
//                    float extraOS = scrollTop + mTotalDragDistance;
//                    float slingshotDist = mTotalDragDistance;
//                    float tensionSlingshotPercent = Math.max(0,
//                            Math.min(extraOS, slingshotDist ) / slingshotDist);
//                    float tensionPercent = (float) ((tensionSlingshotPercent / 4) - Math.pow(
//                            (tensionSlingshotPercent / 4), 2)) * 2f;
//                    float extraMove = (slingshotDist) * tensionPercent / 2;
//                    int targetY = (int) ((slingshotDist * boundedDragPercent) + extraMove);
//                    refreshView.onClose(mCurrentDragPercent);
//                    setTargetOffsetTop(targetY - mCurrentOffsetTop, true);
//                    break;
                if (!mRefreshing) {
                    Log.d("mRefreshing", mRefreshing + "");
                    float boundedDragPercent = Math.min(1f, Math.abs(mCurrentDragPercent));
                    float extraOS = Math.abs(scrollTop) - mTotalDragDistance;
                    float slingshotDist = mTotalDragDistance;
                    float tensionSlingshotPercent = Math.max(0,
                            Math.min(extraOS, slingshotDist) / slingshotDist);
                    float tensionPercent = (float) ((tensionSlingshotPercent / 4) - Math.pow(
                            (tensionSlingshotPercent / 4), 2)) * 2f;
                    float extraMove = (slingshotDist) * tensionPercent / 2;
                    int targetY = (int) ((slingshotDist * boundedDragPercent) + extraMove);
                    refreshView.onShow(mCurrentDragPercent);
                    Log.d("onTouchEvent MOVE", "extraOS=" + extraOS + ", boundedDragPercent="
                            + boundedDragPercent + ", tensionSlingshotPercent=" + tensionSlingshotPercent + ", tensionPercent=" + tensionPercent
                            + ", extraMove=" + extraMove + ", targetY=" + targetY + ",offect=" + (targetY - mCurrentOffsetTop) + "");
                    setTargetOffsetTop(targetY - mCurrentOffsetTop, true);
                }
                break;
            }
            case MotionEventCompat.ACTION_POINTER_DOWN:
                final int index = MotionEventCompat.getActionIndex(ev);
                mActivePointerId = MotionEventCompat.getPointerId(ev, index);
                break;
            case MotionEventCompat.ACTION_POINTER_UP:
                onSecondaryPointerUp(ev);
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL: {
                if (mActivePointerId == INVALID_POINTER) {
                    return false;
                }
                if (!mRefreshing) {
                    final int pointerIndex = MotionEventCompat.findPointerIndex(ev, mActivePointerId);
                    final float y = MotionEventCompat.getY(ev, pointerIndex);
                    final float overScrollTop = (y - mInitialMotionY) * drag_rate;
                    mIsBeingDragged = false;
                    if (overScrollTop > mTotalDragDistance) {
                        setRefreshing(true);

                    } else {
//                        mRefreshing = false;
                        animateOffsetToStartPosition();
                    }
                }
                mActivePointerId = INVALID_POINTER;
                return false;
            }
        }

        return true;
    }

    private void animateOffsetToStartPosition() {
        mFrom = mCurrentOffsetTop;
        mFromDragPercent = mFrom * 1.0f / mTotalDragDistance;
        long animationDuration = Math.abs((long) (max_offset_animation_duration * mFromDragPercent));

        mAnimateToStartPosition.reset();
        mAnimateToStartPosition.setDuration(animationDuration);
        mAnimateToStartPosition.setInterpolator(mDecelerateInterpolator);
        mAnimateToStartPosition.setAnimationListener(mToStartListener);
        refreshContainer.clearAnimation();
        refreshContainer.startAnimation(mAnimateToStartPosition);
    }

    private void animateOffsetToCorrectPosition() {
        mFrom = mCurrentOffsetTop;
        mFromDragPercent = mCurrentDragPercent;

        mAnimateToCorrectPosition.reset();
        mAnimateToCorrectPosition.setDuration(max_offset_animation_duration);
        mAnimateToCorrectPosition.setInterpolator(mDecelerateInterpolator);
        refreshContainer.clearAnimation();
        refreshContainer.startAnimation(mAnimateToCorrectPosition);
        if (mRefreshing) {
            start();
            if (mNotify) {
                if (mListener != null) {
                    mListener.onRefresh();
                }
            }
        } else {
            stop();
            animateOffsetToStartPosition();
        }
        mCurrentOffsetTop = mTarget.getTop();
        mTarget.setPadding(mTargetPaddingLeft, mTargetPaddingTop, mTargetPaddingRight, mTotalDragDistance);
    }

    private final Animation mAnimateToStartPosition = new Animation() {
        @Override
        public void applyTransformation(float interpolatedTime, Transformation t) {
            moveToStart(interpolatedTime);
        }
    };

    private final Animation mAnimateToCorrectPosition = new Animation() {
        @Override
        public void applyTransformation(float interpolatedTime, Transformation t) {
            int targetTop;
            int endTarget = mTotalDragDistance;
            targetTop = (mFrom + (int) ((endTarget - mFrom) * interpolatedTime));
            int offset = targetTop - mTarget.getTop();
            mCurrentDragPercent = mFromDragPercent - (mFromDragPercent - 1.0f) * interpolatedTime;
            if (!mRefreshing) {
                refreshView.onShow(mCurrentDragPercent);
            }
            int targetOffetset = (int) (offset * interpolatedTime);
            Log.d("interpolatedTime", interpolatedTime + " , " + targetOffetset);
            setTargetOffsetTop(targetOffetset + 1, false /* requires update */);
        }
    };

    private void moveToStart(float interpolatedTime) {
        if (!mRefreshing) {
            int targetTop = mFrom - (int) (mFrom * interpolatedTime);
            float targetPercent = mFromDragPercent * (1.0f - interpolatedTime);
            int offset = targetTop - mTarget.getTop();
            mCurrentDragPercent = targetPercent;
            refreshView.onShow(mCurrentDragPercent);
            mTarget.setPadding(mTargetPaddingLeft, mTargetPaddingTop, mTargetPaddingRight, mTargetPaddingBottom);
            setTargetOffsetTop(offset, true);
        } else {
            int targetTop = mFrom - (int) (mFrom * interpolatedTime);
            int offset = targetTop - mTarget.getTop();
            Log.d("moveToStart", interpolatedTime + " , " + offset);
            setTargetOffsetTop(offset, true);
        }

//        mRefreshing = false;
        mNotify = false;

    }

    public void setRefreshing(boolean refreshing) {
//        setRefreshing(refreshing, refreshing /* notify */);
        if (mRefreshing && refreshing) return;
        mNotify = refreshing;
        mRefreshing = refreshing;
        cleckRefresh = refreshing;
        ensureTarget();
        if (mRefreshing) {
            animateOffsetToCorrectPosition();
            refreshView.onLoading();
        } else {
            postDelayed(new Runnable() {
                @Override
                public void run() {
                    animateOffsetToStartPosition();
                }
            }, 300);
            refreshView.onStop();
        }
    }

//    public void setRefreshing(boolean refreshing, final boolean notify) {
//        if (mRefreshing != refreshing) {
//            mNotify = notify;
//            ensureTarget();
//            mRefreshing = refreshing;
//            if (mRefreshing) {
////                refreshView.onPercent(1);
//                animateOffsetToCorrectPosition();
//            } else {
//                animateOffsetToStartPosition();
//            }
//        }
//    }

    private Animation.AnimationListener mToStartListener = new Animation.AnimationListener() {
        @Override
        public void onAnimationStart(Animation animation) {
        }

        @Override
        public void onAnimationRepeat(Animation animation) {
        }

        @Override
        public void onAnimationEnd(Animation animation) {
            stop();
            mCurrentOffsetTop = mTarget.getTop();
        }
    };

    private void onSecondaryPointerUp(MotionEvent ev) {
        final int pointerIndex = MotionEventCompat.getActionIndex(ev);
        final int pointerId = MotionEventCompat.getPointerId(ev, pointerIndex);
        if (pointerId == mActivePointerId) {
            final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
            mActivePointerId = MotionEventCompat.getPointerId(ev, newPointerIndex);
        }
    }

    private float getMotionEventY(MotionEvent ev, int activePointerId) {
        final int index = MotionEventCompat.findPointerIndex(ev, activePointerId);
        if (index < 0) {
            return -1;
        }
        return MotionEventCompat.getY(ev, index);
    }

    private void setTargetOffsetTop(int offset, boolean requiresUpdate) {
//        if (mCurrentOffsetTop<0 || mCurrentOffsetTop>mTotalDragDistance+50) return;
        mTarget.offsetTopAndBottom(offset);
        mCurrentOffsetTop = mTarget.getTop();
        if (cleckRefresh) {
            refreshView.onShow(mCurrentOffsetTop * 1.0f / mTotalDragDistance);
        }
        if (requiresUpdate && Build.VERSION.SDK_INT < 11) {
            invalidate();
        }
        if (mCurrentOffsetTop == 0) {
            mRefreshing = false;
        }
        if (mCurrentOffsetTop == mTotalDragDistance) {
            cleckRefresh = false;
        }
    }

    private boolean canChildScrollUp() {
        if (Build.VERSION.SDK_INT < 14) {
            if (mTarget instanceof AbsListView) {
                final AbsListView absListView = (AbsListView) mTarget;
                return absListView.getChildCount() > 0
                        && (absListView.getFirstVisiblePosition() > 0 || absListView.getChildAt(0)
                        .getTop() < absListView.getPaddingTop());
            } else {
                return mTarget.getScrollY() > 0;
            }
        } else {
            return ViewCompat.canScrollVertically(mTarget, -1);
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {

        ensureTarget();
        if (mTarget == null)
            return;

        int height = getMeasuredHeight();
        int width = getMeasuredWidth();
        int left = getPaddingLeft();
        int top = getPaddingTop();
        int right = getPaddingRight();
        int bottom = getPaddingBottom();

        mTarget.layout(left, top + mCurrentOffsetTop, left + width - right, top + height - bottom + mTotalDragDistance + mCurrentOffsetTop);
        refreshContainer.layout(left, top, left + width - right, top + height - bottom);
    }

    public void setOnRefreshListener(OnRefreshListener listener) {
        mListener = listener;
    }

    @Override
    public void start() {

    }

    @Override
    public void stop() {
        refreshView.onStop();
    }

    @Override
    public boolean isRunning() {
        return false;
    }

    public interface OnRefreshListener {
        void onRefresh();
    }


}

