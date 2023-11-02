package com.abiddarris.lanfileviewer.explorer;
import android.view.LayoutInflater;
import android.widget.Button;
import android.widget.RelativeLayout;
import androidx.fragment.app.Fragment;
import com.abiddarris.lanfileviewer.R;

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
            .getString(LocalSelectorExplorerFragment.ACTION_TEXT));
        
        button.setOnClickListener(v -> {
            fragment.getParentFragmentManager()
                .popBackStack();
        });
    }
    
}
