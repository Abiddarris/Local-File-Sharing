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
import com.abiddarris.lanfileviewer.utils.DrawableTinter;
import com.bumptech.glide.Glide;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class FileAdapter extends RecyclerView.Adapter<FileAdapter.ViewHolder> {
    
    private boolean showBox;
    private Context context;
    private File[] files = new File[0];
    private Explorer explorer;
    private Handler handler = new Handler(Looper.getMainLooper());
    private LayoutInflater inflater;
    private static final SimpleDateFormat anotherYear = new SimpleDateFormat("dd LLL YYYY HH.mm");
    private static final SimpleDateFormat currentYear = new SimpleDateFormat("dd LLL HH.mm");
    
    public FileAdapter(Explorer explorer) {
        this.explorer = explorer;
        this.context = explorer.getContext();
        inflater = LayoutInflater.from(context);
    }
    
    public Handler getMainThread() {
    	return handler;
    }
    
    public void setFiles(File[] files) {
    	this.files = files;
        
        handler.post(()-> notifyDataSetChanged());
    }
    
    public File get(int pos) {
    	return files[pos];
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
        
        double length = file.length();
        
        double kbLength = length / 1024;
        if(kbLength < 0.9) {
        	textView.setText(Math.round(length) + " B");
            return;
        }
        
        if(kbLength < 100) {
            textView.setText(formatSize(kbLength) + " KB");
            return;
        }
        
        double mbLength = kbLength / 1024;
        if(mbLength < 0.9) {
            textView.setText(Math.round(kbLength) + " KB");
            return;
        }
        
        if(mbLength < 100) {
            textView.setText(formatSize(mbLength) + " MB");
            return;
        }
        
        double gbLength = mbLength / 1024;
        if(gbLength < 0.9) {
            textView.setText(Math.round(mbLength) + " MB");
            return;
        }
        
        if(gbLength < 100) {
            textView.setText(formatSize(gbLength) + " GB");
            return;
        }
        
        textView.setText(Math.round(gbLength) + " GB");
    }

    private String formatSize(final double length) {
        return String.format("%.2f", length);
    }

    private void setDate(final TextView textView, final File file) {
        Calendar fileTime = Calendar.getInstance();
        int currentYear = fileTime.get(Calendar.YEAR);
        
        fileTime.setTimeInMillis(file.lastModified());
        int fileYear = fileTime.get(Calendar.YEAR);
        
        if(fileYear == currentYear) {
            textView.setText(FileAdapter.currentYear
                .format(fileTime.getTime()));
            return;
        }
        
        textView.setText(FileAdapter.anotherYear
                .format(fileTime.getTime()));
    }

    private void setIcon(final ImageView thumbnail, final File file) {
        if(hasThumbnail(file)) {
            Glide.with(context)
                .load(file.toUri())
                .error(R.drawable.icons8_file)
                .centerCrop()
                .into(thumbnail);
            return;
        }
        Glide.with(context) 
            .clear(thumbnail);
        
        DrawableTinter.withContext(context)
            .withColor(R.color.colorPrimary)
            .withDrawable(file.isFile() ? R.drawable.icons8_file : R.drawable.icons8_folder)
            .tint()
            .applyTo(thumbnail);
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
