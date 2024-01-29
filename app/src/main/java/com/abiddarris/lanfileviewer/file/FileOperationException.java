package com.abiddarris.lanfileviewer.file;

public class FileOperationException extends RuntimeException {
    
    public FileOperationException() {}
    
    public FileOperationException(String message) {
        super(message);
    }
    
    public FileOperationException(Throwable cause) {
        super(cause);
    }
    
}
