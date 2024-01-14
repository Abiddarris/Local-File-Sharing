package com.abiddarris.lanfileviewer.file;

import android.net.Uri;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import org.json.JSONObject;

public abstract class File {
    
    private File parentFile;
    
    protected File() {}
    
    protected File(File parentFile) {
        this.parentFile = parentFile;
    }

    public abstract void updateData(Callback callback);

    public abstract void updateDataSync();

    public abstract FileSource getSource();

    public abstract boolean isDirectory();

    public abstract boolean isFile();

    public final File getParentFile() {
        return parentFile;
    }

    public abstract String getName();

    public abstract File[] listFiles();

    public abstract String getAbsolutePath();

    public abstract Uri toUri();

    public abstract String getMimeType();

    public abstract long length();

    public abstract long lastModified();

    public abstract boolean createNewFile() throws IOException;

    public abstract boolean makeDirs();

    public abstract boolean exists();

    public abstract OutputStream newOutputStream() throws IOException;

    public abstract InputStream newInputStream() throws IOException;

    public abstract Progress copy(File dest);

    public abstract boolean rename(String newName);
    
    public abstract boolean delete();
    
    public abstract Progress move(File dest);
    
    public abstract Object getThumbnail();
    
    public abstract String getPath();
    
    public static interface Callback {
        void onDataUpdated(Exception e);
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
}
