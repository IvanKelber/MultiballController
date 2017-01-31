package com.ivankelber.multiballcontroller;

import android.view.View;

/**
 * Created by ivankelber on 1/31/17.
 */
public interface HoldListener {
    void onRelease(View v);
    void onHeld(View v);
}