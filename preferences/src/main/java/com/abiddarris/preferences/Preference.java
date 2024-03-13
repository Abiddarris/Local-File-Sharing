package com.abiddarris.preferences;

import android.content.Context;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.core.content.ContextCompat;
import com.abiddarris.preferences.databinding.LayoutPreferenceBinding;

public class Preference {

    private DataStore dataStore;
    private OnSavePreference onSavePreference;
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
    
    public Context getContext() {
        return getFragment()
            .getContext();
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

    public DataStore getDataStore() {
        return this.dataStore;
    }

    public void setDataStore(DataStore dataStore) {
        this.dataStore = dataStore;
    }

    public DataStore getNonNullDataStore() {
        DataStore dataStore = getDataStore();
        if (dataStore != null) {
            return dataStore;
        }
        dataStore = getFragment().getDefaultDataStore();
        if (dataStore != null) {
            return dataStore;
        }
        throw new NullPointerException("Fragment DataStore is null!");
    }

    protected View createView() {
        return LayoutPreferenceBinding.inflate(LayoutInflater.from(getFragment().getContext()))
                .getRoot();
    }

    protected void fillView(View view) {
        if (getSummaryProvider() != null) setSummary(getSummaryProvider().getSummary(this));

        TextView title = view.findViewById(R.id.title);
        TextView summary = view.findViewById(R.id.summary);

        title.setText(getTitle());
        summary.setText(getSummary());
    }

    protected void storeString(String value) {
        if(getOnSavePreference() != null &&
            !getOnSavePreference().save(this, value)) return;
        
        getNonNullDataStore().store(getKey(), value);
    }

    protected void storeBoolean(boolean value) {
        if(getOnSavePreference() != null &&
            !getOnSavePreference().save(this, value)) return;
        
        getNonNullDataStore().store(getKey(), value);
    }

    protected void refillView() {
        if (view != null) fillView(view);
    }

    @Deprecated
    protected void onClick() {}

    protected void onClick(View view) {
        onClick();
    }

    View getView() {
        view = createView();
        view.setClickable(true);
        view.setOnClickListener(v -> onClick(view));

        TypedValue value = new TypedValue();
        view.getContext()
                .getTheme()
                .resolveAttribute(android.R.attr.selectableItemBackground, value, true);

        view.setBackgroundResource(value.resourceId);

        fillView(view);
        return view;
    }

    public static interface OnSavePreference {
        boolean save(Preference preference, Object newValue);
    }

    public OnSavePreference getOnSavePreference() {
        return this.onSavePreference;
    }

    public void setOnSavePreference(OnSavePreference onSavePreference) {
        this.onSavePreference = onSavePreference;
    }
}
