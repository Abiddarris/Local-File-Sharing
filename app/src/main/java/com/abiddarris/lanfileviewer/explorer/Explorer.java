package com.abiddarris.lanfileviewer.explorer;

import android.content.Context;
import android.content.Intent;
import android.net.Network;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.abiddarris.lanfileviewer.databinding.FragmentFileExplorerBinding;
import com.abiddarris.lanfileviewer.file.File;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import java.util.ArrayList;
import java.util.List;

public class Explorer {

    final NavigateMode navigateMode = new NavigateMode(this);
    ModifyMode selectMode;
    
    private ExplorerFragment fragment;
    private FragmentFileExplorerBinding ui;
    private File parent;
    private FileAdapter adapter;
    private int targetCount;
    private List<File> cache = new ArrayList<>();
    private Mode mode = navigateMode;
    private SwipeRefreshLayout refresher;

    public Explorer(ExplorerFragment fragment, FragmentFileExplorerBinding ui, SwipeRefreshLayout refresher) {
        this.fragment = fragment;
        this.ui = ui;
        this.refresher = refresher;

        selectMode = fragment.getModifyMode(this);
        adapter = new FileAdapter(this);
    }

    public void open(File file) {
        if (file.isDirectory()) {
            this.parent = file;
            refresher.setRefreshing(true);
            update();
            return;
        }

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(file.toUri(), file.getMimeType());
        getContext().startActivity(intent);
    }

    public void update() {
        load(parent.listFiles());
    }

    private void load(File[] files) {
        cache.clear();
        targetCount = files.length;
        if (targetCount == 0) {
            onLoaded();
            return;
        }
        for (File file : files) {
            file.updateData(() -> onFileLoaded(file));
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
        adapter.setFiles(files);

        adapter.getMainThread().post(() -> refresher.setRefreshing(false));
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
        if (this.mode != null) {
            this.mode.onModeDeselected();
        }
        this.mode = mode;
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
       
}
