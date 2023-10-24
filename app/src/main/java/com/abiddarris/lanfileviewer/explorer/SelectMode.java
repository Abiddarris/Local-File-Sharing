package com.abiddarris.lanfileviewer.explorer;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ActionMode;
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

public class SelectMode extends Mode implements TopNavigationView.Callback {
    
    private TopNavigationView view;
    private boolean programaticlyEvent;
    private Set<File> checked = new HashSet<>();
    private LayoutSelectBinding actionModeLayout;
    
    public static final String TAG = Log.getTag(SelectMode.class);
    
    public SelectMode(FileExplorer explorer) {
        super(explorer);
    }
   
    @Override
    public void onModeSelected() {
        super.onModeSelected();
        
        getExplorer().getFragment()
            .showTopNavigationView(this);
        Context context = getExplorer().getContext();
        AppCompatActivity compat = (AppCompatActivity)context;
        compat.startSupportActionMode(this);
        
        getExplorer().getActionGroup()
            .show();
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
        getExplorer().getActionGroup()
            .hide();
        view.finish();
        view = null;
    }
    
    @Override
    public void onShow(TopNavigationView nav) {
        this.view = nav;
        
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
        
        nav.setCustomView(view);
    }
    
    @Override
    public void onHide(TopNavigationView view) {
        getExplorer().setMode(getExplorer().navigateMode);
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
    
}
