package com.abiddarris.lanfileviewer;

import com.gretta.util.log.Log;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.Socket;
import org.json.JSONException;
import org.json.JSONObject;

public class NetworkRandomAccess {

    private NetworkFileClient client;
    private DataInputStream input;
    private DataOutputStream output;
    private NetworkFile file;
    private long length = -1;
    private Object lock = new Object();
    private Socket socket;
    
    private static final String TAG = Log.getTag(NetworkRandomAccess.class);
    
    public NetworkRandomAccess(NetworkFileClient client, NetworkFile file) {
        this.client = client;
        this.file = file;
    }

    public void open() {
        JSONObject request = null;
        try {
            request = new JSONObject()
                            .put(NetworkFileServer.KEY_REQUEST, NetworkFileServer.REQUEST_OPEN_RANDOM_ACCESS)
                            .put(NetworkFileServer.KEY_PATH, file.getPath());
        } catch (JSONException e) {
            Log.debug.log(TAG, e);
        }
        client.sendRequest(request, (response) -> {
            int port = response.optInt(ServerThread.KEY_PORT);
            try {
                socket = new Socket(client.getAddress(),port); 
                input = new DataInputStream(socket.getInputStream());
                output = new DataOutputStream(socket.getOutputStream());      
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }
    
    private void checkSocketOpened() {
    	if(socket == null) {
            synchronized(lock) {
                try {
                    lock.wait();
                } catch (InterruptedException e) {
                    Log.err.log(TAG,e);
                }
            }
        }
    }
    
    public synchronized long length() throws IOException {
        if(length != -1) return length;
        
    	checkSocketOpened();
        
        try {
            JSONObject jsonObject = new JSONObject()
                .put(NetworkRandomAccessThread.KEY_REQUEST, NetworkRandomAccessThread.REQUEST_GET_LENGTH);
            output.writeUTF(jsonObject.toString());
            length = input.readLong();
        } catch (JSONException e) {
            Log.err.log(TAG,e);
        }
        return length;
    }
    
    public synchronized int read(byte[] buf, int off, int len) throws IOException {
    	checkSocketOpened();
        try {
            JSONObject jsonObject = new JSONObject()
                .put(NetworkRandomAccessThread.KEY_REQUEST, NetworkRandomAccessThread.REQUEST_READ)
                .put(NetworkRandomAccessThread.KEY_SIZE, len);
            output.writeUTF(jsonObject.toString());
            len = input.readInt();
            input.read(buf,off,len);
            return len;
        } catch (JSONException e) {
            Log.err.log(TAG,e);
        }
        return -1;
    }
    
    public synchronized void seek(long pos) throws IOException {
    	checkSocketOpened();
        try {
            JSONObject jsonObject = new JSONObject()
                .put(NetworkRandomAccessThread.KEY_REQUEST, NetworkRandomAccessThread.REQUEST_SEEKTO)
                .put(NetworkRandomAccessThread.KEY_POSITION, pos);
            output.writeUTF(jsonObject.toString());
        } catch (JSONException e) {
            Log.err.log(TAG,e);
        }
    }
    
    public synchronized void close() throws IOException{
        if(socket != null) 
            socket.close();
    }
}
