package com.abiddarris.lanfileviewer.explorer;

import android.animation.Animator;
import android.widget.RelativeLayout;
import android.view.View;
import android.animation.ValueAnimator;
import android.animation.AnimatorListenerAdapter;
import com.gretta.util.log.Log;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;

public abstract class BottomToolbarMode extends NavigateMode {

    public static final String TAG = Log.getTag(BottomToolbarMode.class);
    
    private float initialY;
    
    public BottomToolbarMode(Explorer explorer) {
        super(explorer);
    }

    @Override
    public void onModeSelected() {
        super.onModeSelected();

        showBottomBar();
    }
    
    @Override
    public void onModeDeselected() {
        super.onModeDeselected();
        
        hideBottomBar();
    }
    

    public abstract void onModifyOptionsCreated(RelativeLayout group);

    public void showBottomBar() {
        Log.debug.log(TAG, "Showing Bottom actions");
        RelativeLayout group = getExplorer().getUI().bottomAction;
        group.removeAllViews();
        
        onModifyOptionsCreated(group);

        group.setVisibility(View.VISIBLE);
        group.getViewTreeObserver()
                .addOnGlobalLayoutListener(
                        new OnGlobalLayoutListener() {
                            @Override
                            public void onGlobalLayout() {
                                initialY = group.getY();
                                ValueAnimator animator =
                                        ValueAnimator.ofFloat(
                                                group.getY() + group.getHeight(), group.getY());
                                animator.setDuration(500);
                                animator.addUpdateListener(
                                        (vAnimator) -> {
                                            Log.debug.log(TAG, "Showing animation : " + vAnimator.getAnimatedValue());
                                            group.setY((float) vAnimator.getAnimatedValue());
                                        });
                                animator.addListener(
                                    new AnimatorListenerAdapter() {
                                        @Override
                                        public void onAnimationEnd(Animator animator) {
                                            super.onAnimationEnd(animator);
    
                                            group.setVisibility(View.VISIBLE);
                                        }
                                });
                                animator.start();
                                group.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                            }
                        });
    }

    public void hideBottomBar() {
        Log.debug.log(TAG, "Hiding Bottom actions");
        
        RelativeLayout group = getExplorer().getUI().bottomAction;
        
        ValueAnimator animator = ValueAnimator.ofFloat(group.getY(), initialY);
        animator.setDuration(500);
        animator.addUpdateListener(
                (vAnimator) -> {
                    Log.debug.log(TAG, "Hide animation : " + vAnimator.getAnimatedValue());
                    group.setY((float) vAnimator.getAnimatedValue());
                });
        animator.addListener(
                new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animator) {
                        super.onAnimationEnd(animator);

                        group.setY(initialY);
                        group.removeAllViews();
                        group.setVisibility(View.GONE);
                    }
                });
        animator.start();
    }

}
