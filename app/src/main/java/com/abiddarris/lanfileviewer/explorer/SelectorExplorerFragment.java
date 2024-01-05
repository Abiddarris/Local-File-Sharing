package com.abiddarris.lanfileviewer.explorer;

import android.app.Activity;
import android.content.Intent;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
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

public class SelectorExplorerFragment extends ExplorerFragment {
    
    public static final String ACTION_TEXT = "actionText";
    public static final String RESULT_KEY = "resultKey";
    
    public static final String TAG = Log.getTag(SelectorExplorerFragment.class);
    
    public SelectorExplorerFragment(FileSource source) {
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
        String[] paths = new String[files.length];
        for(int i = 0; i < paths.length; ++i) {
        	paths[i] = files[i].getPath();
        }    
                
        Intent intent = new Intent();
        intent.putExtra(SelectorExplorerFragment.FileContract.RESULT, paths);
                
        getActivity()
            .setResult(Activity.RESULT_OK, intent);
        getActivity()
            .finish();    
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
        public void onBottomToolbarShown(RelativeLayout group) {
            LayoutInflater inflater = LayoutInflater.from(
                getExplorer().getContext());
          
            inflater.inflate(R.layout.layout_action_button, group, true);
        
            Button button = group.findViewById(R.id.action_button);
            button.setText(requireArguments()
                .getString(SelectorExplorerFragment.ACTION_TEXT));
        
            button.setOnClickListener(v -> {
                File[] files = getSelection();
                onSelected(files);
            });
        }
    }
    
    public static class FileContract extends ActivityResultContract<Bundle, File[]> {
        
        private FileSource source;
        private Class<? extends Activity> activity;
        
        public static final String RESULT = "result";
        
        public FileContract(FileSource source, Class<? extends Activity> activity) {
            this.source = source;
            this.activity = activity;
        }
    
        @Override
        public Intent createIntent(Context context, Bundle bundle) {
            Intent intent = new Intent(context, activity);
            intent.putExtra("extra", bundle);
            return intent;
        }
        
        @Override
        public File[] parseResult(int resultCode, Intent intent) {
            Log.debug.log(TAG, "result code : " + resultCode);
            
            if(intent == null) return null;
            
            String[] paths = intent.getStringArrayExtra(RESULT);
            if(paths == null) return null;
            
            File[] files = new File[paths.length];
            for(int i = 0; i < files.length; ++i) {
            	files[i] = source.getFile(paths[i]);
            }
            
            return files;
        }
        
    }
    
}
