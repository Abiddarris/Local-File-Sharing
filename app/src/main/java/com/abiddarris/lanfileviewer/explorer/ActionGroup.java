package com.abiddarris.lanfileviewer.explorer;

import android.animation.ValueAnimator;
import android.view.View;
import android.widget.RelativeLayout;

public class ActionGroup {

    private RelativeLayout bottomLayout;

    public ActionGroup(RelativeLayout bottomLayout) {
        this.bottomLayout = bottomLayout;
    }
    
    public void show() {
        bottomLayout.setVisibility(View.VISIBLE);
        ValueAnimator animator = ValueAnimator.ofFloat(bottomLayout.getY() - bottomLayout.getHeight(),bottomLayout.getY());
        animator.setDuration(500);
        animator.addUpdateListener((vAnimator) -> {
            bottomLayout.setY((float)vAnimator.getAnimatedValue());
        });
        animator.start();
    }
    
    public void hide() {
        ValueAnimator animator = ValueAnimator.ofFloat(bottomLayout.getY(), bottomLayout.getY() - bottomLayout.getHeight());
        animator.setDuration(500);
        animator.addUpdateListener((vAnimator) -> {
            bottomLayout.setY((float)vAnimator.getAnimatedValue());
        });
        animator.start();
        bottomLayout.setVisibility(View.GONE);
    }
}
