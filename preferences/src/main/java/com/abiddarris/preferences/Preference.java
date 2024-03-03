package com.abiddarris.preferences;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import com.abiddarris.preferences.databinding.LayoutPreferenceBinding;

public class Preference {

    private Context context;
    private String key;
    private String title;
    private String summary = "";
    private SummaryProvider summaryProvider;
    private View view;

    public Preference(Context context, String key) {
        this.context = context;
        this.key = key;
    }

    public String getTitle() {
        return this.title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setTitle(int resId) {
        setTitle(context.getString(resId));
    }

    public String getSummary() {
        return this.summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public Context getContext() {
        return this.context;
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
        return LayoutPreferenceBinding.inflate(LayoutInflater.from(getContext())).getRoot();
    }

    protected void fillView(View view) {
        if(getSummaryProvider() != null)
            setSummary(getSummaryProvider().getSummary(this));
        
        LayoutPreferenceBinding binding = LayoutPreferenceBinding.bind(view);
        binding.title.setText(getTitle());
        binding.summary.setText(getSummary());
    }

    View getView() {
        if (view == null) {
            view = createView();
        }
        fillView(view);
        return view;
    }
    
}
