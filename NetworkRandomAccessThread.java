package com.abiddarris.lanfileviewer;

import android.os.IBinder;
import android.view.inputmethod.InputBinding;
import com.gretta.util.log.Log;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.RandomAccessFile;
import java.net.ServerSocket;
import java.io.IOException;
import java.net.Socket;
import org.json.JSONObject;

public class NetworkRandomAccessThread implements Runnable {
    
    private File file;
    private ServerSocket serverSocket;
    
    private static final String TAG = Log.getTag(NetworkRandomAccessThread.class);
    
    public static final int REQUEST_GET_LENGTH = 1;
    public static final int REQUEST_READ = 2;
    public static final int REQUEST_SEEKTO = 4;
    public static final String KEY_POSITION = "position";
    public static final String KEY_REQUEST = "request";
    public static final String KEY_SIZE = "size";
    
    public NetworkRandomAccessThread(File file) throws IOException {
        this.file = file;
        serverSocket = new ServerSocket(0);
    }
    
    @Override
    public void run() {
        try {
            Socket socket = serverSocket.accept();
            serverSocket.close();
            
            RandomAccessFile randomAccessFile = new RandomAccessFile(file, "r");
            byte[] buf = new byte[0];
            
            DataInputStream input = new DataInputStream(socket.getInputStream());
            DataOutputStream output = new DataOutputStream(socket.getOutputStream());
            while(true) {
                JSONObject request = new JSONObject(input.readUTF());
                int requestCode = request.getInt(KEY_REQUEST);
                if(requestCode == REQUEST_GET_LENGTH) {
                    output.writeLong(randomAccessFile.length());
                }
                if(requestCode == REQUEST_SEEKTO) {
                    long pos = request.getLong(KEY_POSITION);
                    randomAccessFile.seek(pos);
                }
                if(requestCode == REQUEST_READ) {
                    int len = request.getInt(KEY_SIZE);
                    if(buf.length != len) {
                        buf = new byte[len];
                    }
                    len = randomAccessFile.read(buf);
                    output.writeInt(len);
                    output.write(buf,0,len);
                }
            }
        } catch (Exception e) {
            Log.err.log(TAG,e);
        }
    }
    
    public int getPort() {
    	return serverSocket.getLocalPort();
    }
    
}
