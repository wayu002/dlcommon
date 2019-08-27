package tv.tutulive.plus.dlcommon.tool;

import android.text.TextUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import tv.tutulive.plus.dlcommon.log.DLLog;

/**
 * Created by wangyu on 16/8/28.
 */
public class DLDir {
    private final static String TAG = "DLDir";

    /**
     * create directories if dir not exist
     * @param path
     */
    public static boolean createDirs(String path){
        File dir = new File(path);
        if(!dir.exists()){
            return dir.mkdirs();
        }
        return true;
    }

    /**
     * create directories if dir not exist
     * @param path
     */
    public static boolean createDirs(File dir){
        if(!dir.exists()){
            return dir.mkdirs();
        }
        return true;
    }

    /**
     * clear the files in the directory that match the regex
     * @param regex: regex of the file name
     *
     */
    public static void clearDir(String dir, String regex){
        try{
            File fdir = new File(dir);
            if(fdir.isDirectory()) {
                File[] files = fdir.listFiles();
                if (files != null) {
                    for (File file : files) {
                        if(file.isDirectory()){
                            clearDir(file.getAbsolutePath(), regex);
                        }else{
                            clearFile(file, regex);
                        }
                    }
                }
            }else{
                clearFile(fdir, regex);
            }
        }catch(Exception e){
            DLLog.e(TAG, e.getMessage());
        }
    }

    public static void clearFile(File f, String regex){
        try{
            if(regex != null){
                if(f.getName().matches(regex)){
                    f.delete();
                }
            }else{
                f.delete();
            }
        }catch (Exception e){
            DLLog.e(TAG, e.getMessage());
        }
    }

    public static void clear(String dir, String regex, long millionSecondsAgo){
        try{
            File fdir = new File(dir);
            File[] files = fdir.listFiles();
            if(files != null){
                for(File file: files){
                    if(regex != null){
                        if(file.getName().matches(regex)){
                            if(System.currentTimeMillis()-file.lastModified() > millionSecondsAgo){
                                file.delete();
                            }
                        }
                    }else{
                        if(System.currentTimeMillis()-file.lastModified() > millionSecondsAgo){
                            file.delete();
                        }
                    }
                }
            }
        }catch(Exception e){
            DLLog.e(TAG, e.getMessage());
        }
    }

    public static boolean exist(String dir){
        try{
            File fdir = new File(dir);
            if(fdir.exists() && fdir.isDirectory()){
                return true;
            }
            return false;
        }catch(Exception e){
            return false;
        }
    }

    /**
     * copy the source directory to destination directory
     * @param srcFile
     * @param destDir
     * @return
     *    true if copy success, otherwise return false;
     */
    public static void copy(File srcFile, File destDir, String fileName, boolean force){
        try{
            if(!srcFile.exists()){
                return; //source directory is not exist;
            }

            if(!destDir.exists()){
                DLDir.createDirs(destDir); //create destination directory if it does not exist
            }

            if(srcFile.isDirectory()){
				/*create new directory in destination directory*/
                File nextDestDir = new File(destDir.getAbsolutePath()+"/"+srcFile.getName());
                if(!nextDestDir.exists()){
                    nextDestDir.mkdirs();
                }

				/*copy each file*/
                File[] srcFiles = srcFile.listFiles();
                for(File nextFile: srcFiles){
                    DLDir.copy(nextFile, nextDestDir, null, force);
                }
            }else{
                File destFile = new File(destDir+"/"+ (TextUtils.isEmpty(fileName) ? srcFile.getName() : fileName));
                if(destFile.exists()){
                    if(force){
                        destFile.delete();
                    }else{
                        return;
                    }
                }

                InputStream is = null;
                OutputStream os = null;
                try{
                    is = new FileInputStream(srcFile);
                    os = new FileOutputStream(destFile);

                    int len = 0;
                    byte[] buf = new byte[16*1024];

                    while((len=is.read(buf)) > 0){
                        os.write(buf, 0, len);
                    }
                }catch(Exception e){
                }finally{
                    try{
                        if(is != null){
                            is.close();
                        }
                        if(os != null){
                            os.close();
                        }
                    }catch(Exception e1){}
                }
            }


        }catch(Exception e){}
    }

    public static long getFilesSizeInDir(String dir){
        long size = 0;
        try {
            File files = new File(dir);
            if(files.isDirectory()){
                File[] lsfiles = files.listFiles();
                for(File f : lsfiles){
                    if(f.isDirectory()){
                        size += getFilesSizeInDir(f.getAbsolutePath());
                    }else {
                        size += getFileSize(f);
                    }
                }
            }
        }catch (Exception e){
            DLLog.e(TAG, e.getMessage());
        }
        return size;
    }

    public static long getFileSize(File file){
        long size = 0;
        try{
            if (file.exists()) {
                FileInputStream fis;
                fis = new FileInputStream(file);
                size = fis.available();
                fis.close();
            }
        }catch(Exception e){
            DLLog.e(TAG, e.getMessage());
        }
        return size;
    }
}