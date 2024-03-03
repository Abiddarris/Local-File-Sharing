package com.abiddarris.preferences;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import com.abiddarris.preferences.databinding.LayoutPreferenceBinding;

public class Preference {

    private Context context;
    private String key;
    private String title;
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

    public Context getContext() {
        return this.context;
    }
    
    protected View createView() {
        return LayoutPreferenceBinding.inflate(LayoutInflater
            .from(getContext()))
            .getRoot();
    }
    
    protected void fillView(View view) {
        LayoutPreferenceBinding binding = LayoutPreferenceBinding.bind(view);
        binding.title.setText(title);
    }

    View getView() {
        if(view == null) {
            view = createView();
        }
        fillView(view);
        return view;
    }
}
