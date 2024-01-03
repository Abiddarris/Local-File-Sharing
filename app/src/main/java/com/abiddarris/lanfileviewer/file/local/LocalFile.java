package com.abiddarris.lanfileviewer.file.local;

import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.webkit.MimeTypeMap;
import androidx.documentfile.provider.DocumentFile;
import com.abiddarris.lanfileviewer.file.File;
import com.abiddarris.lanfileviewer.file.FileSource;
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

public class LocalFile implements File {

    private java.io.File file;
    private File parent;
    private LocalFileSource source;
    private static ExecutorService service = Executors.newCachedThreadPool();

    public static final String TAG = Log.getTag(LocalFile.class);

    protected LocalFile(LocalFileSource source, File parent, java.io.File file) {
        this.source = source;
        this.parent = parent;
        this.file = file;
    }

    @Override
    public void updateData(Callback callback) {
        callback.onDataUpdated(null);
    }

    @Override
    public void updateDataSync() {}

    @Override
    public boolean isDirectory() {
        source.getSecurityManager().checkRead(this);

        return file.isDirectory();
    }

    @Override
    public boolean isFile() {
        source.getSecurityManager().checkRead(this);

        return file.isFile();
    }

    @Override
    public File getParentFile() {
        return parent;
    }

    @Override
    public String getName() {
        source.getSecurityManager().checkRead(this);

        return file.getName();
    }

    @Override
    public File[] listFiles() {
        source.getSecurityManager().checkRead(this);

        java.io.File[] javaFiles = file.listFiles();
        if (javaFiles == null) return null;

        File[] files = new File[javaFiles.length];
        for (int i = 0; i < files.length; ++i) {
            files[i] = source.getFile(getPath() + "/" + javaFiles[i].getName());
        }

        return files;
    }

    @Override
    public String getPath() {
        return getParentFile().getPath() + "/" + getName();
    }
    
    @Override
    public String getAbsolutePath() {
        return file.getPath();
    }

    @Override
    public Uri toUri() {
        source.getSecurityManager().checkRead(this);

        return Uri.fromFile(file);
    }

    @Override
    public String getMimeType() {
        Uri uri = toUri();

        String type = null;
        String extension = MimeTypeMap.getFileExtensionFromUrl(uri.toString());
        if (extension != null) {
            type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension.toLowerCase());
        }

        return type == null ? "*/*" : type;
    }

    @Override
    public long length() {
        source.getSecurityManager().checkRead(this);

        return file.length();
    }

    @Override
    public long lastModified() {
        source.getSecurityManager().checkRead(this);

        return file.lastModified();
    }

    @Override
    public boolean createNewFile() throws IOException {
        source.getSecurityManager().checkWrite(this);

        return file.createNewFile();
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
    public boolean exists() {
        source.getSecurityManager().checkRead(this);

        return file.exists();
    }

    @Override
    public FileSource getSource() {
        return source;
    }

    @Override
    public InputStream newInputStream() throws IOException {
        source.getSecurityManager().checkRead(this);

        Log.debug.log(TAG, "opening input stream, path : " + getPath() + " abs :" + getAbsolutePath());
        return new FileInputStream(getAbsolutePath());
    }

    @Override
    public OutputStream newOutputStream() throws IOException {
        source.getSecurityManager().checkWrite(this);

        if (file.getParentFile() != null & file.getParentFile().canWrite()) {
            return new FileOutputStream(getAbsolutePath());
        }

        DocumentFile parentDocumentFile = source.findDocumentFile(getParentFile());
        if (parentDocumentFile != null) {
            DocumentFile file = parentDocumentFile.findFile(getName());
            file =
                    file == null
                            ? parentDocumentFile.createFile("application/notexist", getName())
                            : file;

            return source.getContext().getContentResolver().openOutputStream(file.getUri());
        }

        throw new IOException("Cannot open an outputstream");
    }

    @Override
    public Progress copy(File dest) {
        return copyInternal(dest, null);
    }

    private Progress copyInternal(File dest, OnCopyDoneListener listener) {
        Progress progress = new Progress(length());

        service.submit(
                () -> {
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
    public File getThumbnail() {
        return this;
    }

    private static interface OnCopyDoneListener {
        void onCopyDone(Progress progress);
    }
}
