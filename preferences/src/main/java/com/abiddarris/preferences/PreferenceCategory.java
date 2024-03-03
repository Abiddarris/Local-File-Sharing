package com.abiddarris.preferences;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import com.abiddarris.preferences.databinding.LayoutCategoryBinding;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

public class PreferenceCategory extends Preference {

    private Context context;
    private List<Preference> preferences = new ArrayList<>();
    private String category;

    public PreferenceCategory(Context context, String key) {
        super(context, key);
    }
    
    public void addPreference(Preference preference) {
        if(preference instanceof PreferenceCategory) 
            throw new IllegalArgumentException("preference cannot be category!");
        preferences.add(preference);
    }
    
    public Preference[] getPreferences() {
        return preferences.toArray(new Preference[0]);
    }
    
    @Override
    protected View createView() {
        return LayoutCategoryBinding.inflate(LayoutInflater
            .from(getContext()))
            .getRoot();
    }
    
    @Override
    protected void fillView(View view) {
        LayoutCategoryBinding binding = LayoutCategoryBinding.bind(view);
        binding.title.setText(getTitle());
    }
    
}
