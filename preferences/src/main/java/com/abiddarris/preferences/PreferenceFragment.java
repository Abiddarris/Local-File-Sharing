package com.abiddarris.preferences;

import android.os.Bundle;
import android.view.View;
import androidx.annotation.MainThread;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.abiddarris.preferences.databinding.PreferenceFragmentBinding;

public abstract class PreferenceFragment extends Fragment {
    
    public PreferenceFragment() {
        super(R.layout.preference_fragment);
    }
    
    @Override
    @MainThread
    public final void onViewCreated(View view, Bundle bundle) {
        super.onViewCreated(view, bundle);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        PreferenceAdapter adapter = new PreferenceAdapter(getContext(), onCreatePreference());
        PreferenceFragmentBinding binding = PreferenceFragmentBinding.bind(view);
        
        binding.recyclerView.setLayoutManager(layoutManager);
        binding.recyclerView.setAdapter(adapter);
    }
    
    public abstract Preference[] onCreatePreference();
    
}
