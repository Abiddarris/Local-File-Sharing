package com.abiddarris.lanfileviewer.explorer;

import static com.abiddarris.lanfileviewer.file.Requests.*;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import androidx.fragment.app.Fragment;
import com.abiddarris.lanfileviewer.R;
import com.abiddarris.lanfileviewer.databinding.DialogTextInputBinding;
import com.abiddarris.lanfileviewer.file.File;
import com.abiddarris.lanfileviewer.file.FileSource;
import com.abiddarris.lanfileviewer.utils.HandlerLogSupport;
import com.gretta.util.log.Log;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FileNameInputValidator implements TextWatcher {

    private Context context;
    private DialogTextInputBinding binding;
    private ExecutorService executor = Executors.newSingleThreadExecutor();
    private FileSource source;
    private HandlerLogSupport handler = new HandlerLogSupport(new Handler(Looper.getMainLooper()));
    private String parentPath;
    
    public static final String TAG = Log.getTag(FileNameInputValidator.class);
    
    public FileNameInputValidator(DialogTextInputBinding binding, Explorer explorer) {
        this.binding = binding;
        this.context = explorer.getContext();
        
        File parent = explorer.getParent();
       
        parentPath = parent.getPath();
        source = parent.getSource();
    }

    @Override
    public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {}

    @Override
    public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {}

    @Override
    public void afterTextChanged(Editable editable) {
        String name = editable.toString();
        binding.positiveAction.setEnabled(false);
        if (name.isBlank()) {
            binding.textInput.setErrorEnabled(false);
            return;
        }

        File folder = source.getFile(parentPath + "/" + name);
        executor.execute(() -> {
            try {
                validateInput(folder);
            } catch (Exception e) {
                Log.err.log(TAG, e);
            }
        });
    }

    private void validateInput(final File folder) throws Exception {
        folder.updateDataSync(REQUEST_EXISTS);

        if (!folder.exists()) {
            handler.post((c) -> {
                binding.positiveAction.setEnabled(true);
                binding.textInput.setErrorEnabled(false);
            });
        } else {
            handler.post((c) -> {
                binding.textInput.setErrorEnabled(true);
                binding.textInput.setError(
                        context.getString(R.string.file_already_exists));
            });
        }
    }
    
    public File createFileFromInput() {
    	String name = binding.textInput.getEditText().getText().toString();
        File file = source.getFile(parentPath + "/" + name);
        return file;
    }
}
