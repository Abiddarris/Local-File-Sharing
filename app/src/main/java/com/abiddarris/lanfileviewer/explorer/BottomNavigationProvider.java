package com.abiddarris.lanfileviewer.explorer;

import android.animation.ValueAnimator;
import android.view.View;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.gretta.util.log.Log;

public class BottomNavigationProvider {
    
    private BottomNavigationView bottomNavigation;
    
    public BottomNavigationProvider(BottomNavigationView bottomNavigation) {
        this.bottomNavigation = bottomNavigation;
    }
    
    public void show() {
        onNavigationProviderShown(bottomNavigation);
        bottomNavigation.setVisibility(View.VISIBLE);
        ValueAnimator animator = ValueAnimator.ofFloat(bottomNavigation.getY() - bottomNavigation.getHeight(),bottomNavigation.getY());
        animator.setDuration(500);
        animator.addUpdateListener((vAnimator) -> {
            Log.debug.log("anim", vAnimator.getAnimatedValue());    
            bottomNavigation.setY((float)vAnimator.getAnimatedValue());
        });
        animator.start();
    }
    
    public void hide() {
        ValueAnimator animator = ValueAnimator.ofFloat(bottomNavigation.getY(), bottomNavigation.getY() - bottomNavigation.getHeight());
        animator.setDuration(500);
        animator.addUpdateListener((vAnimator) -> {
            bottomNavigation.setY((float)vAnimator.getAnimatedValue());
        });
        animator.start();
        //bottomNavigation.setVisibility(View.GONE);
    }
    
    public void onNavigationProviderShown(BottomNavigationView view) {}
    
}
