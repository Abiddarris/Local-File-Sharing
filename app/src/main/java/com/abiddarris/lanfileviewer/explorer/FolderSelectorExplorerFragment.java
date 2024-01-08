package com.abiddarris.lanfileviewer.explorer;

import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import com.abiddarris.lanfileviewer.R;
import com.abiddarris.lanfileviewer.file.File;
import com.abiddarris.lanfileviewer.file.FileSource;
import com.abiddarris.lanfileviewer.explorer.FileAdapter.ViewHolder;

public class FolderSelectorExplorerFragment extends ExplorerFragment {

    private OnFolderSelectedListener onFolderSelectedListener;

    public FolderSelectorExplorerFragment(FileSource source) {
        super(source);
    }

    @Override
    protected Mode getMainMode(Explorer explorer) {
        return new SelectMode(explorer);
    }

    private class SelectMode extends NavigateMode {

        public SelectMode(Explorer explorer) {
            super(explorer);
        }

        @Override
        public void onModeSelected() {
            ViewGroup group = getExplorer().getUI().bottomAction;
            group.setVisibility(View.VISIBLE);
            getLayoutInflater().inflate(R.layout.layout_action_button, group, true);

            Button button = group.findViewById(R.id.action_button);
            button.setText(getString(R.string.select));
            button.setOnClickListener((v) -> {
                onFolderSelectedListener.onFolderSelected(getExplorer().getParent());
            });
        }

        @Override
        public void onItemClickListener(ViewHolder holder, int pos) {
            File file = getExplorer().getAdapter().get(pos);

            if (file.isFile()) return;

            super.onItemClickListener(holder, pos);
        }

        @Override
        public void onItemLongClickListener(ViewHolder holder, int pos) {}
    }

    public static interface OnFolderSelectedListener {
        void onFolderSelected(File folder);
    }

    public OnFolderSelectedListener getOnFolderSelectedListener() {
        return this.onFolderSelectedListener;
    }

    public void setOnFolderSelectedListener(OnFolderSelectedListener onFolderSelectedListener) {
        this.onFolderSelectedListener = onFolderSelectedListener;
    }
}
