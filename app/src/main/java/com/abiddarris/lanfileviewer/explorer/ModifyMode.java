package com.abiddarris.lanfileviewer.explorer;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.CheckBox;
import android.widget.RelativeLayout;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ActionMode;
import androidx.appcompat.widget.Toolbar;
import com.abiddarris.lanfileviewer.R;
import com.abiddarris.lanfileviewer.R;
import com.abiddarris.lanfileviewer.actions.ActionDialog;
import com.abiddarris.lanfileviewer.actions.runnables.DownloadRunnable;
import com.abiddarris.lanfileviewer.databinding.LayoutModifyBinding;
import com.abiddarris.lanfileviewer.databinding.LayoutSelectBinding;
import com.abiddarris.lanfileviewer.explorer.FileAdapter.ViewHolder;
import com.abiddarris.lanfileviewer.file.File;
import com.abiddarris.lanfileviewer.file.FileSource;
import com.abiddarris.lanfileviewer.ui.LocalExplorerDialog;
import com.google.android.material.bottomappbar.BottomAppBar;
import com.gretta.util.log.Log;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import kotlin.jvm.internal.Lambda;

public class ModifyMode extends BottomToolbarMode implements ActionMode.Callback {
    
    private ActionMode view;
    private ActivityResultLauncher<Void> launcher;
    private boolean hide = true;
    private boolean programaticlyEvent;
    private CopyMode copyMode;
    private Menu menu;
    private MoveMode moveMode;
    private Set<File> checked = new HashSet<>();
    private LayoutSelectBinding actionModeLayout;
    
    public static final String TAG = Log.getTag(ModifyMode.class);
    
    public ModifyMode(Explorer explorer) {
        super(explorer);
        
        FileSource source = FileSource.getDefaultLocalSource(explorer.getContext());
        
        launcher = explorer.getFragment()
            .registerForActivityResult(new LocalFolderSelectorActivity.FileContract(source),
            new ActivityResultCallback<File>() {
                @Override
                public void onActivityResult(File file) {
                    Log.debug.log(TAG, file.getPath());
                    
                    File[] items = checked.toArray(new File[0]);
                    
                    new ActionDialog(getExplorer(), 
                        new DownloadRunnable(items, file))
                    .show(getExplorer().getFragment().getParentFragmentManager(), null);
                    
                    getExplorer().setMode(getExplorer().navigateMode);
                }
            });
        
        this.copyMode = new CopyMode(getExplorer());
        this.moveMode = new MoveMode(getExplorer());
    }
   
    @Override
    public void onModeSelected() {
        Context context = getExplorer().getContext();
        AppCompatActivity compat = (AppCompatActivity)context;
        compat.startSupportActionMode(this);
        
        super.onModeSelected();
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
        checked.clear();
        
        if(hide) {
            super.onModeDeselected();
        }
        hide = true;
        
        view.finish();
        view = null;
    }
    
    public void select(int pos) {
    	File file = getExplorer().getAdapter().get(pos);
        checked.add(file);
    }
    
    public File[] getSelection() {
        return checked.toArray(new File[0]);
    }
    
    private void updateActionModeView() {
        MenuItem menu = this.menu.findItem(R.id.rename);
        menu.setVisible(!(checked.size() > 1));
        
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
        if(getExplorer().getMode() == this) {
            getExplorer().setMode(getExplorer().navigateMode);
        }
    }
    
    @Override
    public void onModifyOptionsCreated(RelativeLayout group) {
        View view = LayoutInflater.from(getExplorer().getContext())
            .inflate(R.layout.layout_modify, null);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams
            (RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        group.addView(view, params);
        
        LayoutModifyBinding binding = LayoutModifyBinding.bind(view);
        binding.actions.setOnMenuItemClickListener(item -> onActionClick(item));
        
        menu = binding.actions.getMenu();
    }

    private boolean onActionClick(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.copy : 
                setCopyMode(copyMode);
                break;
            case R.id.rename :
                File target = checked.toArray(new File[0])[0];
                new RenameDialog(getExplorer(), target)
                    .show(getExplorer().getFragment().getParentFragmentManager(), null);
                break;
            case R.id.detail :
                new DetailDialog(checked.toArray(new File[0]))
                      .show(getExplorer().getFragment().getParentFragmentManager(), null);
                break;
            case R.id.download :
                launcher.launch(null);
                break;
            case R.id.delete :
                new DeleteConfirmationDialog(getExplorer(), checked.toArray(new File[0]))
                    .show(getExplorer().getFragment().getParentFragmentManager(), null);
                break;
            case R.id.move :
                setCopyMode(moveMode);
        }
        return false;
    }
    
    private void setCopyMode(CopyMode mode) {
        hide = false;
            
        Set<File> items = new HashSet<>(checked);
        mode.setItems(items);
        getExplorer().setMode(mode);
    }
}
    