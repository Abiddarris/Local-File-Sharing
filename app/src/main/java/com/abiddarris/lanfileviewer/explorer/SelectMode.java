package com.abiddarris.lanfileviewer.explorer;

import static com.abiddarris.lanfileviewer.file.Requests.*;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.RelativeLayout;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ActionMode;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.content.FileProvider;
import com.abiddarris.lanfileviewer.ApplicationCore;
import com.abiddarris.lanfileviewer.R;
import com.abiddarris.lanfileviewer.actions.ActionDialog;
import com.abiddarris.lanfileviewer.actions.runnables.DownloadManager;
import com.abiddarris.lanfileviewer.actions.runnables.DownloadRunnable;
import com.abiddarris.lanfileviewer.databinding.LayoutSelectBinding;
import com.abiddarris.lanfileviewer.databinding.LayoutSelectModeBinding;
import com.abiddarris.lanfileviewer.explorer.FileAdapter.ViewHolder;
import com.abiddarris.lanfileviewer.file.File;
import com.abiddarris.lanfileviewer.file.FilePointer;
import com.abiddarris.lanfileviewer.file.FileSource;
import com.gretta.util.log.Log;
import java.util.HashSet;
import java.util.Set;

public class SelectMode extends BottomToolbarMode implements ActionMode.Callback {
    
    private ActionMode view;
    private ActivityResultLauncher<Void> launcher;
    private boolean hide = true;
    private boolean programaticlyEvent;
    private CopyMode copyMode;
    private LayoutSelectModeBinding binding;
    private Menu menu;
    private MoveMode moveMode;
    private Set<File> checked = new HashSet<>();
    private LayoutSelectBinding actionModeLayout;
    
    public static final String TAG = Log.getTag(SelectMode.class);
    
    public SelectMode(Explorer explorer) {
        super(explorer);
        
        FileSource source = FileSource.getDefaultLocalSource(explorer.getContext());
        View toolbar = LayoutInflater.from(getExplorer().getContext())
            .inflate(R.layout.layout_select_mode, null);
        
        binding = LayoutSelectModeBinding.bind(toolbar);
        binding.copy.setOnClickListener((v) -> copy());
        binding.move.setOnClickListener((v) -> move());
        binding.download.setOnClickListener((v) -> download());
        binding.delete.setOnClickListener((v) -> delete());
        binding.others.setOnClickListener((v) -> showPopupMenu());
        
        launcher = explorer.getFragment()
            .registerForActivityResult(new LocalFolderSelectorActivity.FileContract(source),
            new DownloadCallback());
        this.copyMode = new CopyMode(getExplorer());
        this.moveMode = new MoveMode(getExplorer());
    }
   
    @Override
    public void onModeSelected() {
        Context context = getExplorer().getContext();
        AppCompatActivity compat = (AppCompatActivity)context;
        compat.startSupportActionMode(this);
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
                onFileStateChanged();    
                return;     
            } 
            checked.remove(f);
            onFileStateChanged();
        });
    }
    
    @Override
    public void onItemClickListener(ViewHolder holder, int pos) {
        CheckBox checkBox = holder.binding.checkBox;
        checkBox.setChecked(!checkBox.isChecked());
    }
    
    @Override
    public void onItemLongClickListener(ViewHolder holder, int pos) {
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
    	File file = getExplorer()
            .getAdapter()
            .get(pos);
        
        checked.add(file);
    }
    
    public File[] getSelection() {
        return checked.toArray(new File[0]);
    }
    
    private void onFileStateChanged() {
        CheckBox selectAll = actionModeLayout.selectAll;
        
        boolean check = checked.size() == getExplorer().getAdapter().getItemCount();
        if(check != selectAll.isChecked()) {
            programaticlyEvent = true;
            selectAll.setChecked(check);
        }
        
        if(checked.size() == 0) {
            hideBottomBar();
            selectAll.setText(" Select item");
        } else {
            if(!isShown()) showBottomBar();
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
                onFileStateChanged();
                adapter.notifyDataSetChanged();    
                return;    
            }
            
            for(int i = 0; i < adapter.getItemCount(); i++) {
            	checked.add(adapter.get(i));
            }
            onFileStateChanged();
            adapter.notifyDataSetChanged();
        });
        
        this.view.setCustomView(view);
        onFileStateChanged();
        
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
        } else {
            hideBottomBar(false);
        }
    }
    
    @Override
    public void onBottomToolbarShown(ViewGroup group) {
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams
            (RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        group.addView(binding.getRoot(), params);
    }
    
    @Override
    public void onParentChanged(File newParent) {
        super.onParentChanged(newParent);
        
        getExplorer()
            .setMode(getExplorer().navigateMode);
    }
    
    
    private void copy() {
        setCopyMode(copyMode);
    }
    
    private void move() {
        setCopyMode(moveMode);
    }
    
    private void download() {
        launcher.launch(null);
    }
    
    private void delete() {
        new DeleteConfirmationDialog(getExplorer(), checked.toArray(new File[0]))
                    .show(getExplorer().getFragment().getParentFragmentManager(), null);
    }
    
    private void showPopupMenu() {
        PopupMenu popup = new PopupMenu(getExplorer().getContext(), binding.others);
        popup.getMenuInflater()
            .inflate(R.menu.explorer_action, popup.getMenu());
        popup.setOnMenuItemClickListener((item) -> onPopupMenuClicked(item));
        popup.setOnDismissListener((p) -> menu = null);
        
        popup.getMenu()
            .setGroupVisible(R.id.singleSelect,!(checked.size() > 1));
      
        popup.getMenu()
            .findItem(R.id.downloadAndOpen)
            .setVisible(checked.size() == 1 && checked.iterator()
                                                .next()
                                                .isFile());
        popup.show();
    }

    private boolean onPopupMenuClicked(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.rename :
                File target = checked.toArray(new File[0])[0];
                new RenameDialog(getExplorer(), target)
                    .show(getExplorer().getFragment().getParentFragmentManager(), null);
                break;
            case R.id.detail :
                new DetailDialog(checked.toArray(new File[0]))
                      .show(getExplorer().getFragment().getParentFragmentManager(), null);
                break;
            case R.id.downloadAndOpen :
                File file = checked.toArray(new File[0])[0];
                Context context = getExplorer().getContext();
                DownloadManager.getDownloadManager(context)
                    .get(file.getFilePointer(), getExplorer(), (resultPointer) -> {
                        File result = resultPointer.get();
                        result.updateDataSync(REQUEST_ABSOLUTE_PATH, REQUEST_GET_MIME_TYPE);   
                        Uri uri = FileProvider.getUriForFile(context, 
                            context.getPackageName() + ".provider",
                            new java.io.File(result.getAbsolutePath()));
        
                        ApplicationCore.getMainHandler()
                            .post((c) -> {
                                Intent intent = new Intent(Intent.ACTION_VIEW);
                                intent.setDataAndType(uri, result.getMimeType());
                            
                                FileSource.freeFiles(result);
                            
                                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                    context.startActivity(intent);
                            });
                    });
                getExplorer()
                    .setMode(getExplorer().navigateMode);
                    
        }
        return false;
    }
    
    private void setCopyMode(CopyMode mode) {
        hide = false;
            
        mode.setItems(
            checked.toArray(new File[0]));
        getExplorer().setMode(mode);
    }
    
    private class DownloadCallback implements ActivityResultCallback<FilePointer> {
        
        @Override
        public void onActivityResult(FilePointer pointer) {
            if(pointer == null) return;
            
            File file = pointer.get();
            
            Log.debug.log(TAG, file.getPath());
                    
            File[] items = checked.toArray(new File[0]);
            
            new ActionDialog(getExplorer(), 
                new DownloadRunnable(FileSource.toPointers(items), file.getFilePointer()))
                    .show(getExplorer().getFragment().getParentFragmentManager(), null);
                    
            getExplorer().setMode(getExplorer().navigateMode);
        }
    }
        
}
    