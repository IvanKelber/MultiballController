package com.ivankelber.multiballcontroller;

import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.View.OnClickListener;

/**
 * Created by ivankelber on 1/31/17.
 */
public class RepeatListener implements OnTouchListener{

    private Handler handler = new Handler();

    private int initialInterval;
    private final int regularInterval;
    private HoldListener holdListener;

    private Runnable handlerRunnable = new Runnable() {

        @Override
        public void run() {
            handler.postDelayed(this,regularInterval);
            holdListener.onHeld(heldView);
        }
    };

    private View heldView;

    public RepeatListener(int initialInterval,int regularInterval,HoldListener holdListener) {
        if (holdListener == null)
            throw new IllegalArgumentException("null runnable");
        if (initialInterval < 0 || regularInterval < 0)
            throw new IllegalArgumentException("negative interval");

        this.initialInterval = initialInterval;
        this.regularInterval = regularInterval;
        this.holdListener = holdListener;
    }



    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                //Held down
                handler.removeCallbacks(handlerRunnable);
                handler.postDelayed(handlerRunnable, initialInterval);
                heldView = v;
                heldView.setPressed(true);
                holdListener.onHeld(v);
                return true;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                //Released
                holdListener.onRelease(v);
                handler.removeCallbacks(handlerRunnable);
                heldView.setPressed(false);
                heldView = null;
                return true;
        }

        return false;
    }
}
