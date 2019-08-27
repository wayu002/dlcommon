package tv.tutulive.plus.dlcommon.log;

import android.text.TextUtils;
import android.util.Log;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import tv.tutulive.plus.dlcommon.preference.DLPreference;
import tv.tutulive.plus.dlcommon.tool.DLDirMgr;

/**
 * 统一日志模块
 * Created by wangyu on 16/8/28.
 */
public class DLLog {
    private static boolean mDebug = true;

    public static boolean isDebug(){
        return mDebug;
    }

    public static void i(String tag, String msg){
        if(!mDebug){
            return;
        }
        if(TextUtils.isEmpty(msg)){
            msg = "";
        }
        Log.i(tag, msg);
    }

    public static void d(String tag, String msg){
        if(!mDebug){
            return;
        }
        if(TextUtils.isEmpty(msg)){
            msg = "";
        }
        Log.d(tag, msg);
    }

    public static void e(String tag, String msg){
        if(!mDebug){
            return;
        }
        if(TextUtils.isEmpty(msg)){
            msg = "";
        }
        Log.e(tag, msg);
    }

    public synchronized static void f(String tag, String msg){
        if(!mDebug){
            return;
        }
        File file = getFile(DLDirMgr.WorkDir.LOG, null, true);
        if(file == null){
            return;
        }
        writeContent(file, tag, msg);
    }

    private static File getFile(DLDirMgr.WorkDir dir, String prefix, boolean simpleDate){
        String rootPath = DLDirMgr.instance().getPath(dir);
        SimpleDateFormat sdf = null;
        if(simpleDate){
            sdf = new SimpleDateFormat("yyyy-MM-dd");
        }else {
            sdf = new SimpleDateFormat("yyyy-MM-dd-HH:mm:ss");
        }
        Date date = new Date();
        String logName = (TextUtils.isEmpty(prefix) ? "" : prefix) + sdf.format(date) + ".log";
        File file = new File(rootPath + "/" + logName);
        if(!file.exists()){
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }
        return file;
    }

    private static void writeContent(File file, String tag, String msg){
        try {
            DLLog.d(tag, msg);
            FileWriter writer = new FileWriter(file, true);
            SimpleDateFormat detail = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
            Date date = new Date();
            String content = detail.format(date) + " " + tag + ": " + msg;
            writer.write(content);
            writer.write("\r\n");
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public synchronized static void payLog(String tag, String msg){
        int report = DLPreference.instance().getInt(DLPreference.PrefID.PREF_REPORT_PAY_LOG);
        if(report == 0){
            return;
        }
        File file = getFile(DLDirMgr.WorkDir.PAY, "google_", true);
        if(file == null){
            return;
        }
        writeContent(file, tag, msg);
    }

    public synchronized static void socketLog(String tag, String liveid, String msg){
        File file = getFile(DLDirMgr.WorkDir.ACTION, liveid + "_", true);
        if(file == null){
            return;
        }
        writeContent(file, tag, msg);
    }
}
