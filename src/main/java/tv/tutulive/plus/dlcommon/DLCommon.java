package tv.tutulive.plus.dlcommon;

import android.content.Context;

import tv.tutulive.plus.dlcommon.preference.DLPreference;
import tv.tutulive.plus.dlcommon.tool.DLDirMgr;

/**
 * 公共模块
 * Created by wangyu on 16/8/28.
 */
public class DLCommon {
    private static final String TAG = "DLCommon";
    private static DLCommon _instance = null;

    public static DLCommon instance() {
        if (_instance == null) {
            synchronized (DLCommon.class) {
                if (_instance == null) {
                    _instance = new DLCommon();
                }
            }
        }
        return _instance;
    }

    public void init(Context context){
        DLPreference.instance().init(context);
        DLDirMgr.instance().init(context);
    }
}
