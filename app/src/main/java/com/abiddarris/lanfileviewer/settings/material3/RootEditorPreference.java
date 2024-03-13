package com.abiddarris.lanfileviewer.settings.material3;

import com.abiddarris.lanfileviewer.R;
import static com.abiddarris.lanfileviewer.file.Requests.*;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.abiddarris.lanfileviewer.databinding.DialogRootEditorBinding;
import com.abiddarris.lanfileviewer.explorer.FilesSelectorFragment;
import com.abiddarris.lanfileviewer.explorer.LocalFilesSelectorActivity;
import com.abiddarris.lanfileviewer.file.File;
import com.abiddarris.lanfileviewer.file.FilePointer;
import com.abiddarris.lanfileviewer.file.FileSource;
import com.abiddarris.lanfileviewer.settings.RootAdapter;
import com.abiddarris.lanfileviewer.settings.Settings;
import com.abiddarris.preferences.DialogPreference;
import com.abiddarris.preferences.PreferenceFragment;

public class RootEditorPreference extends DialogPreference {
    
    private ActivityResultLauncher<Bundle> getRootsLauncher;
    private AlertDialog dialog;
    private DialogRootEditorBinding binding;
    private RootAdapter adapter;
    
    public RootEditorPreference(PreferenceFragment fragment, String key) {
        super(fragment, key);
        
        getRootsLauncher = fragment.registerForActivityResult(
            new LocalFilesSelectorActivity.FileContract(getContext()), new ResultCallback());
        
    }
    
    @Override
    protected View onCreateView(LayoutInflater inflater) {
        binding = DialogRootEditorBinding.inflate(inflater);
 
        return binding.getRoot();
    }
    
    @Override
    protected void onViewCreated(AlertDialog dialog, View view) {
        adapter = new RootAdapter(getContext());
        
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        
        binding.rootList.setAdapter(adapter);
        binding.rootList.setLayoutManager(layoutManager);
        binding.rootList.addItemDecoration(
            new DividerItemDecoration(getContext(), layoutManager.getOrientation()));
        
        adapter.setRoots(Settings.getRoots(getContext()));
        
        binding.add.setOnClickListener(v -> {
            Bundle bundle1 = new Bundle();
            bundle1.putString(FilesSelectorFragment.ACTION_TEXT, getContext()
                     .getString(R.string.select));
            
            getRootsLauncher.launch(bundle1);
        });
        binding.resetToDefault.setOnClickListener(v -> {
            adapter.setRoots(Settings.getDefaultRoots(getContext()));
            adapter.notifyDataSetChanged();    
        });
        
        
        adapter.setOnRootRemoved((adapter) -> {
            if(adapter.getRoots().size() == 0) {
               dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                .setEnabled(false);
            }   
        });
    }
    
    @Override
    protected void onSave() {
        super.onSave();
   
        Settings.setRoots(getContext(), adapter.getRoots());
    }
    
    private class ResultCallback implements ActivityResultCallback<FilePointer[]> {
        
        @Override
        public void onActivityResult(FilePointer[] files) {
            if(files == null) return;
            
            for(FilePointer pointer : files){
                File file = pointer.get();
                
                file.updateDataSync(REQUEST_ABSOLUTE_PATH);
                adapter.addRoot(new java.io.File(file.getAbsolutePath()));
                
                FileSource.freeFiles(file);
            }
            adapter.notifyDataSetChanged();
            
            if(adapter.getRoots().size() > 0) {
               dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                .setEnabled(true);
            }
        }
        
    }
}
