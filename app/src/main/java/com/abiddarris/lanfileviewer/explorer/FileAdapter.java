package com.abiddarris.lanfileviewer.explorer;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import com.abiddarris.lanfileviewer.R;
import com.abiddarris.lanfileviewer.databinding.LayoutFileCardBinding;
import com.abiddarris.lanfileviewer.file.File;
import com.abiddarris.lanfileviewer.file.Files;
import com.abiddarris.lanfileviewer.utils.DrawableTinter;
import com.abiddarris.lanfileviewer.utils.HandlerLogSupport;
import com.bumptech.glide.Glide;
import com.gretta.util.log.Log;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class FileAdapter extends RecyclerView.Adapter<FileAdapter.ViewHolder> {
    
    public static final String TAG = Log.getTag(FileAdapter.class);
    
    private boolean showBox;
    private Context context;
    private File[] files = new File[0];
    private Explorer explorer;
    private HandlerLogSupport handler = new HandlerLogSupport(new Handler(Looper.getMainLooper()));
    private LayoutInflater inflater;
    private RecyclerView recyclerView;
    
    public FileAdapter(Explorer explorer) {
        this.explorer = explorer;
        this.context = explorer.getContext();
        inflater = LayoutInflater.from(context);
    }
    
    public HandlerLogSupport getMainThread() {
    	return handler;
    }
    
    public void setFiles(File[] files) {
        handler.post((c)-> {
            this.files = files;
            notifyDataSetChanged();
        });
        handler.post((c) -> recyclerView.smoothScrollToPosition(0));
    }
    
    public File[] getFiles() {
    	return files;
    }
    
    public File get(int pos) {
    	return files[pos];
    }
    
    public RecyclerView getAttachedRecyclerView() {
        return recyclerView;
    }
    
    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        
        this.recyclerView = recyclerView;
    }

    @Override
    public void onDetachedFromRecyclerView(RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
        
        recyclerView = null;
    }
    
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup group, int viewType) {
        View view = inflater.inflate(R.layout.layout_file_card, group, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        LayoutFileCardBinding binding = holder.binding;
        File file = files[position];
        
        Mode mode = explorer.getMode();
        mode.onViewBind(holder,position);

        binding.getRoot().setOnClickListener(v -> mode.onItemClickListener(holder,position));
        binding.getRoot().setOnLongClickListener(v -> {
            mode.onItemLongClickListener(holder,position);
            return true;
        });
        binding.name.setText(file.getName());
        
        setIcon(binding.thumbnail,file);
        setDate(binding.date, file);
        setItem(binding.item, file);
    }

    private void setItem(TextView textView, final File file) {
        if(file.isDirectory()) {
            File[] files = file.listFiles();
            if(files == null) {
                textView.setText("ERROR");
                return;
            }
            
            int item = file.listFiles().length;
            textView.setText(item + " item");
            return;
        }
        textView.setText(Files.formatSize(file.length()));
    }
    private void setDate(final TextView textView, final File file) {
        textView.setText(Files.formatToDate(file.lastModified()));
    }

    private void setIcon(final ImageView imageView, final File file) {
        if(hasThumbnail(file)) {
            file.createThumbnail((uri) -> {
                Log.debug.log(TAG, "icon uri " + uri);
                Glide.with(context)
                    .load(uri)
                    .timeout(6000)
                    .error(R.drawable.icons8_file)
                    .centerCrop()
                    .into(imageView);
            });
        
            return;
        }
        Glide.with(context) 
            .clear(imageView);
        
        Log.debug.log(TAG, file.getPath() + " does not have thumbnail");
        DrawableTinter.withContext(context)
            .withColor(R.color.colorPrimary)
            .withDrawable(file.isFile() ? R.drawable.icons8_file : R.drawable.icons8_folder)
            .tint()
            .applyTo(imageView);
    }

    private boolean hasThumbnail(final File file) {
        String mimeType = file.getMimeType();
        if(mimeType == null) return false;
        
        if(mimeType.contains("image") || mimeType.contains("video")) {
            return true;
        }
        return false;
    }

    @Override
    public int getItemCount() {
        return files.length;
    }
    
    public static class ViewHolder extends RecyclerView.ViewHolder {
        
        LayoutFileCardBinding binding;
        
        public ViewHolder(View view) {
            super(view);
            
            binding = LayoutFileCardBinding.bind(view);
        }
        
    }
}
