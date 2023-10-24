package com.abiddarris.lanfileviewer.file.network;

import com.abiddarris.lanfileviewer.BaseRunnable;
import java.io.DataInputStream;
import java.io.InputStream;
import java.io.OutputStream;

public abstract class RequestHandler extends BaseRunnable {

    private InputStream inputStream;
    private OutputStream outputStream;
    private String request;

    public InputStream getInputStream() {
        return this.inputStream;
    }

    public void setInputStream(InputStream inputStream) {
        this.inputStream = inputStream;
    }

    public OutputStream getOutputStream() {
        return this.outputStream;
    }

    public void setOutputStream(OutputStream outputStream) {
        this.outputStream = outputStream;
    }

    public String getRequest() {
        return this.request;
    }

    public void setRequest(String request) {
        this.request = request;
    }
}
