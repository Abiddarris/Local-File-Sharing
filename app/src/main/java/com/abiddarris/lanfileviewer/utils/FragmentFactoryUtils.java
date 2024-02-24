package com.abiddarris.lanfileviewer.utils;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentFactory;

public class FragmentFactoryUtils {
    
    public static FragmentFactory createFactory(IFragmentFactory factory) {
        return new FragmentFactoryImpl(factory);
    }
    
    public static interface IFragmentFactory {
        Fragment create(Class<? extends Fragment> fragmentClass);
    }
    
    private static class FragmentFactoryImpl extends FragmentFactory {
        
        private IFragmentFactory factory;
        
        private FragmentFactoryImpl(IFragmentFactory factory) {
            this.factory = factory;
        }
        
        @Override
        @NonNull
        public Fragment instantiate(ClassLoader loader, String name) {
            Class<? extends Fragment> fragmentClass = loadFragmentClass(loader,name);
            Fragment fragment = factory.create(fragmentClass);
            
            return fragment != null ? fragment : super.instantiate(loader, name);
        }
    }
    
}
