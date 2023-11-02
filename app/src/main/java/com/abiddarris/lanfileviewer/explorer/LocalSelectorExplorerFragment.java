package com.abiddarris.lanfileviewer.explorer;

import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;
import com.gretta.util.Random;
import com.gretta.util.Randoms;

public class LocalSelectorExplorerFragment extends LocalExplorerFragment {
    
    public static final String ACTION_TEXT = "actionText";
    public static final String RESULT_KEY = "resultKey";
    
    @Override
    public ModifyMode getModifyMode(Explorer explorer) {
        return new GetFileMode(this, explorer);
    }
    
}
