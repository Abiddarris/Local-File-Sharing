package com.abiddarris.lanfileviewer.explorer;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.Button;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.abiddarris.lanfileviewer.R;
import com.abiddarris.lanfileviewer.databinding.LayoutPathButtonBinding;
import com.abiddarris.lanfileviewer.file.File;
import com.abiddarris.lanfileviewer.utils.HandlerLogSupport;
import java.util.List;
import java.util.ArrayList;

public class PathAdapter extends RecyclerView.Adapter<PathAdapter.ViewHolder> {

    private Context context;
    private Explorer explorer;
    private HandlerLogSupport handler = new HandlerLogSupport(new Handler(Looper.getMainLooper()));
    private LayoutInflater inflater;
    private String[] paths = new String[0];
    private String parent;
    private RecyclerView recyclerView;

    public PathAdapter(RecyclerView recyclerView) {
        this.recyclerView = recyclerView;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup group, int type) {
        return new ViewHolder(LayoutPathButtonBinding.inflate(inflater));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int index) {
        holder.name.setText(paths[index]);
        holder.name.setBackgroundColor(
                ContextCompat.getColor(context, android.R.color.transparent));

        if (index == paths.length - 1)
            holder.name.setTextColor(ContextCompat.getColor(context, R.color.colorAccent));
        else holder.name.setTextColor(Color.WHITE);

        holder.name.setOnClickListener(
                (v) -> {
                    StringBuilder path = new StringBuilder();
                    path.append(parent);
                    for (int i = 0; i <= index; ++i) {
                        path.append(paths[i]);
                        if (i != index) path.append("/");
                    }
                    File file = explorer.getParent().getSource().getFile(path.toString());
                    explorer.open(file);
                });
    }

    @Override
    public int getItemCount() {
        return paths.length;
    }

    public void setExplorer(Explorer explorer) {
        this.explorer = explorer;
        explorer.addOnUpdatedListener((v) -> {
            File file = explorer.getParent();
         
            String filePath = file.getPath();
            if(filePath.startsWith("/")) filePath = filePath.substring(1);
            String[] paths = filePath.split("/");
            setPaths(paths);

            handler.post((c) -> {
                notifyDataSetChanged();
            });
        });
    }
    
    
    public Explorer getExplorer() {
        return explorer;
    }

    public String[] getPaths() {
        return this.paths;
    }

    public void setPaths(String[] paths) {
        this.paths = paths;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private Button name;

        public ViewHolder(LayoutPathButtonBinding binding) {
            super(binding.getRoot());

            name = binding.name;
        }
    }

    public Context getContext() {
        return this.context;
    }

    public void setContext(Context context) {
        this.context = context;

        inflater = LayoutInflater.from(context);
    }
}
