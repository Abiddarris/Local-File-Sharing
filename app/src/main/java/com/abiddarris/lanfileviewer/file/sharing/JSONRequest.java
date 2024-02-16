package com.abiddarris.lanfileviewer.file.sharing;

import com.abiddarris.lanfileviewer.file.Requests;
import org.json.JSONArray;
import org.json.JSONObject;

public final class JSONRequest implements Requests {
    
    public static final String REQUEST_CONNECT = "requestConnect";
    public static final String REQUEST_GET_TOP_DIRECTORY_FILES = "requestGetTopDirectoryFiles";
    public static final String REQUEST_MAKE_DIRECTORIES = "requestMakeDirectories";
    public static final String REQUEST_COPY = "requestCopy";
    public static final String REQUEST_PROGRESS  = "requestProgress";
    public static final String REQUEST_CANCEL_PROGRESS = "requestCancelProgress";
    public static final String REQUEST_REMOVE_PROGRESS = "requestRemoveProgress";
    public static final String REQUEST_RENAME = "requestRename";
    public static final String REQUEST_DELETE = "requestDelete";
    public static final String REQUEST_MOVE = "requestMove";
    public static final String KEY_REQUEST = "request";
    public static final String KEY_PATH = "path";
    public static final String KEY_TOP_DIRECTORY_FILES = "topDirectoryFiles";
    public static final String KEY_MAKE_DIRECTORIES_SUCCESS = "makeDirectoriesSuccess";
    public static final String KEY_DEST = "dest";
    public static final String KEY_PROGRESS_ID = "progressId";
    public static final String KEY_COMPLETED = "completed";
    public static final String KEY_PROGRESS = "progress";
    public static final String KEY_EXCEPTION = "exception";
    public static final String KEY_NEW_NAME = "newName";
    public static final String KEY_SUCESS = "sucess";
    public static final String KEY_SERVER_ID = "serverId";
    public static final String KEY_CLIENT_ID = "clientId";
    public static final String KEY_CLIENT_NAME = "clientName";
    public static final String KEY_SESSION = "session";
    
    public static JSONArray createRequest(String... requestKeys) {
        JSONArray requests = new JSONArray();
        for(String requestKey : requestKeys) {
        	requests.put(requestKey);
        }
        return requests;
    }
    
}
