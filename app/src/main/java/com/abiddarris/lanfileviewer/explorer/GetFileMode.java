package com.abiddarris.lanfileviewer.explorer;
import android.view.LayoutInflater;
import android.widget.Button;
import android.widget.RelativeLayout;
import com.abiddarris.lanfileviewer.R;

public class GetFileMode extends ModifyMode {
    
    public GetFileMode(FileExplorer explorer) {
        super(explorer);
    }
    
    @Override
    public void onModifyOptionsCreated(RelativeLayout group) {
        LayoutInflater inflater = LayoutInflater.from(
            getExplorer().getContext());
        inflater.inflate(R.layout.layout_upload_button, group, true);
    }
    
}
