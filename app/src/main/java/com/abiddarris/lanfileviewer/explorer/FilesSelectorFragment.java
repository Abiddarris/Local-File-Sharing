package com.abiddarris.lanfileviewer.explorer;

import android.app.Activity;
import android.content.Intent;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.view.LayoutInflater;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;
import com.abiddarris.lanfileviewer.R;
import com.abiddarris.lanfileviewer.file.File;
import com.abiddarris.lanfileviewer.file.FileSource;
import com.gretta.util.Random;
import com.gretta.util.Randoms;
import androidx.activity.result.contract.ActivityResultContract;
import com.gretta.util.log.Log;
import java.util.Arrays;
import com.abiddarris.lanfileviewer.explorer.FileAdapter.ViewHolder;

public class FilesSelectorFragment extends SelectorFragment {
    
    public static final String ACTION_TEXT = "actionText";
    public static final String RESULT_KEY = "resultKey";
    
    public static final String TAG = Log.getTag(FilesSelectorFragment.class);
    
    public FilesSelectorFragment(FileSource source) {
        super(source);
    }
    
    @Override
    protected Mode getMainMode(Explorer explorer) {
        return new MainMode(explorer);
    }
    
    @Override
    public SelectMode getSelectMode(Explorer explorer) {
        return new GetFileMode(explorer);
    }
    
    private void onSelected(File... files) {
        getOnSelectedListener()
            .onSelected(FileSource.toPointers(files));
    }
    
    private class MainMode extends NavigateMode {
        
        public MainMode(Explorer explorer) {
            super(explorer);
        }
        
        @Override
        public void onItemClickListener(ViewHolder holder, int pos) {
            File file = getExplorer()
                .getAdapter()
                .get(pos);
            if(file.isDirectory()) {
                super.onItemClickListener(holder, pos);
                return;
            }
            onSelected(file);
        }    
    }
    
    private class GetFileMode extends SelectMode {
    
        public GetFileMode(Explorer explorer) {
            super(explorer);
        }
    
        @Override
        public void onBottomToolbarShown(ViewGroup group) {
            LayoutInflater inflater = LayoutInflater.from(
                getExplorer().getContext());
          
            inflater.inflate(R.layout.layout_action_button, group, true);
        
            Button button = group.findViewById(R.id.action_button);
            button.setText(requireArguments()
                .getString(ACTION_TEXT));
        
            button.setOnClickListener(v -> {
                File[] files = getSelection();
                onSelected(files);
            });
        }
    }
    
    
    
}
