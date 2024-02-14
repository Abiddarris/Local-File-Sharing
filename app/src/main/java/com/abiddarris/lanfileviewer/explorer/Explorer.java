package com.abiddarris.lanfileviewer.explorer;

import static com.abiddarris.lanfileviewer.file.Requests.*;

import android.content.Context;
import android.content.Intent;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.abiddarris.lanfileviewer.actions.runnables.DownloadManager;
import com.abiddarris.lanfileviewer.databinding.FragmentFileExplorerBinding;
import com.abiddarris.lanfileviewer.file.File;
import com.abiddarris.lanfileviewer.file.FilePointer;
import com.abiddarris.lanfileviewer.file.FileSource;
import com.abiddarris.lanfileviewer.sorter.FileSorter;
import com.abiddarris.lanfileviewer.ui.ExceptionDialog;
import com.gretta.util.log.Log;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Explorer implements DefaultLifecycleObserver {

    public static final String TAG = Log.getTag(Explorer.class);
    
    Mode navigateMode;
    SelectMode selectMode;

    private boolean loading;
    private boolean error;
    private DownloadManager downloadManager;
    private ExplorerFragment fragment;
    private FileSorter sorter = FileSorter.createSorter(FileSorter.NAME | FileSorter.ASCENDING);
    private FragmentFileExplorerBinding ui;
    private File parent;
    private FileAdapter adapter;
    private int targetCount;
    private Mode mode;
    private List<File> cache = new ArrayList<>();
    private List<OnExplorerUpdatedListener> updatedListeners = new ArrayList<>();
    private OnExplorerUpdatedListener temporaryListener;
    private SwipeRefreshLayout refresher;

    public Explorer(
            ExplorerFragment fragment,
            FragmentFileExplorerBinding ui,
            SwipeRefreshLayout refresher) {
        this.fragment = fragment;
        this.ui = ui;
        this.refresher = refresher;

        navigateMode = fragment.getMainMode(this);
        selectMode = fragment.getSelectMode(this);
        adapter = new FileAdapter(this);
        
        setMode(navigateMode);
    }

    public void open(FilePointer pointer) {
        if(loading) {
            Log.debug.log(TAG, "cannot open " + pointer + " reason : already loading something");
            return;
        }
        File file = pointer.get();
        file.updateData(e -> {
            if(e != null){
                showErrorDialog(e);
                cancel();      
                return;
            }    
            if (file.isDirectory()) {
                setParent(file);
            
                loading = true;
                refresher.setRefreshing(true);
                update();
                return;
            }

            String mimeType = file.getMimeType();

            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(file.toUri(), mimeType == null ? "*" : mimeType);
            getContext().startActivity(intent);
                
            FileSource.freeFiles(file);  
        }, REQUEST_IS_DIRECTORY, REQUEST_GET_MIME_TYPE);
    }
    
    public void refresh() {
        refresh(null);
    }
    
    public void refresh(OnExplorerUpdatedListener listener) {
        if(loading) {
            Log.debug.log(TAG, "cannot refresh " + parent + " reason : already refreshing");
            return;
        }
        loading = true;
        temporaryListener = listener;
        update();
    }

    private void update() {
        parent.updateData((e) -> {
            if(e != null){
                showErrorDialog(e);  
                cancel();
                return;
            }    
            load(parent.listFiles());
        }, REQUEST_LIST);
    }
    
    private synchronized void showErrorDialog(Exception e) {
        if(error) {
            return;
        }
        error = true;
        Log.debug.log(TAG, e.toString());
        new ExceptionDialog(e)
                      .show(fragment.getParentFragmentManager(), null);
    }

    private void load(File[] files) {
        targetCount = files.length;
        if (targetCount == 0) {
            onLoaded();
            return;
        }
        for (File file : files) {
            file.updateData((e) -> {
                if(e != null){
                    showErrorDialog(e);
                    cancel();      
                    return;
                }    
                onFileLoaded(file);
            });
        }
    }
    
    
    private synchronized void onFileLoaded(File file) {
        cache.add(file);
        if (cache.size() == targetCount) {
            onLoaded();
        }
    }

    private void onLoaded() {
        File[] files = cache.toArray(new File[0]);
        cache.clear();
        
        Arrays.sort(files, sorter);
        adapter.setFiles(files);

        adapter.getMainThread().post((c) -> refresher.setRefreshing(false));
        loading = false;
        
        if(temporaryListener != null) 
            temporaryListener.onUpdated(this);
        
        temporaryListener = null;
        
        for(OnExplorerUpdatedListener listener : updatedListeners) {
            listener.onUpdated(this);
        }
    }
    
    private void cancel() {
        FileSource.freeFiles(cache);
        cache.clear();
        
        onLoaded();
    }
    
    private void setParent(File parent) {
        if(this.parent != null) {
            this.parent.getSource()
                .free(this.parent);
        }
        this.parent = parent;
        
        getMode().onParentChanged(this.parent);
    }
    
    public boolean navigateUp() {
        if (refresher.isRefreshing()) return true;
        if (parent == null) return false;

        File grandParent = parent.getParentFile();
        if (grandParent == null) return false;

        FilePointer pointer = grandParent.getFilePointer();
        FileSource.freeFiles(grandParent);
        
        open(pointer);
        
        return true;
    }

    public FilePointer getParent() {
        return parent.getFilePointer();
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
    
    @Override
    public void onDestroy(LifecycleOwner owner) {
        getAdapter()
            .setFiles(new File[0]);
        FileSource.freeFiles(parent);
    }
    
    public static interface OnExplorerUpdatedListener {
        
        void onUpdated(Explorer e);
        
    }

    public DownloadManager getDownloadManager() {
        return this.downloadManager;
    }
    
    public void setDownloadManager(DownloadManager downloadManager) {
        this.downloadManager = downloadManager;
    }
}
