package tv.tutulive.plus.dlcommon.tool;

import android.annotation.SuppressLint;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Environment;
import android.os.StatFs;
import android.provider.Settings;
import android.support.v4.content.ContextCompat;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import tv.tutulive.plus.dlcommon.log.DLLog;

/**
 * 设备操作工具类
 * Created by wangyu on 16/8/28.
 */
public class DLDevice {
    /**
     * 判断GPS是否开启，GPS或者AGPS开启一个就认为是开启的
     *
     * @param context
     * @return true 表示开启
     */
    public static boolean isGPSOpen(Context context) {
        LocationManager locationManager
                = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        boolean gps = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        boolean network = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        return gps || network;
    }

    public final static class Dev {
        /**
         * get the telephone device id, which is the IMEI for GSM and the MEID
         * or ESN for CDMA phones
         *
         * @param context : current context
         * @return device id, or null
         */
        public static String getDeviceID(Context context) {
            TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            try {
                if (tm != null) {
                    if(!TextUtils.isEmpty(tm.getDeviceId())){
                        return tm.getDeviceId();
                    }
                    return Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
                }
            } catch (Exception e) {
                DLLog.e("DEVICE", e.getMessage());
                return Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
            }
            return null;
        }

        public static String getPhoneNum(Context context){
            TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            try {
                if (tm != null) {
                    return tm.getLine1Number();
                }
            } catch (Exception e) {
                DLLog.e("DEVICE", e.getMessage());
            }
            return null;
        }

        public static String getSimCountryCode(Context context) {
            TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            if (TextUtils.isEmpty(tm.getSimCountryIso())) {
                //return context.getResources().getConfiguration().locale.getCountry();
                return Locale.getDefault().getCountry();
            }
            return tm.getSimCountryIso().toUpperCase();
        }

        /**
         * get the serial number of device
         *
         * @return serial number or null
         */
        public static String getSerialNum() {
            try {
                Class<?> c = Class.forName("android.os.SystemProperties");
                Method get = c.getMethod("get", String.class, String.class);
                String serialNum = (String) (get.invoke(c, "ro.serialno", "unknown"));
                return serialNum;
            } catch (Exception e) {
                return null;
            }
        }

        /**
         * get the memory information, size in MB
         *
         * @return memory information, size in MB
         */
        public static MemInfo getMemInfo() {
            FileInputStream fis = null;
            try {
                /* the memory information file path under linux os */
                String mempath = "/proc/meminfo";

				/* read data in the memory information file */
                fis = new FileInputStream(mempath);

                byte[] buffer = new byte[4096];
                ByteArrayOutputStream baos = new ByteArrayOutputStream();

                int sz = fis.read(buffer);
                while (sz != -1) {
                    baos.write(buffer, 0, sz);
                    sz = fis.read(buffer);
                }

                fis.close();
                fis = null;

				/* parse the total memory and left memory */
                String strMemInfo = baos.toString();
                HashMap<String, Long> infos = new HashMap<String, Long>();

                Pattern p = Pattern.compile("(\\w+):\\s*(\\d+)");
                Matcher m = p.matcher(strMemInfo);
                while (m.find()) {
                    infos.put(m.group(1), Long.valueOf(m.group(2)));
                }

                if (infos.containsKey("MemTotal") && infos.containsKey("MemFree") && infos.containsKey("Buffers") && infos.containsKey("Cached"))
                    return new MemInfo(infos.get("MemTotal").longValue() / 1024, infos.get("MemFree").longValue() / 1024, infos.get("Buffers").longValue(), infos.get("Cached").longValue());

                return null;
            } catch (Exception e) {
                if (fis != null) {
                    try {
                        fis.close();
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                }
                return null;
            }
        }

        /**
         * judge whether it's locked screen status
         *
         * @param context
         * @return true indicates that it's locked screen; otherwise is false
         */
        public static boolean isLockScreen(Context context) {
            if (null == context) {
                return true;
            }

            try {
                KeyguardManager mkeyguardManager = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
                return (null != mkeyguardManager && mkeyguardManager.inKeyguardRestrictedInputMode());
            } catch (Exception e) {
                DLLog.i("FSDevice", "isLockScreen() " + e.getMessage());
            }
            return true;
        }
    }

    /**
     * pack the os utility methods of device
     */
    public final static class OS {

        /**
         * get the android os version
         *
         * @return android os version
         */
        public static String getVersion() {
            return android.os.Build.VERSION.RELEASE;
        }

        /**
         * get brand of the android device
         *
         * @return brand
         */
        public static String getBrand() {
            return android.os.Build.BRAND;
        }

        /**
         * get model of the android device
         *
         * @return model
         */
        public static String getModel() {
            return android.os.Build.MODEL;
        }

        /**
         * get the android id, which is A 64-bit number (as a hex string) that
         * is randomly generated on the device's first boot and should remain
         * constant for the lifetime of the device. (The value may change if a
         * factory reset is performed on the device)
         *
         * @return android id of the device,
         */
        public static String getAndroidID(Context context) {
            return Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
        }
    }

    /**
     * pack the wifi utility methods of device
     */
    public final static class Wifi {
        /**
         * get the wifi information object
         *
         * @param context : current context
         * @return wifi information object, or null
         */
        public static WifiInfo getWifiInfo(Context context) {
            try {
                WifiManager wm = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
                WifiInfo wi = wm.getConnectionInfo();
                if (wi != null)
                    return wi;
            } catch (Exception e) {
            }
            return null;
        }

        /**
         * get the mac address of device in format: AABBCCDDEEFF
         *
         * @param context : current context
         * @return mac address, or null
         */
        public static String getMacAddress(Context context) {
            try {
                String strMac = getMacAddress1(context);
                if (strMac != null) {
                    return strMac.replace(":", "").toLowerCase(Locale.getDefault());
                }
            } catch (Exception e) {
            }
            return "000000000000";
        }

        /**
         * get the mac address of device in format: xx:xx:xx:xx:xx:xx
         *
         * @param context : current context
         * @return mac address, or null
         */
        public static String getMacAddress1(Context context) {
            try {
                WifiInfo wi = getWifiInfo(context);
                if (wi != null) {
                    String mac = wi.getMacAddress();
                    if (mac != null) {
                        return mac.toLowerCase(Locale.getDefault());
                    }
                }
            } catch (Exception e) {
            }

            return "00:00:00:00:00:00";
        }

        /**
         * get the ip address in format: xxx.xxx.xxx.xxx
         *
         * @param context : current context
         * @return ip address, or null
         */
        public static String getIPAddress(Context context) {
            WifiInfo wi = getWifiInfo(context);
            if (wi != null) {
                int ip = wi.getIpAddress();
                int section1 = 0x000000FF & (ip >> 24);
                int section2 = 0x000000FF & (ip >> 16);
                int section3 = 0x000000FF & (ip >> 8);
                int section4 = 0x000000FF & ip;
                String strIP = Integer.toString(section4) + "." + Integer.toString(section3) + "." + Integer.toString(section2) + "." + Integer.toString(section1);
                return strIP;
            }

            return null;
        }
    }

    /**
     * pack the network utility methods of device
     */
    public final static class Network {
        /**
         * get the current active network information object
         *
         * @param context : current context
         * @return current active network information object, or null
         */
        public static NetworkInfo getCurrentActiveNetwork(Context context) {
            try {
                ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
                if (cm != null) {
                    NetworkInfo ni = cm.getActiveNetworkInfo();
                    if (ni != null)
                        return ni;
                }
            } catch (Exception e) {
            }

            return null;
        }

        /**
         * check current active network is available, notice available not means
         * the network is connected to the internet.
         *
         * @param context : current context
         * @return: true if the network is available, otherwise return false
         */
        public static boolean isAvailable(Context context) {
            NetworkInfo ni = getCurrentActiveNetwork(context);
            if (ni != null) {
                return ni.isAvailable();
            }
            return false;
        }

        /**
         * check current active network is connected, connected means you may
         * send data to the internet
         *
         * @param context : current context
         * @return: true if the network is available, otherwise return false
         */
        public static boolean isConnected(Context context) {
            NetworkInfo ni = getCurrentActiveNetwork(context);
            if (ni != null) {
                return ni.isConnected();
            }
            return false;
        }

        /**
         * get the current active network type name
         *
         * @param context : current context
         * @return: network type name, or null
         */
        public static String getTypeName(Context context) {
            Type type = getNetworkType(context);
            return type.getTypeName();
        }

        public static int getTypeOrdinal(Context context) {
            Type type = getNetworkType(context);
            return type.ordinal();
        }

        public static Type getNetworkType(Context context) {
            Type mobileType = Type.UNKNOWN;

            NetworkInfo networkInfo = getCurrentActiveNetwork(context);
            if (networkInfo != null && networkInfo.isConnected()) {
                if (networkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
                    mobileType = Type.WIFI;
                } else if (networkInfo.getType() == ConnectivityManager.TYPE_MOBILE) {
                    String _strSubTypeName = networkInfo.getSubtypeName();

                    // TD-SCDMA   networkType is 17
                    int networkType = networkInfo.getSubtype();
                    switch (networkType) {
                        case TelephonyManager.NETWORK_TYPE_GPRS:
                        case TelephonyManager.NETWORK_TYPE_EDGE:
                        case TelephonyManager.NETWORK_TYPE_CDMA:
                        case TelephonyManager.NETWORK_TYPE_1xRTT:
                        case TelephonyManager.NETWORK_TYPE_IDEN: //api<8 : replace by 11
                            mobileType = Type.MOBILE2G;
                            break;
                        case TelephonyManager.NETWORK_TYPE_UMTS:
                        case TelephonyManager.NETWORK_TYPE_EVDO_0:
                        case TelephonyManager.NETWORK_TYPE_EVDO_A:
                        case TelephonyManager.NETWORK_TYPE_HSDPA:
                        case TelephonyManager.NETWORK_TYPE_HSUPA:
                        case TelephonyManager.NETWORK_TYPE_HSPA:
                        case TelephonyManager.NETWORK_TYPE_EVDO_B: //api<9 : replace by 14
                        case TelephonyManager.NETWORK_TYPE_EHRPD:  //api<11 : replace by 12
                        case TelephonyManager.NETWORK_TYPE_HSPAP:  //api<13 : replace by 15
                            mobileType = Type.MOBILE3G;
                            break;
                        case TelephonyManager.NETWORK_TYPE_LTE:    //api<11 : replace by 13
                            mobileType = Type.MOBILE4G;
                            break;
                        default:
                            // http://baike.baidu.com/item/TD-SCDMA 中国移动 联通 电信 三种3G制式
                            if (_strSubTypeName.equalsIgnoreCase("TD-SCDMA") || _strSubTypeName.equalsIgnoreCase("WCDMA") || _strSubTypeName.equalsIgnoreCase("CDMA2000")) {
                                mobileType = Type.MOBILE3G;
                            }
                            break;
                    }
                }
            }

            return mobileType;
        }

        /**
         * get ip v4 address by using the java sdk methods
         *
         * @return ip v4 address, like 192.168.1.2, or failed return 0.0.0.0
         */
        public static String getIPAddress() {
            try {
                for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements(); ) {
                    NetworkInterface intf = en.nextElement();
                    if (intf != null) {
                        for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                            InetAddress inetAddress = enumIpAddr.nextElement();
                            if (!inetAddress.isLoopbackAddress() && inetAddress instanceof Inet4Address) {
                                return inetAddress.getHostAddress().toString();
                            }
                        }
                    }
                }
            } catch (Exception ex) {
            }

            return "0.0.0.0";
        }

        public enum Type {
            WIFI("wifi"),
            MOBILE2G("2G"),
            MOBILE3G("3G"),
            MOBILE4G("4G"),
            UNKNOWN("unknown");
            private String typeName;

            Type(String name) {
                typeName = name;
            }

            public String getTypeName() {
                return typeName;
            }
        }
    }

    /**
     * pack the file system utility methods of device
     *
     * @author wangyu
     */
    public final static class FileSystem {
        public final static int DIR_NEED_BLOCK_SIZE = 32;

        /**
         * test if the given directory is writable
         *
         * @param dir : directory path to be test
         * @return true if writable, otherwise return false
         */
        public static boolean isWritable(String dir) {
            try {
                if (TextUtils.isEmpty(dir)) {
                    DLLog.i("sdcard", "isWritable return false because dir is empty");
                    return false;
                }

                File fdir = new File(dir);
                if (!fdir.exists()) {
                    if (fdir.mkdirs() || fdir.isDirectory()) {
                        DLLog.i("sdcard", dir + " not exist and mkdirs success");
                    } else {
                        DLLog.i("sdcard", dir + " not exist and mkdirs error");
                        return false;
                    }
                } else {
                    DLLog.i("sdcard", dir + " exist and mkdirs success");
                }

                String testDir = DLString.randomString(16) + ".deelive";
                File fTestDir = new File(dir, testDir);

                if (!fTestDir.mkdirs() && !fTestDir.isDirectory()) {
                    DLLog.i("sdcard", fTestDir.getAbsolutePath() + " isWritable return false 2");
                    return false;
                }
                fTestDir.delete();
                DLLog.i("sdcard", dir + " isWritable return true");
                return true;
            } catch (Exception e) {
                DLLog.i("sdcard", dir + " isWritable error" + e.getMessage());
                return false;
            }
        }

        public static boolean mkdir(String dir) {
            if (TextUtils.isEmpty(dir)) {
                return false;
            }
            try {
                File fdir = new File(dir);
                return (fdir.mkdirs() || fdir.isDirectory());
            } catch (Exception e) {
                DLLog.i("sdcard", dir + " isWritable error" + e.getMessage());
                return false;
            }
        }

        /**
         * get the state of specified volume by path
         *
         * @param path
         * @return state of the volume, or null
         */
        @SuppressWarnings("deprecation")
        public static State getState(String path) {
            if (TextUtils.isEmpty(path)) {
                return null;
            }

            try {
                StatFs sf = new StatFs(path);
                long blockSize = sf.getBlockSize();
                long total = (long) sf.getBlockCount() * (long) blockSize;
                long available = (long) sf.getAvailableBlocks() * (long) blockSize;
                return new State(total, available);
            } catch (Exception e) {
                // add log here
                return null;
            }
        }

        public static long getBlockSize(String path) {
            if (TextUtils.isEmpty(path)) {
                return 0;
            }

            try {
                StatFs sf = new StatFs(path);
                return sf.getBlockSize();

            } catch (Exception e) {
                return 0;
            }
        }

        /**
         * get the total size in bytes of volume specified by the input path
         *
         * @param path
         * @return total size in bytes
         */
        @SuppressWarnings("deprecation")
        public static long getTotalSize(String path) {
            if (TextUtils.isEmpty(path)) {
                return 0;
            }

            try {
                StatFs sf = new StatFs(path);
                return (long) sf.getBlockCount() * (long) sf.getBlockSize();
            } catch (Exception e) {
                return 0;
            }
        }

        public static long getAvailableSize(String path) {
            if (TextUtils.isEmpty(path)) {
                return 0;
            }

            try {
                StatFs sf = new StatFs(path);
                return (long) sf.getAvailableBlocks() * (long) sf.getBlockSize();
            } catch (Exception e) {
                return 0;
            }
        }


        /**
         * check if the given path is exist directory
         *
         * @param path
         * @return return true when the path is an exist directory, otherwise
         * return false
         */
        public static boolean isDirectory(String path) {
            if (TextUtils.isEmpty(path))
                return false;

            File f = new File(path);
            if (f.exists() && f.isDirectory())
                return true;

            return false;
        }

        /**
         * get the external storage information, size in MB
         *
         * @return external storage information or null;
         */
        @SuppressWarnings("deprecation")
        public static Volume getExternalStorage() {
            try {
                File path = Environment.getExternalStorageDirectory();
                StatFs sf = new StatFs(path.getPath());
                long total = (long) sf.getBlockCount() * (long) sf.getBlockSize();
                long available = (long) sf.getAvailableBlocks() * (long) sf.getBlockSize();

                DLLog.e("FSDevice", "getExternalStorage() found volume:" + path.getPath());
                return new Volume("手机存储", path.getPath(), new State(total, available));
            } catch (Exception e) {
                DLLog.e("FSDevice", "Error in getExternalStorage()" + e.getMessage());
                return null;
            }
        }

        /**
         * get the external storage path
         *
         * @return
         */
        public static String getExternalStorageDir() {
            try {
                File path = Environment.getExternalStorageDirectory();
                return path.getAbsolutePath();
            } catch (Exception e) {
                return null;
            }
        }

        public static String getAppDataDir(Context context) {
            try {
                Class<?> c = Class.forName("android.os.Environment");
                Method getExternalStorageAppDataDirectory = c.getMethod("getExternalStorageAppDataDirectory", String.class);
                File dir = (File) getExternalStorageAppDataDirectory.invoke(Environment.class, context.getPackageName());
                if (dir.exists() || dir.mkdirs()) {
                    return dir.getAbsolutePath();
                } else {
                    return FileSystem.getDefaultAppDataDir(context);
                }
            } catch (Exception e) {
                return FileSystem.getDefaultAppDataDir(context);
            }
        }

        public static String getDefaultAppDataDir(Context context) {
            try {
                File defaultAppDataDir = new File(new File(Environment.getDataDirectory(), "data"), context.getPackageName() + "/data");
                if (defaultAppDataDir.exists() || defaultAppDataDir.mkdirs()) {
                    return defaultAppDataDir.getAbsolutePath();
                } else {
                    return null;
                }
            } catch (Exception e) {
                return null;
            }
        }

        public static String getAppMediaDir(Context context) {
            try {
                Class<?> c = Class.forName("android.os.Environment");
                Method getExternalStorageAppMediaDirectory = c.getMethod("getExternalStorageAppMediaDirectory", String.class);
                File dir = (File) getExternalStorageAppMediaDirectory.invoke(Environment.class, context.getPackageName());
                if (dir.exists() || dir.mkdirs()) {
                    return dir.getAbsolutePath();
                } else {
                    return FileSystem.getDefaultAppMediaDir(context);
                }
            } catch (Exception e) {
                return FileSystem.getDefaultAppMediaDir(context);
            }
        }

        public static String getDefaultAppMediaDir(Context context) {
            try {
                File defaultAppMediaDir = new File(new File(Environment.getDataDirectory(), "data"), context.getPackageName() + "/media");
                if (defaultAppMediaDir.exists() || defaultAppMediaDir.mkdirs()) {
                    return defaultAppMediaDir.getAbsolutePath();
                } else {
                    return null;
                }
            } catch (Exception e) {
                return null;
            }
        }

        public static String getAppFilesDir(Context context) {
            try {
                Class<?> c = Class.forName("android.os.Environment");
                Method getExternalStorageAppFilesDirectory = c.getMethod("getExternalStorageAppFilesDirectory", String.class);
                File dir = (File) getExternalStorageAppFilesDirectory.invoke(Environment.class, context.getPackageName());
                if (dir.exists() || dir.mkdirs()) {
                    return dir.getAbsolutePath();
                } else {
                    return FileSystem.getDefaultAppFilesDir(context);
                }

            } catch (Exception e) {
                return FileSystem.getDefaultAppFilesDir(context);
            }
        }

        public static String getDefaultAppFilesDir(Context context) {
            try {
                File filesDir = context.getFilesDir();
                if (filesDir.exists() || filesDir.mkdirs()) {
                    return filesDir.getAbsolutePath();
                } else {
                    return null;
                }
            } catch (Exception e) {
                return null;
            }
        }

        public static String getAppCacheDir(Context context) {
            try {
                Class<?> c = Class.forName("android.os.Environment");
                Method getExternalStorageAppCacheDirectory = c.getMethod("getExternalStorageAppCacheDirectory", String.class);
                File dir = (File) getExternalStorageAppCacheDirectory.invoke(Environment.class, context.getPackageName());
                if (dir.exists() || dir.mkdirs()) {
                    return dir.getAbsolutePath();
                } else {
                    return FileSystem.getDefaultAppCacheDir(context);
                }
            } catch (Exception e) {
                return FileSystem.getDefaultAppCacheDir(context);
            }
        }

        public static String getDefaultAppCacheDir(Context context) {
            try {
                File cacheDir = context.getCacheDir();
                if (cacheDir.exists() || cacheDir.mkdirs()) {
                    return cacheDir.getAbsolutePath();
                } else {
                    return null;
                }
            } catch (Exception e) {
                return null;
            }
        }

        /**
         * get all valid volumes
         *
         * @param context
         * @return
         */

        @SuppressLint("NewApi")
        public static Volume[] getValidVolumes(Context context) {
            try {
                File file1 = Environment.getExternalStorageDirectory();
                File file2 = context.getExternalFilesDir(null);
                File file3 = context.getFilesDir();
                DLLog.i("sdcard", "getExternalStorageDirectory: " + file1.getAbsolutePath());
                DLLog.i("sdcard", "getExternalFilesDir: " + file2.getAbsolutePath());
                DLLog.i("sdcard", "getFilesDir: " + file3.getAbsolutePath());

                File[] files4 = ContextCompat.getExternalFilesDirs(context, null);
                for (int i = 0; i < files4.length; i++) {
                    DLLog.i("sdcard", "getExternalFilesDirs: " + files4[i].getAbsolutePath());
                }

            } catch (Throwable t) {
                DLLog.i("sdcard", "getExternalFilesDirs exception");
            }

            List<Volume> volumes = getVolumes1(context);
            if (volumes == null || volumes.size() == 0) {
                volumes = getVolumes2(context);
            }

            if (volumes == null || volumes.size() == 0) {
                Volume volume = getExternalStorage();
                if (volume != null) {
                    if (volumes == null) {
                        volumes = new ArrayList<Volume>();
                    }
                    volumes.add(volume);
                }
            }

            if (volumes != null && !volumes.isEmpty()) {
                return volumes.toArray(new Volume[volumes.size()]);
            } else {
                return null;
            }
        }

        private static String getAndroidDataDir(String root) {
            if (root == null) {
                return "/data/data";
            }

            return root + "/Android/data";
        }

        private static String getVolumeWritePath(String volumePath, String packageName) {

            try {
                File file = new File(volumePath);
                long blockSize = getBlockSize(volumePath);
                long avaliableSize = file.getUsableSpace();
                long totalSize = file.getTotalSpace();
                String androidDataDir = FileSystem.getAndroidDataDir(volumePath);
                DLLog.i("sdcard", volumePath + " info: " + totalSize + " | " + avaliableSize + " | " + blockSize);
                if (totalSize == 0) {
                    DLLog.i("sdcard", "no use path because totalSize is 0:" + volumePath);
                    return null;
                }

                String path = volumePath;
                if (FileSystem.isWritable(path)) {
                    return path;
                }

                if (DLDir.exist(androidDataDir)) {
                    path = androidDataDir + "/" + packageName + "/files";
                } else {
                    path = volumePath + "/" + packageName + "/files";
                }

                if (FileSystem.isWritable(path)) {
                    return path;
                } else {
                    return volumePath;
                }
            } catch (Exception e) {
                return volumePath;
            }
        }

        private static List<Volume> getVolumes1(Context context) {

            try {
                String path;
                String strDescription;
                boolean isRemovable;
                List<Volume> volumes = new ArrayList<Volume>();

                Method getSystemService = context.getClass().getMethod("getSystemService", String.class);
                Object sm = getSystemService.invoke(context, "storage");
                Method getVolumeList = sm.getClass().getMethod("getVolumeList");

                Object[] objs = (Object[]) getVolumeList.invoke(sm);

                Method method;
                for (int i = 0; i < objs.length; i++) {
                    method = objs[i].getClass().getMethod("getPath");
                    method.setAccessible(true);
                    path = (String) method.invoke(objs[i]);

                    try {
                        method = objs[i].getClass().getMethod("getDescription", Context.class);
                        method.setAccessible(true);
                        strDescription = (String) method.invoke(objs[i], context);
                    } catch (Exception e) {
                        strDescription = "";
                    }


                    try {
                        method = objs[i].getClass().getMethod("isRemovable");
                        method.setAccessible(true);
                        isRemovable = (Boolean) method.invoke(objs[i]);
                    } catch (Exception e) {
                        isRemovable = true;
                    }

                    boolean isExternalVolue = true;
                    String storageNmae = "sd卡";
                    if (isRemovable) {
                        if (strDescription.contains("内") || strDescription.contains("手机存储") || strDescription.contains("internal")) {
                            isExternalVolue = false;
                            storageNmae = "手机存储";
                        }
                    } else {
                        isExternalVolue = false;
                        storageNmae = "手机存储";
                    }

                    path = getVolumeWritePath(path, context.getPackageName());
                    DLLog.e("sdcard", "getVolumeWritePath " + path);
                    if (path == null) {
                        continue;
                    }

                    Volume volume = new Volume(storageNmae, path, getState(path));
                    if (volume != null && !FileSystem.existVolumeInList(volume, volumes, context.getPackageName())) {
                        volume.setExternal(isExternalVolue);
                        volume.setName(storageNmae);
                        volumes.add(volume);
                    }
                }
                DLLog.i("sdcard", "getVolumes1() found volumes:" + volumes.toString());
                return volumes;
            } catch (Exception e) {
                DLLog.e("sdcard", "Error in getVolumes1()" + e.getMessage());
            }
            return null;
        }

        private static List<Volume> getVolumes2(Context context) {
            try {
                List<Volume> volumes = new ArrayList<Volume>();

                Scanner scanner = new Scanner(new File("/system/etc/vold.fstab"));
                while (scanner.hasNext()) {
                    boolean isExternalVolue = true;
                    String storageNmae = "sd卡";
                    String line = scanner.nextLine();
                    if (line.startsWith("dev_mount")) {
                        String[] lineElements = line.split(" ");
                        String lable = lineElements[1];
                        String path = lineElements[2];

                        if (path.contains(":")) {
                            path = path.substring(0, path.indexOf(":"));
                        }

                        if (path.contains("usb")) {
                            continue;
                        }

                        if (lable.contains("int") || path.contains("int")) {
                            isExternalVolue = false;
                            storageNmae = "手机存储";
                        }

                        path = getVolumeWritePath(path, context.getPackageName());
                        if (path == null) {
                            continue;
                        }

                        Volume volume = new Volume(storageNmae, path, getState(path));
                        if (volume != null && !FileSystem.existVolumeInList(volume, volumes, context.getPackageName())) {
                            volume.setExternal(isExternalVolue);
                            volume.setName(storageNmae);
                            volumes.add(volume);
                        }

                    }
                }
                scanner.close();
                DLLog.i("FSDevice", "getVolumes2() found volumes:" + volumes.toString());
                return volumes;
            } catch (Exception e) {
                DLLog.e("FSDevice", "Error in getVolumes2()" + e.getMessage());
            }
            return null;
        }

        private static boolean existVolumeInList(Volume volume, List<Volume> volumeList, String packageName) {
            try {
                for (Volume listVolume : volumeList) {
                    if (volume.equals(listVolume, packageName)) {
                        return true;
                    }
                }
                return false;
            } catch (Exception e) {
                return false;
            }
        }

        /**
         * storage space data structure of a volume
         */
        public static class State {
            private long total = 0;// in bytes
            private long available = 0; // in bytes

            public State(long total, long available) {
                this.total = total;
                this.available = available;
            }

            public long getTotal() {
                return total;
            }

            public long getAvailable() {
                return available;
            }
        }

        /**
         * volume information data structure
         */
        public static class Volume {
            private String name = null;
            private String path = null;
            private State state = null;
            private boolean isExternal = false;

            public Volume(String name, String path, State state) {
                this.name = name;
                this.path = path;
                this.state = state;
            }

            public String getName() {
                return this.name;
            }

            public void setName(String name) {
                this.name = name;
            }

            public String getPath() {
                return this.path;
            }

            public void setPath(String path) {
                this.path = path;
            }

            public State getState() {
                return this.state;
            }

            public boolean isExternal() {
                return isExternal;
            }

            public void setExternal(boolean isExternal) {
                this.isExternal = isExternal;
            }

            public String getRootPath(String packageName) {
                int index = path.indexOf("/deelive");
                String retPath = path;
                if (index > 0) {
                    retPath = retPath.substring(0, index);
                }

                index = retPath.indexOf("/" + packageName);
                if (index > 0) {
                    retPath = retPath.substring(0, index);
                }

                return retPath;
            }


            public String toString() {
                if (state != null) {
                    return "name: " + this.name + ", path: " + this.path + ", total: " + this.state.getTotal() + "B, available: " + this.state.getAvailable() + "B";
                } else {
                    return "name: " + this.name + ", path: " + this.path;
                }

            }

            public boolean equals(Object obj, String packageName) {
                try {
                    if (!(obj instanceof Volume)) {
                        return false;
                    }

                    Volume volume = (Volume) obj;
                    String path1 = volume.getRootPath(packageName) + "/";
                    String path2 = this.getRootPath(packageName) + "/";
                    if (path1.startsWith(path2) || path2.startsWith(path1)) {
                        if (volume.getState().getTotal() == this.getState().getTotal()) {
                            DLLog.i("sdcard", path2 + " equals true " + path1);
                            return true;
                        }
                    }
                    DLLog.i("sdcard", path2 + " equals false " + path1);
                    return false;
                } catch (Exception e) {
                    DLLog.i("sdcard", "equals error" + e.getMessage());
                    return false;
                }
            }
        }
    }

    /**
     * memory information data structure
     */
    public static class MemInfo {
        public long total = 0; // in MB
        public long left = 0; // in MB
        public long buffers = 0;// in MB
        public long cached = 0;// in MB

        public MemInfo(long total, long left, long buffers, long cached) {
            this.total = total;
            this.left = left;
            this.buffers = buffers;
            this.cached = cached;
        }
    }

    public final static class ApplicationInfos {

        public static List<PackageInfo> getInstallApplication(Context context, boolean filtSystemApp) {
            List<PackageInfo> appList = new ArrayList<PackageInfo>();
            List<PackageInfo> packages = context.getPackageManager().getInstalledPackages(0);

            for (int i = 0; i < packages.size(); i++) {
                PackageInfo packageInfo = packages.get(i);
                if ((packageInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0) {
                    appList.add(packageInfo);// 如果非系统应用，则添加至appList
                }

            }

            return appList;
        }

        public static String loadApplicationLable(Context context, PackageInfo packageInfo) {
            return "" + packageInfo.applicationInfo.loadLabel(context.getPackageManager());
        }

        /**
         * 比较版本号
         *
         * @param version1
         * @param version2
         * @return int 正数 前面版本大。 负数后面版本大。 0为相等
         * @author wangyu
         */
        public static int compareVersionName(String version1, String version2) {
            if (version1 == null && version2 == null)
                return 0;
            else if (version1 == null)
                return -1;
            else if (version2 == null)
                return 1;

            String[] versionArray1 = version1.split("[^a-zA-Z0-9]+");
            String[] versionArray2 = version2.split("[^a-zA-Z0-9]+");

            int singleVersion1, singleVersion2;

            for (int index = 0, max = Math.min(versionArray1.length, versionArray2.length); index <= max; index++) {
                if (index == versionArray1.length)
                    return index == versionArray2.length ? 0 : -1;
                else if (index == versionArray2.length)
                    return 1;

                try {
                    singleVersion1 = Integer.parseInt(versionArray1[index]);
                } catch (Exception x) {
                    singleVersion1 = Integer.MAX_VALUE;
                }

                try {
                    singleVersion2 = Integer.parseInt(versionArray2[index]);
                } catch (Exception x) {
                    singleVersion2 = Integer.MAX_VALUE;
                }

                if (singleVersion1 != singleVersion2) {
                    return singleVersion1 - singleVersion2;
                }

                int result = versionArray1[index].compareTo(versionArray2[index]);

                if (result != 0)
                    return result;
            }

            return 0;
        }

    }
}
