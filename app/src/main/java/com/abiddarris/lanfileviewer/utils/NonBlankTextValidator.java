package com.abiddarris.lanfileviewer.utils;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;

public class NonBlankTextValidator implements TextWatcher{

    private Button target;
    
    public NonBlankTextValidator(Button target) {
        this.target = target;
    }
    
    @Override
    public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
    }
    

    @Override
    public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
    }
    

    @Override
    public void afterTextChanged(Editable editable) {
        String text = editable.toString();
        
        target.setEnabled(!text.isBlank());
    }
    
}
