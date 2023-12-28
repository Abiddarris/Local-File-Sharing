package com.abiddarris.lanfileviewer.file;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

public class Files {
    
    private static final SimpleDateFormat anotherYear = new SimpleDateFormat("dd LLL YYYY HH.mm");
    private static final SimpleDateFormat currentYear = new SimpleDateFormat("dd LLL HH.mm");
    
    public static void getFilesTree(List<File> files, File parent) {
        files.add(parent);
        parent.updateDataSync();
        if(parent.isFile()) {
            return;
        }
        
        File[] children = parent.listFiles();
        if(children == null) return;
        
        for(File file : children) {
            getFilesTree(files,file);
        }
    }
    
    public static long getFilesTreeSize(List<File> files) {
    	long size = 0;
        
        for(File file : files) {
        	if(file.isFile()) {
                size += file.length();
            }
        }
        
        return size;
    }
    
    public static int getFilesCount(List<File> files) {
    	int filesCount = 0;
        for(File file : files) {
    		if(file.isFile()) filesCount++;
    	}
        return filesCount;
    }
    
    public static int getDirectoriesCount(List<File> files) {
    	int foldersCount = 0;
        for(File file : files) {
    		if(file.isDirectory()) foldersCount++;
    	}
        return foldersCount;
    }
    
    public static int getFilesCount(File[] files) {
    	int filesCount = 0;
        for(File file : files) {
    		if(file.isFile()) filesCount++;
    	}
        return filesCount;
    }
    
    public static int getDirectoriesCount(File[] files) {
    	int foldersCount = 0;
        for(File file : files) {
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
    
    private static String formatSize(final double length) {
        return String.format("%.2f", length);
    }
}
