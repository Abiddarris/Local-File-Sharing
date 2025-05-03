package com.abiddarris.lanfileviewer;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import com.abiddarris.lanfileviewer.settings.SettingsFragment;
import com.abiddarris.lanfileviewer.ui.HomeFragment;
import com.abiddarris.lanfileviewer.ui.ScanFragment;

public class MainFragmentAdapter extends FragmentStateAdapter {

    public MainFragmentAdapter(FragmentActivity activity) {
        super(activity);
    }
    
    @Override
    public int getItemCount() {
        return 3;
    }

    @Override
    public Fragment createFragment(int pos) {
        switch(pos) {
            case 0 :
                return new HomeFragment();
            case 1 :
                return new ScanFragment();
            case 2 :
                return new SettingsFragment();
        }
        return null;
    }
}
