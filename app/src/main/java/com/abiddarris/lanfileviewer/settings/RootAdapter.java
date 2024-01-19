package com.abiddarris.lanfileviewer.settings;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.recyclerview.widget.RecyclerView.Adapter;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;
import com.abiddarris.lanfileviewer.R;
import com.abiddarris.lanfileviewer.databinding.LayoutRootFileEditorBinding;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class RootAdapter extends Adapter<RootAdapter.RootViewHolder>{
    
    private LayoutInflater inflater;
    private List<File> roots = new ArrayList<>();
    
    public RootAdapter(Context context) {
        inflater = LayoutInflater.from(context);
    }

    @Override
    public RootViewHolder onCreateViewHolder(ViewGroup group, int type) {
        View view = inflater.inflate(R.layout.layout_root_file_editor,group, false);
        return new RootViewHolder(LayoutRootFileEditorBinding
            .bind(view));
    }
    

    @Override
    public void onBindViewHolder(RootViewHolder holder, int index) {
        LayoutRootFileEditorBinding binding = holder.binding;
        File root = roots.get(index);
       
        binding.name.setText(root.getName());
        binding.absolutePath.setText(root.getAbsolutePath());
        binding.remove.setOnClickListener((v) -> {
            roots.remove(root);
            notifyItemRemoved(index);
        });
    }
    

    @Override
    public int getItemCount() {
        return roots.size();
    }
    
    public List<File> getRoots() {
        return this.roots;
    }
    
    public void setRoots(List<File> roots) {
        this.roots = roots;
        
        notifyDataSetChanged();
    }
    
    protected static class RootViewHolder extends ViewHolder {
        
        private LayoutRootFileEditorBinding binding;
        
        protected RootViewHolder(LayoutRootFileEditorBinding binding) {
            super(binding.getRoot());
            
            this.binding = binding;
        }
        
    }

}
