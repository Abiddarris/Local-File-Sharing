package com.abiddarris.lanfileviewer.file.sharing;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.io.InputStream;

public class NetworkOutputStream extends OutputStream {
    
    private static final String CRLF = "\r\n"; // Line separator required by multipart/form-data.
   
    private boolean open;
    private boolean sent;
    private HttpURLConnection connection;
    private int headerLength;
    private long length;
    private long writtenBytes;
    private OutputStream stream;
    private String boundary = Long.toHexString(System.currentTimeMillis()); // Just generate some unique random value.
    private String partFooter = CRLF + "--" + boundary + "--" + CRLF;
    private String fileName;
    private String name;
    private String mimeType;
    
    NetworkOutputStream(URL url) throws IOException {
        connection = (HttpURLConnection) url.openConnection();
        connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);           
    }
    
    public void setRequestProperty(String key, String value) {
        connection.setRequestProperty(key,value);
    }

    public void open(long length) throws IOException {
        checkClosed();
        
        if(length < 0) {
            throw new IllegalArgumentException("length can not be less than zero");
        }
        
        this.length = length;
        
        checkArgument(fileName, "fileName is null, call setFileName(String) to set file name");
        checkArgument(name, "name is null, call setName(String) to set name");
        checkArgument(mimeType, "mimetype is null, call setMimeType(String) to set mime type");
        
        String partHeader = new StringBuilder()
            .append("--").append(boundary).append(CRLF)
            .append("Content-Disposition: form-data; name=\"").append(getName())
            .append("\"; filename=\"").append(getFileName()).append("\"").append(CRLF)
            .append("Content-Type: " + getMimeType()).append(CRLF)
            .append("Content-Transfer-Encoding: binary").append(CRLF)
            .append(CRLF)
            .toString();
        
        headerLength = partHeader.length();   
            
        connection.setFixedLengthStreamingMode(headerLength + length + partFooter.length());
        
        stream = connection.getOutputStream();
        open = true;
        
        write(partHeader.getBytes()); 
    }

    private void checkArgument(Object obj, String message) {
        if(obj == null) throw new NullPointerException(message);
    }
    
    private void checkClosed() {
        if(isOpen()) throw new IllegalStateException("Outputstream already opened");
    }
    private void checkOpen() {
        if(!isOpen()) throw new IllegalStateException("Outputstream is not opened");
    }
    
    private void checkIsSent() {
        if(!sent) throw new IllegalStateException("stream hasn't been sent yet");
    }

    @Override
    public void write(int oneByte) throws IOException {           
        checkOpen();
        writtenBytes++;
        
        stream.write(oneByte);
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        checkOpen();
        
        writtenBytes += len;
        
        stream.write(b,off,len);
    }
    
    @Override
    public void flush() throws IOException {
        checkOpen();
        
        if(writtenBytes == length + headerLength) {
            write(partFooter.getBytes());
            
            sent = true;
        }
        
        stream.flush();
    }
    
    @Override
    public void close() throws IOException {
        checkOpen();     
        
        connection.disconnect();
    }
    
    public int getResponseCode() throws IOException {
        checkIsSent();
        
        
        return connection.getResponseCode();
    }
    
    public Map<String,List<String>> getHeaderFields() {
        checkIsSent();
        
        return connection.getHeaderFields();
    }
    
    public InputStream getResponse() throws IOException {
        checkIsSent();
        
        return connection.getInputStream();
    }
    
    public InputStream getErrorResponse() {
        checkIsSent();
        
        return connection.getErrorStream();
    }

    public void setFileName(String fileName) {
        checkClosed();
        
        this.fileName = fileName;
    }

    public String getFileName() {
        return fileName;
    }

    public void setName(String name) {
        checkClosed();
        
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setMimeType(String mimeType) {
        checkClosed();
        
        this.mimeType = mimeType;
    }

    public String getMimeType() {
        return mimeType;
    }
    
    public boolean isOpen() {
        return open;
    }
    
}