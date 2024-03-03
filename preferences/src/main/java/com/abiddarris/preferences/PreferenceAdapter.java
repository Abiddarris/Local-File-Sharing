package com.abiddarris.preferences;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.Adapter;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;
import com.abiddarris.preferences.databinding.LayoutPreferenceAdapterBinding;
import java.util.ArrayList;
import java.util.List;

public class PreferenceAdapter extends Adapter<PreferenceAdapter.PreferenceHolder> {

    private Context context;
    private LayoutInflater inflater;
    private Preference[] preferences;

    PreferenceAdapter(Context context, Preference[] preferences) {
        this.context = context;
        
        List<Preference> preferencesList = new ArrayList<>();
        for(Preference preference : preferences) {
            preferencesList.add(preference);
            if(preference instanceof PreferenceCategory) {
                PreferenceCategory category = (PreferenceCategory)preference;
                for(Preference preference0 : category.getPreferences()) {
                    preferencesList.add(preference0);
                }
            }
        }
        this.preferences = preferencesList.toArray(new Preference[0]);
        
        inflater = LayoutInflater.from(context);
    }

    @Override
    public PreferenceHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        LayoutPreferenceAdapterBinding binding = LayoutPreferenceAdapterBinding.inflate(inflater);
        return new PreferenceHolder(binding.layout);
    }

    @Override
    public void onBindViewHolder(PreferenceHolder holder, int index) {
        holder.layout.removeAllViews();
        holder.layout.addView(preferences[index].getView(), new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT     
        ));
    }

    @Override
    public int getItemCount() {
        return preferences.length;
    }
    
    public static class PreferenceHolder extends ViewHolder {
        
        private LinearLayout layout;
        
        public PreferenceHolder(LinearLayout layout) {
            super(layout);
            
            this.layout = layout;
        }
        
    }
}
