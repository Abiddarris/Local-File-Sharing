package com.abiddarris.lanfileviewer.file.network;
import org.json.JSONArray;
import org.json.JSONObject;

public final class JSONRequest {
    
    public static final String REQUEST_GET_NAME = "requestGetName";
    public static final String REQUEST_LIST_FILES = "requestListFiles";
    public static final String REQUEST_IS_DIRECTORY = "requestIsDirectory";
    public static final String REQUEST_IS_FILE = "requestIsFile";
    public static final String REQUEST_GET_PARENT_FILE = "requestGetParentFile";
    public static final String REQUEST_GET_TOP_DIRECTORY_FILES = "requestGetTopDirectoryFiles";
    public static final String REQUEST_GET_MIME_TYPE = "requestGetMimeType";
    public static final String REQUEST_GET_LENGTH = "requestGetLength";
    public static final String REQUEST_GET_LAST_MODIFIED = "requestGetLastModified";
    public static final String REQUEST_MAKE_DIRECTORIES = "requestMakeDirectories";
    public static final String KEY_IS_DIRECTORY = "isDirectory";
    public static final String KEY_IS_FILE = "isFile";
    public static final String KEY_GET_PARENT_FILE = "getParentFile";
    public static final String KEY_REQUEST = "request";
    public static final String KEY_PATH = "path";
    public static final String KEY_MIME_TYPE = "mimeType";
    public static final String KEY_NAME = "name";
    public static final String KEY_LIST_FILES = "listFiles";
    public static final String KEY_TOP_DIRECTORY_FILES = "topDirectoryFiles";
    public static final String KEY_LENGTH = "length";
    public static final String KEY_LAST_MODIFIED = "lastModified";
    public static final String KEY_MAKE_DIRECTORIES_SUCCESS = "makeDirectoriesSuccess";
    
    public static JSONArray createRequest(String... requestKeys) {
        JSONArray requests = new JSONArray();
        for(String requestKey : requestKeys) {
        	requests.put(requestKey);
        }
        return requests;
    }
    
}
