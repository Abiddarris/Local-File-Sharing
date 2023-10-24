package com.abiddarris.lanfileviewer.file.network;

import android.util.Base64;
import com.abiddarris.lanfileviewer.ApplicationCore;
import com.abiddarris.lanfileviewer.file.File;
import com.abiddarris.lanfileviewer.file.local.LocalFileSource;
import com.gretta.util.log.Log;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HTTPRequestHandler extends RequestHandler {
    
    private static final Pattern URL_PATTERN = Pattern.compile("GET /(.*) HTTP");
    private static final Pattern RANGE_HEADER_PATTERN = Pattern.compile("[R,r]ange:[ ]?bytes=(\\d*)-");
    
    @Override
    public void onExecute() throws Exception {
        String encodedPath = findUri(getRequest());
        String decodedPath = encodedPath.replace("%20", " ");
        
        File file = LocalFileSource.getDefaultLocalSource(ApplicationCore.getApplication())
            .getFile(new String(decodedPath));
        
        long rangeOffset = findRangeOffset(getRequest());
        rangeOffset = Math.max(0, rangeOffset);
        long length = file.length();
        long contentLength = length - rangeOffset;
        
        String response = "HTTP/1.1 206 PARTIAL CONTENT\r\n" +
            "Accept-Ranges: bytes\r\n" +
            "Content-Type: " + file.getMimeType() + "\r\n" +
            "Content-Range: bytes " + rangeOffset + "-" + (length) + "/" + length + "\r\n" +
            "Content-Length: " + contentLength + "\r\n\r\n";
               
        getOutputStream().write(response.getBytes(StandardCharsets.UTF_8));
        getOutputStream().flush();
        
        Log.debug.log(getTag(),"Response : ");
        Log.debug.log(getTag(),response);
        
        RandomAccessFile input = new RandomAccessFile(new java.io.File(file.getPath()),"r");
        byte[] buf = new byte[1024];
        int len = 0;              
        input.seek(rangeOffset);
        while((len = input.read(buf)) != -1) {                 
            getOutputStream().write(buf,0,len);                   
        }
        getOutputStream().flush();           
    }
       
    private long findRangeOffset(String request) {
        Matcher matcher = RANGE_HEADER_PATTERN.matcher(request);
        if (matcher.find()) {
            String rangeValue = matcher.group(1);
            return Long.parseLong(rangeValue);
        }
        return -1;
    }
    
    private String findUri(String request) {
        Matcher matcher = URL_PATTERN.matcher(request);
        if (matcher.find()) {
            return matcher.group(1);
        }
        throw new IllegalArgumentException("Invalid request `" + request + "`: url not found!");
    }
}
