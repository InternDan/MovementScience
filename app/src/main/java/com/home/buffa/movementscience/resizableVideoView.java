package com.home.buffa.movementscience;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.VideoView;

public class resizableVideoView extends VideoView {

    private int mForceHeight = 0;
    private int mForceWidth = 0;
    public resizableVideoView(Context context) {
        super(context);
    }

    public resizableVideoView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public resizableVideoView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void setDimensions(int w, int h) {
        this.mForceHeight = h;
        this.mForceWidth = w;

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        Log.i("@@@@", "onMeasure");

        setMeasuredDimension(mForceWidth, mForceHeight);
    }
}
