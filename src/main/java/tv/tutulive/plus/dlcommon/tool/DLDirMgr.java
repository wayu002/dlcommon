package tv.tutulive.plus.dlcommon.tool;

import android.content.Context;
import android.text.TextUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import tv.tutulive.plus.dlcommon.log.DLLog;
import tv.tutulive.plus.dlcommon.preference.DLPreference;

/**
 * 目录操作管理
 * Created by wangyu on 16/8/28.
 */
public class DLDirMgr {
    private static final String TAG = "DLDirMgr";
    private final String ROOT_DIR = "/tutulive";
    private static DLDirMgr _instance;
    private String workDir = null;

    public static DLDirMgr instance() {
        if (_instance == null) {
            synchronized (DLDirMgr.class) {
                if (_instance == null) {
                    _instance = new DLDirMgr();
                }
            }
        }
        return _instance;
    }

    private DLDirMgr(){

    }

    public boolean init(Context ctx){
        DLLog.d(TAG, String.format("DLDirMgr init path:%s", this.ROOT_DIR));
        initWorkDir(ctx);
        createWorkDirs();
        return true;
    }

    /**
     * get the relate directory path
     * @param type: directory type
     * @return
     * request directory path of relate type, or ""
     */
    public String getPath(WorkDir type){
        String path = "";
        if(this.workDir != null){
            boolean flag = DLDir.createDirs(this.workDir);
            if(!flag){
                DLLog.e(TAG, "create work directory: "+this.workDir+" failed!");
            }

            path = this.workDir + type.getPath();
            flag = DLDir.createDirs(path);

            if(!flag){
                DLLog.e(TAG, "create directory: "+path+" failed!");
            }
        }

        return path;
    }

    private boolean initWorkDir(Context context){
        this.workDir = getWorkDir();
		/*try to get the work directory from shared preferences*/
        if(TextUtils.isEmpty(workDir) || !DLDevice.FileSystem.isWritable(this.workDir)){
			/*select new work root directory if saved path not available*/
            String workRootDir = selectWorkDir(context);
			/*save the root path to shared preference*/
            if(workRootDir != null){
                this.workDir = workRootDir+this.ROOT_DIR;
                this.saveWorkDir(this.workDir);
                DLLog.i("sdcard", "init with new selected work dir: " + this.workDir);
            }
            else{
                DLLog.i("sdcard", "fatal: init with new selected work dir failed");
                return false;
            }
        }else{
            DLLog.i("sdcard", "init with last selected work dir: "+this.workDir);
        }

        return true;
    }

    /**
     * select root path used for funshion root
     * @param context: current context
     * @return
     * path selected or null
     */
    private String selectWorkDir(Context context){
        final long MIN_SPACE_LIMIT = 10*1024*1024; // 1MB

		/*use external storage directory default*/
        String dir = DLDevice.FileSystem.getExternalStorageDir();
        if(dir != null && DLDevice.FileSystem.isWritable(dir) && DLDevice.FileSystem.getAvailableSize(dir) > MIN_SPACE_LIMIT){
            return dir;
        }

		/*if default external storage is not useable, use other volumes*/
        DLDevice.FileSystem.Volume[] volumes = DLDevice.FileSystem.getValidVolumes(context);
        if(volumes != null){
			/*use the max available size volume*/
            DLDevice.FileSystem.Volume selected = null;
            for(DLDevice.FileSystem.Volume volume: volumes){
                if (DLDevice.FileSystem.isWritable(volume.getPath())) {
                    if(selected == null){
                        selected = volume;
                        continue;
                    }

                    if(volume.getState().getAvailable() > selected.getState().getAvailable()){
                        selected = volume;
                    }
                }
            }

            if(selected != null){
                return selected.getPath();
            }
        }

        return DLDevice.FileSystem.getAppFilesDir(context);
    }

    /**
     * get the work directory from context shared preferences
     * @return
     * root path, or null
     */
    private String getWorkDir(){
        return DLPreference.instance().getString(DLPreference.PrefID.PREF_WORK_DIR);
    }

    /**
     * save the root path to context shared preferences
     * @param path: root path to save
     */
    private void saveWorkDir(String path){
        DLPreference.instance().putString(DLPreference.PrefID.PREF_WORK_DIR, path);
    }

    public List<DLDevice.FileSystem.Volume> getValidVolumes(Context context){
		/*valid storage volumes*/
        List<DLDevice.FileSystem.Volume> validVolumes = new ArrayList<DLDevice.FileSystem.Volume>();

		/*try to select the other volumes*/
        DLDevice.FileSystem.Volume[] volumes = DLDevice.FileSystem.getValidVolumes(context);
        int sdIndex = 1;
        if(volumes != null){
            for(DLDevice.FileSystem.Volume volume: volumes){
                if(volume.isExternal()){
                    volume.setName("SD卡" + sdIndex++);
                }else{
                    volume.setName("手机存储");
                }
                validVolumes.add(volume);
            }
        }

        return validVolumes;
    }

    public void removeDirFiles(String dirName, boolean removeDir){
        File dir = new File(dirName);
        if(!dir.isDirectory()){
            return;
        }
        File[] files = dir.listFiles();
        if(files != null && files.length > 0){
            for (File file : files){
                file.delete();
            }
        }
        if(removeDir){
            dir.delete();
        }
    }

    public void removeFile(String filepath){
        File file = new File(filepath);
        if(file.isFile() && file.exists()){
            file.delete();
        }
    }

    public boolean containFile(String dirPath, String fileName, boolean exact){
        if(TextUtils.isEmpty(dirPath) || TextUtils.isEmpty(fileName)){
            return false;
        }
        try{
            File dir = new File(dirPath);
            if(!dir.isDirectory()){
                return false;
            }
            File[] files = dir.listFiles();
            if(files == null){
                return false;
            }
            for (File f : files){
                if(exact){
                    String nameWithExtension = f.getAbsolutePath().substring(f.getAbsolutePath().lastIndexOf(File.separatorChar));
                    if(TextUtils.isEmpty(nameWithExtension)){
                        continue;
                    }
                    if(nameWithExtension.equals(fileName)){
                       return true;
                    }
                }else{
                    if(f.getName().contains(fileName)){
                        return true;
                    }
                }
            }
        }catch (Exception e){
            return false;
        }
        return false;
    }

    public String getMatchFilePath(String dirPath, String fileName, boolean exact){
        if(TextUtils.isEmpty(dirPath) || TextUtils.isEmpty(fileName)){
            return null;
        }
        try{
            File dir = new File(dirPath);
            if(!dir.isDirectory()){
                return null;
            }
            File[] files = dir.listFiles();
            if(files == null){
                return null;
            }
            for (File f : files){
                if(exact){
                    String nameWithExtension = f.getAbsolutePath().substring(f.getAbsolutePath().lastIndexOf(File.separatorChar));
                    if(TextUtils.isEmpty(nameWithExtension)){
                        continue;
                    }
                    if(nameWithExtension.equals(fileName)){
                        return f.getAbsolutePath();
                    }
                }else{
                    if(f.getName().contains(fileName)){
                        return f.getAbsolutePath();
                    }
                }
            }
        }catch (Exception e){
            return null;
        }
        return null;
    }

    /**
     * create funshion directory under the funshion root path
     */
    private boolean createWorkDirs(){
        if(this.workDir == null)
            return false;

        DLDir.createDirs(this.workDir);

        for(WorkDir dir: WorkDir.values()){
            String path = this.workDir+dir.getPath();
            DLDir.createDirs(path);
        }

        return true;
    }


    /**
     * directory types that application used
     */
    public enum WorkDir{
        ROOT(""),

        CACHE("/cache"),
        CACHE_HTTP("/cache/http"),
        CACHE_IMG("/cache/image"),
        CACHE_SOCKET("/cache/socket"),
        CACHE_GIFT("/gift"),

        EMOTICON("/emoticon"),

        AD("/ad"),
        AD_IMG("/ad/img"),

        DUMP_APP("/dump/app"),

        LOG("/log"),
        PAY("/log/pay"),
        ACTION("/log/action"),

        CONFIG("/config"),

        UPDATE("/update"),

        COVER_IMG("/cover/image"),

        REC_MEDIA("/rec"),

        SCREEN_SHOT("/screen_shot"),

        VIDEO_PRE("/video/pre"),
        VIDEO_COVER("/video/cover"),
        VIDEO("/video");

        private String path;

        WorkDir(String path){
            this.path = path;
        }

        public String getPath(){
            return this.path;
        }
    }
}
