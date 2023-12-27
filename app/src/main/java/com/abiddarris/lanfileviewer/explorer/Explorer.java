package com.abiddarris.lanfileviewer.explorer;

import android.content.Context;
import android.content.Intent;
import android.net.Network;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.abiddarris.lanfileviewer.databinding.FragmentFileExplorerBinding;
import com.abiddarris.lanfileviewer.file.File;
import com.abiddarris.lanfileviewer.sorter.FileSorter;
import com.abiddarris.lanfileviewer.ui.ExceptionDialog;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Explorer {

    Mode navigateMode;
    ModifyMode selectMode;

    private ExplorerFragment fragment;
    private FileSorter sorter = FileSorter.createSorter(FileSorter.NAME | FileSorter.ASCENDING);
    private FragmentFileExplorerBinding ui;
    private File parent;
    private FileAdapter adapter;
    private int targetCount;
    private Mode mode;
    private List<File> cache = new ArrayList<>();
    private List<OnExplorerUpdatedListener> updatedListeners = new ArrayList<>();
    private SwipeRefreshLayout refresher;

    public Explorer(
            ExplorerFragment fragment,
            FragmentFileExplorerBinding ui,
            SwipeRefreshLayout refresher) {
        this.fragment = fragment;
        this.ui = ui;
        this.refresher = refresher;

        navigateMode = fragment.getMainMode(this);
        selectMode = fragment.getModifyMode(this);
        adapter = new FileAdapter(this);
        
        setMode(navigateMode);
    }

    public void open(File file) {
        if (file.isDirectory()) {
            this.parent = file;
            refresher.setRefreshing(true);
            update();
            return;
        }

        String mimeType = file.getMimeType();

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(file.toUri(), mimeType == null ? "*" : mimeType);
        getContext().startActivity(intent);
    }

    public void update() {
        parent.updateData((e) -> {
            if(e != null){
                new ExceptionDialog(e)
                      .show(fragment.getChildFragmentManager(), null);
                return;
            }    
            load(parent.listFiles());
        });
    }

    private void load(File[] files) {
        cache.clear();
        targetCount = files.length;
        if (targetCount == 0) {
            onLoaded();
            return;
        }
        for (File file : files) {
            file.updateData((e) -> {
                if(e != null){
                    new ExceptionDialog(e)
                          .show(fragment.getChildFragmentManager(), null);
                    return;
                }
                onFileLoaded(file);
            });
        }
    }

    public boolean navigateUp() {
        if (refresher.isRefreshing()) return true;
        if (parent == null) return false;

        File grandParent = parent.getParentFile();
        if (grandParent == null) return false;

        open(grandParent);
        return true;
    }

    private synchronized void onFileLoaded(File file) {
        cache.add(file);
        if (cache.size() == targetCount) {
            onLoaded();
        }
    }

    private void onLoaded() {
        File[] files = cache.toArray(new File[0]);
        Arrays.sort(files, sorter);
        adapter.setFiles(files);

        adapter.getMainThread().post(() -> refresher.setRefreshing(false));
        
        for(OnExplorerUpdatedListener listener : updatedListeners) {
            listener.onUpdated(this);
        }
    }

    public File getParent() {
        return parent;
    }

    public boolean onBackPressed() {
        return mode.onBackPressed();
    }

    public FileAdapter getAdapter() {
        return this.adapter;
    }

    public Mode getMode() {
        return this.mode;
    }

    public void setMode(Mode mode) {
        Mode oldMode = this.mode;
        this.mode = mode;
        
        if (oldMode != null) {
            oldMode.onModeDeselected();
        }
        
        mode.onModeSelected();

        adapter.notifyDataSetChanged();
    }

    public FragmentFileExplorerBinding getUI() {
        return ui;
    }

    public Context getContext() {
        return fragment.getContext();
    }

    public ExplorerFragment getFragment() {
        return this.fragment;
    }

    public FileSorter getSorter() {
        return this.sorter;
    }

    public void setSorter(FileSorter sorter) {
        this.sorter = sorter;
    }
    
    public void addOnUpdatedListener(OnExplorerUpdatedListener listener) {
    	updatedListeners.add(listener);
    }
    
    public static interface OnExplorerUpdatedListener {
        
        void onUpdated(Explorer e);
        
    }
}
