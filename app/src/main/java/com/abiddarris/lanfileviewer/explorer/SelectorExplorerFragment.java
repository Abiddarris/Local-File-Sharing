package com.abiddarris.lanfileviewer.explorer;

import android.app.Activity;
import android.content.Intent;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;
import com.abiddarris.lanfileviewer.file.File;
import com.abiddarris.lanfileviewer.file.FileSource;
import com.gretta.util.Random;
import com.gretta.util.Randoms;
import androidx.activity.result.contract.ActivityResultContract;
import com.gretta.util.log.Log;
import java.util.Arrays;

public class SelectorExplorerFragment extends ExplorerFragment {
    
    public static final String ACTION_TEXT = "actionText";
    public static final String RESULT_KEY = "resultKey";
    
    public static final String TAG = Log.getTag(SelectorExplorerFragment.class);
    
    public SelectorExplorerFragment(FileSource source) {
        super(source);
    }
    
    @Override
    public ModifyMode getModifyMode(Explorer explorer) {
        return new GetFileMode(this, explorer);
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
