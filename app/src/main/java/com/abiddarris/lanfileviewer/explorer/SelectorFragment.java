package com.abiddarris.lanfileviewer.explorer;
import android.os.Bundle;
import androidx.annotation.CallSuper;
import androidx.annotation.MainThread;
import com.abiddarris.lanfileviewer.file.File;
import com.abiddarris.lanfileviewer.file.FilePointer;
import com.abiddarris.lanfileviewer.file.FileSource;

public class SelectorFragment extends ExplorerFragment{
    
    private OnSelectedListener onSelectedListener;

    public SelectorFragment(FileSource source) {
        super(source);
    }
    
    public static interface OnSelectedListener {
        void onSelected(FilePointer... pointers);
    }

    public OnSelectedListener getOnSelectedListener() {
        return this.onSelectedListener;
    }
    
    public void setOnSelectedListener(OnSelectedListener onSelectedListener) {
        this.onSelectedListener = onSelectedListener;
    }
}