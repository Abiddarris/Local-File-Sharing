package com.abiddarris.lanfileviewer.explorer;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.CheckBox;
import android.widget.RelativeLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ActionMode;
import com.abiddarris.lanfileviewer.R;
import com.abiddarris.lanfileviewer.R;
import com.abiddarris.lanfileviewer.databinding.LayoutSelectBinding;
import com.abiddarris.lanfileviewer.explorer.FileAdapter.ViewHolder;
import com.abiddarris.lanfileviewer.file.File;
import com.gretta.util.log.Log;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import kotlin.jvm.internal.Lambda;

public class ModifyMode extends Mode implements ActionMode.Callback {
    
    private ActionMode view;
    private boolean programaticlyEvent;
    private Set<File> checked = new HashSet<>();
    private LayoutSelectBinding actionModeLayout;
    
    public static final String TAG = Log.getTag(ModifyMode.class);
    
    public ModifyMode(Explorer explorer) {
        super(explorer);
    }
   
    @Override
    public void onModeSelected() {
        super.onModeSelected();
        
        Context context = getExplorer().getContext();
        AppCompatActivity compat = (AppCompatActivity)context;
        compat.startSupportActionMode(this);
        
        showModifyOptions();
    }
    
    @Override
    public void onViewBind(ViewHolder holder, int pos) {
        FileAdapter adapter = getExplorer().getAdapter();
        boolean check = checked.contains(adapter.get(pos));
        
        CheckBox checkBox = holder.binding.checkBox;
        checkBox.setVisibility(View.VISIBLE);
        checkBox.setOnCheckedChangeListener(null);
        checkBox.setChecked(check);
        checkBox.setOnCheckedChangeListener((v,b) -> {
            Log.debug.log(TAG, "checked calles");
            File f = adapter.get(pos);
            if(b) {
                checked.add(f);   
                updateActionModeView();    
                return;     
            } 
            checked.remove(f);
            updateActionModeView();
        });
    }
    
    @Override
    public void onItemClickListener(ViewHolder holder, int pos) {
        CheckBox checkBox = holder.binding.checkBox;
        checkBox.setChecked(!checkBox.isChecked());
    }
    
    @Override
    public void onItemLongClickListener(ViewHolder holder, int pos) {
        // TODO: Implement this method
    }
    
    @Override
    public boolean onBackPressed() {
        getExplorer().setMode(getExplorer().navigateMode);
        return true;
    }
    
    @Override
    public void onModeDeselected() {
        super.onModeDeselected();
        
        checked.clear();
        
        hideModifyOptions();
        
        view.finish();
        view = null;
    }
    
    public void select(int pos) {
    	File file = getExplorer().getAdapter().get(pos);
        checked.add(file);
    }
    
    private void updateActionModeView() {
        CheckBox selectAll = actionModeLayout.selectAll;
        
        boolean check = checked.size() == getExplorer().getAdapter().getItemCount();
        if(check != selectAll.isChecked()) {
            programaticlyEvent = true;
            selectAll.setChecked(check);
        }
        
        if(checked.size() == 0) {
            selectAll.setText(" Select item");
        } else {
            selectAll.setText(checked.size() + " selected");
        }
    }
    

    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        this.view = mode;
        
        LayoutInflater inflater = LayoutInflater.from(getExplorer().getContext());
        View view = inflater.inflate(R.layout.layout_select, null);
        actionModeLayout = LayoutSelectBinding.bind(view);
        
        actionModeLayout.selectAll.setOnCheckedChangeListener((v,b) -> {
            if(programaticlyEvent) {
                programaticlyEvent = false;
                return;
            } 
            FileAdapter adapter = getExplorer().getAdapter();    
            if(!b) {
                checked.clear();
                updateActionModeView();
                adapter.notifyDataSetChanged();    
                return;    
            }
            
            for(int i = 0; i < adapter.getItemCount(); i++) {
            	checked.add(adapter.get(i));
            }
            updateActionModeView();
            adapter.notifyDataSetChanged();
        });
        
        this.view.setCustomView(view);
        
        return true;
    }
    

    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        return false;
    }
    

    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem menu) {
        return false;
    }
    

    @Override
    public void onDestroyActionMode(ActionMode mode) {
        getExplorer().setMode(getExplorer().navigateMode);
    }
    
    private void showModifyOptions() {
        RelativeLayout group = getExplorer().getUI()
            .bottomAction;
        
    	onModifyOptionsCreated(group);
        
        group.setVisibility(View.VISIBLE);
        group.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                ValueAnimator animator = ValueAnimator.ofFloat(group.getY() + group.getHeight(), group.getY());
                animator.setDuration(500);
                animator.addUpdateListener((vAnimator) -> {
                    Log.debug.log("anim", vAnimator.getAnimatedValue());    
                    group.setY((float)vAnimator.getAnimatedValue());
                });
                animator.start();
                group.getViewTreeObserver().removeOnGlobalLayoutListener(this); 
            }
        });
    }
    
    private void hideModifyOptions() {
        RelativeLayout group = getExplorer().getUI()
            .bottomAction;
        
        float initialY = group.getY();
        
        ValueAnimator animator = ValueAnimator.ofFloat(initialY, initialY + group.getHeight());
        animator.setDuration(500);
        animator.addUpdateListener((vAnimator) -> {
            group.setY((float)vAnimator.getAnimatedValue());
        });
        animator.addListener(new AnimatorListenerAdapter() {
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
    
    public void onModifyOptionsCreated(RelativeLayout group) {
        LayoutInflater.from(getExplorer().getContext())
            .inflate(R.layout.layout_modify, group, true);
    }
    
}
    