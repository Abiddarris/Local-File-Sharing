package com.abiddarris.lanfileviewer.file.network;

public final class JSONRequest {
    
    public static final int REQUEST_GET_NAME = 1;
    public static final int REQUEST_LIST_FILES = 2;
    public static final int REQUEST_IS_DIRECTORY = 4;
    public static final int REQUEST_IS_FILE = 8;
    public static final int REQUEST_GET_PARENT_FILE = 16;
    public static final int REQUEST_GET_TOP_DIRECTORY_FILES = 32;
    public static final int REQUEST_GET_MIME_TYPE = 64;
    public static final int REQUEST_GET_LENGTH = 128;
    public static final int REQUEST_GET_LAST_MODIFIED = 256;
    public static final String KEY_ID = "id";
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
}
