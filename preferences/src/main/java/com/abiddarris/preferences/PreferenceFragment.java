package com.abiddarris.preferences;

import android.os.Bundle;
import android.view.View;
import androidx.annotation.CallSuper;
import androidx.annotation.MainThread;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.abiddarris.preferences.databinding.PreferenceFragmentBinding;

public abstract class PreferenceFragment extends Fragment {

    private DataProvider provider;

    public PreferenceFragment() {
        super(R.layout.preference_fragment);
    }

    @Override
    @MainThread
    public final void onViewCreated(View view, Bundle bundle) {
        super.onViewCreated(view, bundle);

        provider = new DefaultDataProvider(getContext());
        
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        PreferenceAdapter adapter = new PreferenceAdapter(getContext(), onCreatePreference());
        PreferenceFragmentBinding binding = PreferenceFragmentBinding.bind(view);

        binding.recyclerView.setLayoutManager(layoutManager);
        binding.recyclerView.setAdapter(adapter);
    }
    
    @Override
    @MainThread
    @CallSuper
    public void onDestroy() {
        super.onDestroy();
        
        new ViewModelProvider(requireActivity())
            .get(DialogCommunicator.class)
            .clear();
    }
    
    
    public DataProvider getProvider() {
        return this.provider;
    }

    public void setProvider(DataProvider provider) {
        this.provider = provider;
    }
    
    public abstract Preference[] onCreatePreference();

}
