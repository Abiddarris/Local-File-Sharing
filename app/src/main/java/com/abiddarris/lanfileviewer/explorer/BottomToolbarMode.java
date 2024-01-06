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
    
    private boolean shown;
    private boolean animating;
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
    
    public boolean isShown() {
    	return shown;
    }

    public abstract void onBottomToolbarShown(RelativeLayout group);

    public void showBottomBar() {
        if(isShown() || animating) return;
        shown = true;
        animating = true;
        
        Log.debug.log(TAG, "Showing Bottom actions");
        RelativeLayout group = getExplorer()
            .getUI().bottomAction;
        group.removeAllViews();
        
        onBottomToolbarShown(group);
        
        group.setVisibility(View.VISIBLE);
        group.getViewTreeObserver()
                .addOnGlobalLayoutListener(new OnGroupVisible(group));
    }
    
    public void hideBottomBar() {
        hideBottomBar(true);
    }

    public void hideBottomBar(boolean withAnimation) {
        if(!isShown() || animating) return;
        
        Log.debug.log(TAG, "Hiding Bottom actions");
        
        shown = false;
        RelativeLayout group = getExplorer()
            .getUI().bottomAction;
        
        AnimationEndListener listener = new AnimationEndListener(group);
        
        if(!withAnimation) {
            listener.onAnimationEnd(null);
            return;
        }
        
        animating = true;
        
        ValueAnimator animator = ValueAnimator.ofFloat(group.getY(), initialY + group.getHeight());
        animator.setDuration(500);
        animator.addUpdateListener((vAnimator) -> {
            Log.debug.log(TAG, "Hide animation : " + vAnimator.getAnimatedValue());
            group.setY((float) vAnimator.getAnimatedValue());
        });
        animator.addListener(listener);
        animator.start();
    }

    private class AnimationEndListener extends AnimatorListenerAdapter {
       
        private RelativeLayout group;
        
        private AnimationEndListener(RelativeLayout group) {
            this.group = group;
        }
        
        @Override
        public void onAnimationEnd(Animator animator) {
            super.onAnimationEnd(animator);

            group.setY(initialY);
            group.removeAllViews();
            group.setVisibility(View.GONE);
            
            animating = false;
        }
        
    }
    
    private class OnGroupVisible implements OnGlobalLayoutListener {
      
        private RelativeLayout group;
        
        private OnGroupVisible(RelativeLayout group) {
            this.group = group;
        }
        
        @Override
        public void onGlobalLayout() {
            initialY = group.getY();
            ValueAnimator animator = ValueAnimator.ofFloat(
                group.getY() + group.getHeight(), group.getY());
            animator.setDuration(500);
            animator.addUpdateListener((vAnimator) -> {
                Log.debug.log(TAG, "Showing animation : " + vAnimator.getAnimatedValue());
                group.setY((float) vAnimator.getAnimatedValue());
            });
            animator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animator) {
                    super.onAnimationEnd(animator);
                     
                    animating = false;            
                }        
            });
            animator.start();
            group.getViewTreeObserver().removeOnGlobalLayoutListener(this);
        }
    }
}
