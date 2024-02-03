package com.abiddarris.lanfileviewer.file;

import static com.abiddarris.lanfileviewer.file.Requests.*;

import android.content.Context;
import com.abiddarris.lanfileviewer.R;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

public class Files {
    
    private static final SimpleDateFormat anotherYear = new SimpleDateFormat("dd LLL YYYY HH.mm");
    private static final SimpleDateFormat currentYear = new SimpleDateFormat("dd LLL HH.mm");
    
    public static void getFilesTree(List<File> files, File parent) {
        parent.updateDataSync(REQUEST_GET_FILES_TREE);
        files.addAll(parent.getFilesTree());
    }
    
    public static long getFilesTreeSize(File[] items) {
    	long size = 0;
        
        for(File file : items) {
            file.updateData(REQUEST_GET_FILES_TREE_SIZE)
                .get();
                
        	size += file.getFilesTreeSize();
        }
        
        return size;
    }
    
    public static int getFilesCount(List<File> files) {
    	int filesCount = 0;
        for(File file : files) {
            file.updateData(REQUEST_IS_FILE)
                .get();
            
    		if(file.isFile()) filesCount++;
    	}
        return filesCount;
    }
    
    public static int getDirectoriesCount(List<File> files) {
    	int foldersCount = 0;
        for(File file : files) {
            file.updateData(REQUEST_IS_DIRECTORY)
                .get();
    		if(file.isDirectory()) foldersCount++;
    	}
        return foldersCount;
    }
    
    public static int getFilesCount(File[] files) {
    	int filesCount = 0;
        for(File file : files) {
            file.updateData(REQUEST_IS_FILE)
                .get();
    		if(file.isFile()) filesCount++;
    	}
        return filesCount;
    }
    
    public static int getDirectoriesCount(File[] files) {
    	int foldersCount = 0;
        for(File file : files) {
            file.updateData(REQUEST_IS_DIRECTORY)
                .get();
    		if(file.isDirectory()) foldersCount++;
    	}
        return foldersCount;
    }
    
    public static String getNameWithoutExtension(File file) {
    	String fileName = file.getName();
        int extensionSeparator = fileName.lastIndexOf(".");
        
        if(extensionSeparator <= 0) return fileName;
        
        return fileName.substring(0, extensionSeparator);
    }
    
    public static String getExtension(File file) {
    	String fileName = file.getName();
        int extensionSeparator = fileName.lastIndexOf(".");
        
        if(extensionSeparator <= 0) return "";
        
        return fileName.substring(extensionSeparator + 1);
    }
    
    public static String formatToDate(long time) {
    	Calendar fileTime = Calendar.getInstance();
        int currentYear = fileTime.get(Calendar.YEAR);
        
        fileTime.setTimeInMillis(time);
        int fileYear = fileTime.get(Calendar.YEAR);
        
        if(fileYear == currentYear) {
            return Files.currentYear.format(fileTime.getTime());
        }
        
        return anotherYear.format(fileTime.getTime());
    }
    
    public static String formatSize(long length) {
        double kbLength = length / 1024;
        if(kbLength < 0.9) {
        	return Math.round(length) + " B";
        }
        
        if(kbLength < 100) {
            return formatSize(kbLength) + " KB";
        }
        
        double mbLength = kbLength / 1024;
        if(mbLength < 0.9) {
            return Math.round(kbLength) + " KB";
        }
        
        if(mbLength < 100) {
            return formatSize(mbLength) + " MB";
        }
        
        double gbLength = mbLength / 1024;
        if(gbLength < 0.9) {
            return Math.round(mbLength) + " MB";
        }
        
        if(gbLength < 100) {
            return formatSize(gbLength) + " GB";
        }
        
        return Math.round(gbLength) + " GB";
    }
    
    public static String formatFromItems(Context context, File[] items) {
        int fileCount = Files.getFilesCount(items);
        int folderCount = Files.getDirectoriesCount(items);
        
        int messageId;
        if(folderCount != 0 && fileCount != 0) {
            messageId = R.string.item_format;
        } else if(folderCount != 0) {
            messageId = folderCount == 1 ? R.string.one_folder_format : R.string.plural_folder_format;
        } else {
            messageId = fileCount == 1 ? R.string.one_file_format : R.string.plural_files_format;
        }
        
        String message = context.getString(messageId);
        
        return String.format(message, items.length);
    }
    
    private static String formatSize(final double length) {
        return String.format("%.2f", length);
    }
}
