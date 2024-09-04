package com.james.imagereader;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import java.lang.ref.WeakReference;

public class ScrollRecyclerView extends RecyclerView {
    private AudoRunnable autoRun;
    private boolean running;
    private boolean canrun;

    private static final int delayTime = 40;//控制滚动的速度，值越大速度越慢

    public ScrollRecyclerView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        autoRun = new AudoRunnable(this);
    }

    public ScrollRecyclerView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    private static class AudoRunnable implements Runnable {
        WeakReference<ScrollRecyclerView> myScrViewWeakReference;

        public AudoRunnable(ScrollRecyclerView myScrView) {
            myScrViewWeakReference = new WeakReference<>(myScrView);
        }

        @Override
        public void run() {
            ScrollRecyclerView scrollRecyclerView = myScrViewWeakReference.get();
            if (scrollRecyclerView.canrun && scrollRecyclerView.running) {
                scrollRecyclerView.scrollBy(2, 2);
                scrollRecyclerView.postDelayed(scrollRecyclerView.autoRun, delayTime);
            }
        }
    }

    //开始滚动
    public void start() {
        if (running) {
            stop();
        }
        running = true;
        canrun = true;
        postDelayed(autoRun, delayTime);
    }

    //停止滚动
    public void stop() {
        running = false;
        removeCallbacks(autoRun);
    }
}