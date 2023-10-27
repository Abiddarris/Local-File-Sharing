package com.abiddarris.lanfileviewer.explorer;

import androidx.fragment.app.FragmentTransaction;

public class LocalSelectorExplorerFragment extends LocalExplorerFragment {
    
    @Override
    public ModifyMode getModifyMode(FileExplorer explorer) {
        return new GetFileMode(explorer);
    }
    
    public static FragmentTransaction replace(FragmentTransaction transaction, int id, String requestCode) {
        
        return transaction.setReorderingAllowed(true)
            .replace(id, LocalSelectorExplorerFragment.class, null);
    }
    
}
