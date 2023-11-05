package com.abiddarris.lanfileviewer.explorer;
import android.app.Activity;
import android.content.Intent;
import android.view.LayoutInflater;
import android.widget.Button;
import android.widget.RelativeLayout;
import androidx.fragment.app.Fragment;
import com.abiddarris.lanfileviewer.R;
import com.abiddarris.lanfileviewer.file.File;

public class GetFileMode extends ModifyMode {
    
    private Fragment fragment;
    
    public GetFileMode(Fragment fragment, Explorer explorer) {
        super(explorer);
        
        this.fragment = fragment;
    }
    
    @Override
    public void onModifyOptionsCreated(RelativeLayout group) {
        LayoutInflater inflater = LayoutInflater.from(
            getExplorer().getContext());
        inflater.inflate(R.layout.layout_action_button, group, true);
        
        Button button = group.findViewById(R.id.action_button);
        button.setText(fragment.requireArguments()
            .getString(SelectorExplorerFragment.ACTION_TEXT));
        
        button.setOnClickListener(v -> {
            File[] files = getSelection();
            String[] paths = new String[files.length];
            for(int i = 0; i < paths.length; ++i) {
            	paths[i] = files[i].getPath();
            }    
                
            Intent intent = new Intent();
            intent.putExtra(SelectorExplorerFragment.FileContract.RESULT, paths);
                
            fragment.getActivity()
                .setResult(Activity.RESULT_OK, intent);
            fragment.getActivity()
                .finish();    
        });
    }
    
}
