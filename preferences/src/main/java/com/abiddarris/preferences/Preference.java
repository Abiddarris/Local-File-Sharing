package com.abiddarris.preferences;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import com.abiddarris.preferences.databinding.LayoutPreferenceBinding;

public class Preference {

    private DataStore dataStore;
    private PreferenceFragment fragment;
    private String key;
    private String title;
    private String summary = "";
    private SummaryProvider summaryProvider;
    private View view;

    public Preference(PreferenceFragment fragment, String key) {
        this.fragment = fragment;
        this.key = key;
    }

    public String getTitle() {
        return this.title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setTitle(int resId) {
        setTitle(getFragment().getString(resId));
    }

    public String getSummary() {
        return this.summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public PreferenceFragment getFragment() {
        return this.fragment;
    }

    public String getKey() {
        return this.key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public SummaryProvider getSummaryProvider() {
        return this.summaryProvider;
    }

    public void setSummaryProvider(SummaryProvider summaryProvider) {
        this.summaryProvider = summaryProvider;
    }

    protected View createView() {
        return LayoutPreferenceBinding.inflate(LayoutInflater.from(getFragment().getContext()))
                .getRoot();
    }

    protected void fillView(View view) {
        if (getSummaryProvider() != null) setSummary(getSummaryProvider().getSummary(this));

        LayoutPreferenceBinding binding = LayoutPreferenceBinding.bind(view);
        binding.title.setText(getTitle());
        binding.summary.setText(getSummary());
    }
    
    public DataStore getDataStore() {
        return this.dataStore;
    }

    public void setDataStore(DataStore dataStore) {
        this.dataStore = dataStore;
    }
    
    public DataStore getNonNullDataStore() {
        DataStore dataStore = getDataStore();
        if(dataStore != null) {
            return dataStore;
        }
        dataStore = getFragment()
            .getDefaultDataStore();
        if(dataStore != null) {
            return dataStore;
        }
        throw new NullPointerException("Fragment DataStore is null!");
    }
    
    protected void storeString(String value) {
        getNonNullDataStore()
            .store(getKey(), value);
    }

    protected void onClick() {}

    View getView() {
        if (view == null) {
            view = createView();
        }
        view.setClickable(true);
        view.setOnClickListener(v -> onClick());
        fillView(view);
        return view;
    }

    
}
