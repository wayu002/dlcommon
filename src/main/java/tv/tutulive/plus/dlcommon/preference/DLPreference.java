package tv.tutulive.plus.dlcommon.preference;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Set;

import tv.tutulive.plus.dlcommon.log.DLLog;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.READ_PHONE_STATE;
import static android.Manifest.permission.RECORD_AUDIO;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

/**
 * Preference存储
 * Created by wangyu on 16/8/28.
 */
public class DLPreference {
    private static final String TAG = "DLPreference";
    private static DLPreference _instance = null;
    private static String PREF_DEELIVE = "deelive";
    private SharedPreferences sp = null;

    private DLPreference() {

    }

    public static DLPreference instance() {
        if (_instance == null) {
            synchronized (DLPreference.class) {
                if (_instance == null) {
                    _instance = new DLPreference();
                }
            }
        }
        return _instance;
    }

    public void init(Context context) {
        sp = context.getSharedPreferences(PREF_DEELIVE, Context.MODE_PRIVATE);
        putString(PrefID.PREF_PACKAGE_NAME, context.getPackageName());
    }

    /*******************
     * get value
     *******************************************/
    public int getInt(PrefID id) {
        try {
            return sp.getInt(id.getKey(), Integer.parseInt(id.getDefaultValue()));
        } catch (Exception e) {
            DLLog.e(TAG, e.getMessage());
        }
        return Integer.parseInt(id.getDefaultValue());
    }

    public long getLong(PrefID id) {
        try {
            return sp.getLong(id.getKey(), Long.parseLong(id.getDefaultValue()));
        } catch (Exception e) {
            DLLog.e(TAG, e.getMessage());
        }
        return Long.parseLong(id.getDefaultValue());
    }

    public float getFloat(PrefID id) {
        try {
            return sp.getFloat(id.getKey(), Float.parseFloat(id.getDefaultValue()));
        } catch (Exception e) {
            DLLog.e(TAG, e.getMessage());
        }
        return Float.parseFloat(id.getDefaultValue());
    }

    public String getString(PrefID id) {
        try {
            return sp.getString(id.getKey(), id.getDefaultValue());
        } catch (Exception e) {
            DLLog.e(TAG, e.getMessage());
        }
        return id.getDefaultValue();
    }

    public boolean getBoolean(PrefID id) {
        try {
            return sp.getBoolean(id.getKey(), Boolean.parseBoolean(id.getDefaultValue()));
        } catch (Exception e) {
            DLLog.e(TAG, e.getMessage());
        }
        return Boolean.parseBoolean(id.getDefaultValue());
    }

    public Set<String> getStringSet(PrefID id) {
        try {
            return sp.getStringSet(id.getKey(), null);
        } catch (Exception e) {
            DLLog.e(TAG, e.getMessage());
        }
        return null;
    }
    /*************************************************************************/

    /**********************
     * put value
     ****************************************/
    public void putInt(PrefID id, int value) {
        try {
            SharedPreferences.Editor editor = sp.edit();
            editor.putInt(id.getKey(), value);
            editor.apply();
        } catch (Exception e) {
            DLLog.e(TAG, e.getMessage());
        }
    }

    public void putLong(PrefID id, long value) {
        try {
            SharedPreferences.Editor editor = sp.edit();
            editor.putLong(id.getKey(), value);
            editor.apply();
        } catch (Exception e) {
            DLLog.e(TAG, e.getMessage());
        }
    }

    public void putFloat(PrefID id, float value) {
        try {
            SharedPreferences.Editor editor = sp.edit();
            editor.putFloat(id.getKey(), value);
            editor.apply();
        } catch (Exception e) {
            DLLog.e(TAG, e.getMessage());
        }
    }

    public void putString(PrefID id, String value) {
        try {
            SharedPreferences.Editor editor = sp.edit();
            editor.putString(id.getKey(), value);
            editor.apply();
        } catch (Exception e) {
            DLLog.e(TAG, e.getMessage());
        }
    }

    public void putBoolean(PrefID id, boolean value) {
        try {
            SharedPreferences.Editor editor = sp.edit();
            editor.putBoolean(id.getKey(), value);
            editor.apply();
        } catch (Exception e) {
            DLLog.e(TAG, e.getMessage());
        }
    }

    public void putStringSet(PrefID id, Set<String> set) {
        try {
            SharedPreferences.Editor editor = sp.edit();
            editor.putStringSet(id.getKey(), set);
            editor.apply();
        } catch (Exception e) {
            DLLog.d(TAG, e.getMessage());
        }
    }

    /*************************************************************************/

    public enum PrefID {
        PREF_WORK_DIR("pref_work_dir", ""),
        PREF_LEVEL_THRESHOLD("pref_level_threshold", "10"),
        PREF_HEART_BEAT("pref_heart_beat", "30"),
        PREF_SERVER_VESRSION("pref_server_version", ""),
        PREF_UPDATE("pref_update", "0"),
        PREF_MUSIC("pref_music", "false"),
        PREF_HEART_BEAT_NUM("pref_heart_beat_num", "4"),
        PREF_PUSH_STREAM_TIMEOUT("pref_push_stream_timeout", "60"),
        PREF_INCOME("pref_income", "1"),
        PREF_WITHDRAW("pref_withdraw", "1"),
        PREF_CONVERT("pref_convert", "1"),
        PREF_PUSH_STREAM_EXIT("pref_stream_exit", "0"),
        PREF_PUSH_TOKEN("pref_push_token", ""),
        PREF_REPORT_STREAM_QUALITY("pref_report_stream_quality", "0"),
        PREF_ADJUST_BITRATE("pref_adjust_bitrate", "0"),
        PREF_SHARE_TIME_INTERVAL("pref_share_time_interval", "120"),
        PREF_REPORT_PAY_LOG("pref_pay_log", "0"),
        PREF_UPDATE_VERSION("pref_update_version", "0"),
        PREF_LAST_UPDATE_TIME("pref_last_update_time", "0"),
        PREF_GUIDE_ROOM("pref_guide_room", "0"),
        PREF_GUIDE_COMMENT("pref_guide_comment", "0"),
        PREF_GUIDE_GIFT("pref_guide_gift", "0"),
        PREF_GUIDE_FOLLOW("pref_guide_follow", "0"),
        PREF_GUIDE_LETTER("pref_guide_letter", "0"),
        PREF_GUIDE_REC("pref_guide_rec","0"),
        PREF_GUIDE_VIDEO_FOLLOW("pref_guide_video_follow", "0"),
        PREF_GUIDE_VIDEO_SWIPE("pref_guide_video_swipe", "0"),
        PREF_GUIDE_VIDEO_COMMENT("pref_guide_video_comment", "0"),
        PREF_GUIDE_VIDEO_DELETE("pref_guide_video_delete", "0"),
        PREF_GUIDE_VIDEO_REPORT("pref_guide_video_report", "0"),
        PREF_GUIDE_STICKER("pref_guide_sticker", "0"),
        PREF_GUIDE_TRANSLATE("pref_guide_translate", "0"),
        PREF_GUIDE_USER_LETTER("pref_guide_user_letter", "0"),
        PREF_GUIDE_ROOM_GAME("pref_guide_room_game", "0"),
        PREF_GUIDE_USER_GAME("pref_guide_user_game", "0"),
        PREF_GPS_DIALOG_TIME_STAMP("pref_gps_dialog_time_stamp", "0"),
        PREF_DNS_LOG("pref_dns_log", "0"),
        PREF_IS_POLICE("pref_is_police", "0"),
        PREF_VER_CONFIG_BANNER("pref_ver_config_banner", "0"),
        PREF_VER_CONFIG_INCOME("pref_ver_config_income", "0"),
        PREF_VER_CONFIG_EXCHANGE("pref_ver_config_exchange", "0"),
        PREF_VER_CONFIG_WITHDRAW("pref_ver_config_withdraw", "0"),
        PREF_VER_CONFIG_ACTIVITY("pref_ver_config_activity", "0"),
        PREF_VER_CONFIG_HOST_ACTIVITY("pref_ver_config_host_activity", "0"),
        PREF_VER_CONFIG_MILLION("pref_ver_config_million", "0"),
        PREF_VER_CONFIG_GAME("pref_ver_config_game","0"),
        PREF_CHECK_TIME_STAMP("pref_check_in_time_stamp", "0"),
        PREF_LONGITUDE("pref_longitude", ""),
        PREF_LATITUDE("pref_latitude", ""),
        PREF_COUNTRY_CODE("pref_country_code", ""),
        PREF_REPORT_SOCKET_LOG("pref_report_socket_log", "0"),
        PERF_LOCAL_ADDRESS("pref_local_address", ""),
        PREF_FB_INVITE_RECEIVE("pref_fb_invite_receive", "0"),
        PREF_PERMISSION_CAMERA(CAMERA, "true"),
        PREF_PERMISSION_AUDIO(RECORD_AUDIO, "true"),
        PREF_PERMISSION_LOCATION(ACCESS_FINE_LOCATION, "true"),
        PREF_PERMISSION_PHONE(READ_PHONE_STATE, "true"),
        PREF_PERMISSION_STORAGE(WRITE_EXTERNAL_STORAGE, "true"),
        PREF_LAST_PHONE_NUM("pref_last_phone_num", ""),
        PREF_STARTUP_IMAGE("pref_startup_image", ""),
        PREF_STARTUP_COUNTDOWN("pref_startup_countdown", "3"),
        PREF_STARTUP_URL("pref_startup_url", ""),
        PREF_STARTUP_TITLE("pref_startup_title", ""),
        PREF_STARTUP_ACTION("pref_startup_action", "web"),
        PREF_STARTUP_LOGINID("pref_startup_loginid", "web"),
        PREF_DISTRIBUTE_CHANNEL("pref_distribute_channel", ""),
        PREF_APP_VERSION("pref_app_version", ""),
        PREF_AGREE_PROTOCOL("pre_agree_protocol", "false"),
        PREF_PACKAGE_NAME("pref_package_name", "");

        private String key;
        private String defaultValue;

        PrefID(String key, String value) {
            this.key = key;
            this.defaultValue = value;
        }

        public String getKey() {
            return this.key;
        }

        public String getDefaultValue() {
            return this.defaultValue;
        }
    }
}
