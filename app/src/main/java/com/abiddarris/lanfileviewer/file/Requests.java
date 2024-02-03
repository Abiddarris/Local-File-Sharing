package com.abiddarris.lanfileviewer.file;

import java.util.HashMap;
import java.util.Map;

public interface Requests {
    
    static Map<String,String> table = new HashMap<>();
    
    public static final String REQUEST_LIST = "requestList";
    public static final String REQUEST_IS_DIRECTORY = "requestIsDirectory";
    public static final String REQUEST_IS_FILE = "requestIsFile";
    public static final String REQUEST_GET_MIME_TYPE = "requestGetMimeType";
    public static final String REQUEST_GET_LENGTH = "requestGetLength";
    public static final String REQUEST_GET_LAST_MODIFIED = "requestGetLastModified";
    public static final String REQUEST_EXISTS = "requestExists";
    public static final String REQUEST_ABSOLUTE_PATH = "requestAbsolutePath";
    public static final String REQUEST_GET_FILES_TREE = "requestGetFilesTree";
    public static final String REQUEST_GET_FILES_TREE_SIZE = "requestGetFilesTreeSize";
    public static final String KEY_IS_DIRECTORY = "isDirectory";
    public static final String KEY_IS_FILE = "isFile";
    public static final String KEY_MIME_TYPE = "mimeType";
    public static final String KEY_LIST = "list";
    public static final String KEY_LENGTH = "length";
    public static final String KEY_LAST_MODIFIED = "lastModified";
    public static final String KEY_EXISTS = "exists";
    public static final String KEY_ABSOLUTE_PATH = "absolutePath";
    public static final String KEY_FILES_TREE = "filesTree";
    public static final String KEY_FILES_TREE_SIZE = "filesTreeSize";
    
    public static String requestToKey(String request) {
        if(table.isEmpty()) {
            table.put(REQUEST_ABSOLUTE_PATH, KEY_ABSOLUTE_PATH);
            table.put(REQUEST_EXISTS, KEY_EXISTS);
            table.put(REQUEST_GET_FILES_TREE, KEY_FILES_TREE);
            table.put(REQUEST_GET_FILES_TREE_SIZE, KEY_FILES_TREE_SIZE);
            table.put(REQUEST_GET_LAST_MODIFIED, KEY_LAST_MODIFIED);
            table.put(REQUEST_GET_LENGTH, KEY_LENGTH);
            table.put(REQUEST_GET_MIME_TYPE, KEY_MIME_TYPE);
            table.put(REQUEST_IS_DIRECTORY, KEY_IS_DIRECTORY);
            table.put(REQUEST_IS_FILE, KEY_IS_FILE);
            table.put(REQUEST_LIST, KEY_LIST);
        }
        return table.get(request);
    }
}
