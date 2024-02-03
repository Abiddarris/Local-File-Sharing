package com.abiddarris.lanfileviewer.file.local;

import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.webkit.MimeTypeMap;
import androidx.documentfile.provider.DocumentFile;
import com.abiddarris.lanfileviewer.file.File;
import com.abiddarris.lanfileviewer.file.FileSource;
import com.abiddarris.lanfileviewer.file.Files;
import com.abiddarris.lanfileviewer.utils.HandlerLogSupport;
import com.abiddarris.lanfileviewer.utils.Thumbnails;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.gretta.util.log.Log;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.security.auth.callback.Callback;

public class LocalFile extends File {

    private java.io.File file;
    private LocalFileSource source;
    
    private static ExecutorService service = Executors.newCachedThreadPool();
    private static HandlerLogSupport handler = new HandlerLogSupport(new Handler(Looper.getMainLooper()));
    
    public static final String TAG = Log.getTag(LocalFile.class);

    protected LocalFile(LocalFileSource source, File parent, java.io.File file) {
        super(source, parent.getPath(), file.getName());
        
        this.source = source;
        this.file = file;
    }
    
    protected LocalFile(LocalFileSource source, String parent, String name) {
        super(source, parent, name);
        
        File parentFile = getParentFile();
        parentFile.updateDataSync(REQUEST_ABSOLUTE_PATH);
        
        getSource()
            .free(parentFile);
        
        this.source = source;
        this.file = new java.io.File(parentFile.getAbsolutePath(), name);
    }

    @Override
    protected void updateInternal(String[] requests) throws Exception { 
        super.updateInternal(requests);
        
        for(String request : requests) {
            if(REQUEST_LIST.equalsIgnoreCase(request)) {
                String[] names = file.list();

                put(KEY_LIST, names);
            } else if(REQUEST_IS_DIRECTORY.equalsIgnoreCase(request)) {
                put(KEY_IS_DIRECTORY, file.isDirectory());
            } else if(REQUEST_IS_FILE.equalsIgnoreCase(request)) {
                put(KEY_IS_FILE, file.isFile());
            } else if(REQUEST_GET_MIME_TYPE.equalsIgnoreCase(request)) {
                String type = null;
                String extension = Files.getExtension(this);
                if (extension != null) {
                    type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension.toLowerCase());
                }    
                put(KEY_MIME_TYPE, type == null ? "*/*" : type);
            } else if(REQUEST_GET_LENGTH.equalsIgnoreCase(request)) {
                put(KEY_LENGTH, file.length());
            } else if(REQUEST_GET_LAST_MODIFIED.equals(request)) {
                put(KEY_LAST_MODIFIED, file.lastModified());
            } else if(REQUEST_EXISTS.equalsIgnoreCase(request)) {
                put(KEY_EXISTS, file.exists());
            } else if(REQUEST_ABSOLUTE_PATH.equalsIgnoreCase(request)) {
                put(KEY_ABSOLUTE_PATH, file.getAbsolutePath());
            }
        }
    }

    @Override
    public Uri toUri() {
        source.getSecurityManager()
            .checkRead(this);

        return Uri.fromFile(file);
    }

    @Override
    public boolean makeDirs() {
        source.getSecurityManager().checkWrite(this);

        if (file.getParentFile() != null & file.getParentFile().canWrite()) {
            return file.mkdirs();
        }

        if (!getParentFile().exists()) getParentFile().makeDirs();

        DocumentFile documentFile = source.findDocumentFile(getParentFile());
        if (documentFile != null) {
            return documentFile.createDirectory(getName()) != null;
        }

        return false;
    }

    @Override
    public InputStream newInputStream() throws IOException {
        source.getSecurityManager().checkRead(this);

        return new FileInputStream((String)get(KEY_ABSOLUTE_PATH, REQUEST_ABSOLUTE_PATH));
    }

    @Override
    public OutputStream newOutputStream() throws IOException {
        source.getSecurityManager().checkWrite(this);

        if (file.getParentFile() != null & file.getParentFile().canWrite()) {
            return new FileOutputStream((String)get(KEY_ABSOLUTE_PATH, REQUEST_ABSOLUTE_PATH));
        }

        DocumentFile parentDocumentFile = source.findDocumentFile(getParentFile());
        if (parentDocumentFile != null) {
            DocumentFile file = parentDocumentFile.findFile(getName());
            file = file == null ? parentDocumentFile.createFile("application/notexist", getName()) : file;

            return source.getContext().getContentResolver().openOutputStream(file.getUri());
        }

        throw new IOException("Cannot open " + file.getPath() +" an outputstream");
    }

    @Override
    public Progress copy(File dest) {
        return copyInternal(dest, null);
    }

    private Progress copyInternal(File dest, OnCopyDoneListener listener) {
        Progress progress = new Progress((Long)get(KEY_LENGTH, REQUEST_GET_LENGTH));

        service.submit(() -> {
                    try {
                        BufferedInputStream inputStream = new BufferedInputStream(newInputStream());
                        BufferedOutputStream outputStream =
                                new BufferedOutputStream(dest.newOutputStream());
                        byte[] buf = new byte[1024 * 4];
                        int len;
                        while ((len = inputStream.read(buf)) != -1) {
                            if (progress.isCancel()) {
                                break;
                            }
                            outputStream.write(buf, 0, len);
                            progress.setCurrentProgress(progress.getCurrentProgress() + len);
                        }
                        outputStream.flush();
                        outputStream.close();
                        inputStream.close();
                    } catch (IOException e) {
                        Log.err.log(TAG, e);
                        progress.setException(e);
                    } finally {
                        progress.setCompleted(true);
                        if (listener != null) listener.onCopyDone(progress);
                    }
                });
        return progress;
    }

    @Override
    public boolean rename(String newName) {
        if (file.canWrite()) {
            java.io.File dest = new java.io.File(file.getParentFile(), newName);
            return file.renameTo(dest);
        }

        DocumentFile file = source.findDocumentFile(this);
        if (file == null) {
            return false;
        }
        return file.renameTo(newName);
    }

    @Override
    public boolean delete() {
        source.getSecurityManager().checkDelete(this);

        if (file.canWrite()) {
            return file.delete();
        }

        DocumentFile file = source.findDocumentFile(this);
        if (file == null) {
            return false;
        }
        return file.delete();
    }

    @Override
    public Progress move(File dest) {
        source.getSecurityManager().checkRead(this);
        
        updateDataSync(REQUEST_ABSOLUTE_PATH);
        
        java.io.File destFile = new java.io.File(dest.getAbsolutePath());
        if (file.canWrite()
                && destFile.getParentFile() != null
                && destFile.getParentFile().canWrite()) {
            boolean success = file.renameTo(destFile);
            if (success) {
                Progress progress = new Progress(1);
                progress.setCompleted(true);
                progress.setCurrentProgress(1);
                return progress;
            }
        }

        Progress progress = copyInternal(dest, (p) -> {
                if (p.isCancel() || p.getException() != null) {
                    dest.delete();
                    return;
                }
                delete();
            });

        return progress;
    }

    @Override
    public void createThumbnail(ThumbnailCallback callback) {
        service.submit(() -> {
            java.io.File thumb = Thumbnails.getThumbnail(getSource().getContext(), file);
            handler.post((c) -> {
                callback.onThumbnailCreated(Uri.fromFile(thumb));    
            });
        });
    }

    private static interface OnCopyDoneListener {
        void onCopyDone(Progress progress);
    }
}
