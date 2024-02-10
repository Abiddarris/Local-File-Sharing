package com.abiddarris.lanfileviewer.file;

import android.annotation.SuppressLint;
import android.net.Uri;
import com.abiddarris.lanfileviewer.ApplicationCore;
import com.abiddarris.lanfileviewer.utils.BaseRunnable;
import com.abiddarris.lanfileviewer.utils.TaskResult;
import com.gretta.util.log.Log;
import com.gretta.util.recycler.Poolable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import org.json.JSONObject;

public abstract class File extends Poolable implements Requests {
    
    private static SimpleDateFormat timeFormatter = new SimpleDateFormat("dd LLL YYYY HH.mm.ss");
    private static final String TAG = Log.getTag(File.class);
    
    private String parent;
    private FileSource source;
    private long validFrom = -1;
    private Map<String, Value> values = new HashMap<>();
    private String name;
    private String path;
    
    protected File(FileSource source, String parent, String name) {
        this.source = source;
        this.parent = parent;
        this.name = name;
    }
    
    protected void setPath(String path) {
        this.path = path;
    }
    
    protected void put(String key, Object obj) {
        checkNotFreed();
        values.put(key, new Value(System.currentTimeMillis(), obj));
    }
    
    protected <T> T get(String key) {
        return get(key, null);
    }
    
    @SuppressLint("unchecked")
    protected <T> T get(String key, String requestKey) {
        checkNotFreed();
        Value value = values.get(key);
        if(value == null && requestKey == null) {
            throw new DataDoesNotExistsException(key + " value does not exist. Call updateData() or updateDataSync() before call this function!");
        } else if(value == null){
            updateDataSync(requestKey);
            return get(key);
        }
        
        if(validFrom != -1 && value.setTime < validFrom) {
            if(requestKey == null) {
                throw new InvalidDataException(
                String.format("%s is last updated at %s [%s] but datas are valid only from %s [%s]",
                    key, formatTime(value.setTime), value.setTime, formatTime(getValidFrom()), getValidFrom()));
            }
            updateDataSync(requestKey);
            return get(key);
        }
        
        return (T)value.data;
    }
    
    private static String formatTime(long time) {
        Date date = new Date(time);
        return timeFormatter.format(date);
    }
    
    public void setValidFrom(long time) {
        checkNotFreed();
        validFrom = time;
    }
    
    public long getValidFrom() {
        checkNotFreed();
        
        return validFrom;
    }
    
    public final String getParent() {
        return parent;
    }
    
    public final File getParentFile() {
        return getParent() == null ? null : source.getFile(getParent());
    }
    
    public final TaskResult updateData() {
        return updateData((e) -> handleDefaultResult(e));
    }
    
    public final TaskResult updateData(Callback callback) {
        return updateData(callback, createDefaultReqeustKeys());
    }
    
    public final TaskResult updateData(String... requests) {
        return updateData(e -> handleDefaultResult(e), requests);
    }

    public final TaskResult updateData(Callback callback, String... requests) {
        checkNotFreed();
        return new TaskResult(getSource().runOnBackground(new BaseRunnable(c -> {
            Exception exception;
            try {
                updateDataSync(requests);
                
                exception = null;        
            } catch (Exception e) {
                exception = e;     
                Log.err.log(TAG, e);            
            } 
            final Exception e1 = exception;        
            ApplicationCore.getMainHandler()
                .post(ctx -> callback.onDataUpdated(e1));
        })));
    }
    
    public final void updateDataSync() {
        updateDataSync(createDefaultReqeustKeys());
    }
    
    public final void updateDataSync(String... requests) {
        checkNotFreed();
        try {
            updateInternal(requests);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Cannot update data", e);
        }
    }
    
    private String[] createDefaultReqeustKeys() {
       return new String[] {
             REQUEST_LIST, REQUEST_IS_DIRECTORY, REQUEST_IS_FILE,
             REQUEST_GET_MIME_TYPE, REQUEST_GET_LENGTH, REQUEST_GET_LAST_MODIFIED,
             REQUEST_EXISTS, REQUEST_ABSOLUTE_PATH
       };
    }
    
    private void handleDefaultResult(Exception e) {
        if(e != null) {
            Log.err.log(TAG, e);
        }
    }
    
    private void checkReadPermission() {
        checkNotFreed();
        
        getSource()
            .getSecurityManager()
            .checkRead(this);
    }
    
    public final FileSource getSource() {
        return source;
    }

    protected void updateInternal(String[] requests) throws Exception {
        for(String request : requests) {
            if(REQUEST_GET_FILES_TREE.equalsIgnoreCase(request)) {
                List<File> filesTree = new ArrayList<>();
        
                getFilesTree(filesTree, this);
        
                put(KEY_FILES_TREE, filesTree);
            } else if(REQUEST_GET_FILES_TREE_SIZE.equalsIgnoreCase(request)) {
                List<File> filesTree = get(KEY_FILES_TREE, REQUEST_GET_FILES_TREE);
        
                long size = 0;
        
                for(File file : filesTree) {
                    if((Boolean)file.get(KEY_IS_FILE, REQUEST_IS_FILE)) {
                        size += (Long)file.get(KEY_LENGTH, REQUEST_GET_LENGTH);
                    }
                }
        
                put(KEY_FILES_TREE_SIZE, size);
            }
        }
    }
    
    public final FilePointer getFilePointer() {
        return source.getFilePointer(getPath());
    }

    public boolean isDirectory() {
        checkReadPermission();
        
        return get(KEY_IS_DIRECTORY);
    }

    public boolean isFile() {
        checkReadPermission();
        
        return get(KEY_IS_FILE);
    }
    
    public String[] list() {
        checkReadPermission();
        
        return get(KEY_LIST);
    }

    public File[] listFiles() {
        checkNotFreed();
        String[] names = list();
        if(names == null) return null;
        
        File[] files = new File[names.length];
        for(int i = 0; i < files.length; i++) {
            files[i] = source.getFile(this, names[i]);
        }
        return files;
    }

    public String getAbsolutePath() {
        checkReadPermission();
        
        return get(KEY_ABSOLUTE_PATH);
    }

    public String getMimeType() {
        checkReadPermission();
        
        return get(KEY_MIME_TYPE);
    }

    public long length() {
        checkReadPermission();
        
        return get(KEY_LENGTH);
    }

    public long lastModified() {
        checkReadPermission();
        
        return get(KEY_LAST_MODIFIED);
    }
    
    public boolean exists() {
        checkReadPermission();
        
        return get(KEY_EXISTS);
    }
    
    public List<File> getFilesTree() {
        return get(KEY_FILES_TREE);
    }
    
    public long getFilesTreeSize() {
        return get(KEY_FILES_TREE_SIZE);
    }
    
    public final String getName() {
        return name;
    }

    public final String getPath() {
        return path != null ? path : parent + 
            "/" + getName();
    }
    
    public final Progress copy(File dest) {
        return copy(dest, (progress) -> {});
    }
    
    public final Progress move(File dest) {
        return move(dest, (progress) -> {});
    }
    
    public abstract Uri toUri();

    public abstract boolean makeDirs();

    public abstract OutputStream newOutputStream() throws IOException;

    public abstract InputStream newInputStream() throws IOException;

    public abstract Progress copy(File dest, OnOperationDoneListener listener);

    public abstract boolean rename(String newName);
    
    public abstract boolean delete();
    
    public abstract Progress move(File dest, OnOperationDoneListener listener); 
    
    public abstract void createThumbnail(ThumbnailCallback callback);
    
    private void getFilesTree(List<File> files, File parent) {
        files.add(parent);
        
        parent.updateDataSync(REQUEST_IS_FILE, REQUEST_LIST);
        if(parent.isFile()) {
            return;
        }
        
        File[] children = parent.listFiles();
        if(children == null) return;
        
        for(File file : children) {
            getFilesTree(files,file);
        }
    }
    
    public static interface Callback {
        void onDataUpdated(Exception e);
    }
    
    public static interface ThumbnailCallback {
        void onThumbnailCreated(Uri thumb);
    }

    public static class Progress {

        private volatile boolean completed;
        private volatile boolean cancel;
        private volatile Exception e;
        private volatile long size = -1;
        private volatile long currentProgress;

        public Progress() {}

        public Progress(long size) {
            this.size = size;
        }

        public boolean isCompleted() {
            return this.completed;
        }

        public void setCompleted(boolean completed) {
            this.completed = completed;
        }

        public long getSize() {
            while(size == - 1) {
                if(Thread.currentThread().isInterrupted()) break;
            }
            return this.size;
        }

        public long getCurrentProgress() {
            return this.currentProgress;
        }

        public void setCurrentProgress(long currentProgress) {
            this.currentProgress = currentProgress;
        }

        public Exception getException() {
            return this.e;
        }

        public void setException(Exception e) {
            this.e = e;
        }

        public void setSize(long size) {
            this.size = size;
        }

        public boolean isCancel() {
            return this.cancel;
        }

        public void setCancel(boolean cancel) {
            this.cancel = cancel;
        }
    }
    
    public static interface OnOperationDoneListener {
        void onOperationDone(Progress progress);
    }
   
    private class Value {
        
        private long setTime;
        private Object data;
        
        private Value(long setTime, Object data) {
            this.setTime = setTime;
            this.data = data;
        }
    }
    
    
}
