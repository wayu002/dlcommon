package tv.tutulive.plus.dlcommon.tool;

import android.app.Activity;
import android.app.Application;
import android.content.ComponentCallbacks;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Build;
import android.support.annotation.NonNull;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.WindowManager;

import tv.tutulive.plus.dlcommon.log.DLLog;

/**
 * 屏幕计算工具类
 * Created by wangyu on 16/9/1.
 */
public class DLScreen {

    private DLScreen() {
        throw new UnsupportedOperationException("Tool class can not instantiate");
    }

    private static DisplayMetrics getDisplayMetrics(Context ctx) {
        try{
            DisplayMetrics outMetrics = new DisplayMetrics();
            WindowManager wm = (WindowManager) ctx.getSystemService(Context.WINDOW_SERVICE);
            wm.getDefaultDisplay().getMetrics(outMetrics);
            return outMetrics;
        }catch(NullPointerException e){
            DLLog.e("DLScreen", e.getMessage());
            return new NullDisplayMetrics();
        }

    }

    /**
     * 获得设备的dpi
     */
    public static int getScreenDpi(Context ctx) {
        return getDisplayMetrics(ctx).densityDpi;
    }

    /**
     * 获得设备屏幕密度
     */
    public static float getScreenDensity(Context ctx) {
        DisplayMetrics dm = getDisplayMetrics(ctx);
        return dm.density;
    }

    public static float getScreenScaledDensity(Context ctx) {
        DisplayMetrics dm = getDisplayMetrics(ctx);
        return dm.scaledDensity;
    }

    /**
     * 获取系统状态栏高度
     */
    public static int getStatusBarHeight(Context context) {
        int result = 0;
        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = context.getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    /**
     * 获得设备屏幕宽度
     */
    public static int getScreenWidth(Context ctx) {
        DisplayMetrics dm = getDisplayMetrics(ctx);
        return dm.widthPixels;
    }

    /**
     * 获得设备屏幕高度
     * According to phone resolution height
     */
    public static int getScreenHeight(Context ctx) {
        DisplayMetrics dm = getDisplayMetrics(ctx);
        return dm.heightPixels;
    }

    /**
     * According to the resolution of the phone from the dp unit will become a px (pixels)
     */
    public static int dip2px(Context ctx, int dip) {
        float density = getScreenDensity(ctx);
        return (int) (dip * density + 0.5f);
    }
    /**
     * According to the resolution of the phone from the dp unit will become a px (pixels)
     */
    public static float dip2px(Context ctx, float dip) {
        float density = getScreenDensity(ctx);
        return  (dip * density + 0.5f);
    }

    /**
     * Turn from the units of px (pixels) become dp according to phone resolution
     */
    public static int px2dip(Context ctx, float px) {
        float density = getScreenDensity(ctx);
        return (int) (px / density + 0.5f);
    }



    public static int px2sp(Context ctx, float px) {
        float scale = getScreenScaledDensity(ctx);
        return (int) (px / scale + 0.5f);
    }


    public static int sp2px(Context ctx, int sp){
        float scale = getScreenScaledDensity(ctx);
        return (int) (sp * scale + 0.5f);
    }

    /**
     * @ClassName: NullDisplayMetrics
     * @Description: 防止获取DisplayMetrics对象失败而导致的nullpointer异常
     * @date 2015年10月12日 下午3:04:17
     *
     */
    static class NullDisplayMetrics extends DisplayMetrics{
        public NullDisplayMetrics(){
            widthPixels = 0;
            heightPixels = 0;
            density = 0.0f;
            densityDpi = 120;
            scaledDensity = 0.0f;
            xdpi = 0.0f;
            ydpi = 0.0f;
        }
    }

    public static boolean deviceHasNavigationBar(Activity activity){
        WindowManager windowManager = activity.getWindowManager();
        Display d = windowManager.getDefaultDisplay();
        DisplayMetrics realDisplayMetrics = new DisplayMetrics();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            d.getRealMetrics(realDisplayMetrics);
        }
        int realHeight = realDisplayMetrics.heightPixels;
        int realWidth = realDisplayMetrics.widthPixels;
        DisplayMetrics displayMetrics = new DisplayMetrics();
        d.getMetrics(displayMetrics);
        int displayHeight = displayMetrics.heightPixels;
        int displayWidth = displayMetrics.widthPixels;
        return (realWidth - displayWidth) > 0 || (realHeight - displayHeight) > 0;
    }

    public static int getNavigationBarHeight(Activity activity) {
        WindowManager windowManager = activity.getWindowManager();
        Display d = windowManager.getDefaultDisplay();
        DisplayMetrics realDisplayMetrics = new DisplayMetrics();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            d.getRealMetrics(realDisplayMetrics);
        }
        int realHeight = realDisplayMetrics.heightPixels;
        DisplayMetrics displayMetrics = new DisplayMetrics();
        d.getMetrics(displayMetrics);
        int displayHeight = displayMetrics.heightPixels;
        return realHeight - displayHeight;
    }

    // 适配基准
    public static final int MATCH_BASE_WIDTH = 0;
    public static final int MATCH_BASE_HEIGHT = 1;

    // 适配单位
    public static final int MATCH_UNIT_DP = 0;

    // 系统原始DisplayMetrics信息
    static DisplayMetrics sDefaultMetrics;

    /**
     * 初始化屏幕适配
     * @param application
     */
    public static void setup(final Application application){
        if(sDefaultMetrics != null){
            return;
        }
        DisplayMetrics displayMetrics = application.getResources().getDisplayMetrics();
        if(sDefaultMetrics == null){
            sDefaultMetrics = new DisplayMetrics();
        }
        sDefaultMetrics.setTo(displayMetrics);
        application.registerComponentCallbacks(new ComponentCallbacks() {
            @Override
            public void onConfigurationChanged(Configuration newConfig) {
                if(newConfig != null && newConfig.fontScale > 0){
                    sDefaultMetrics.scaledDensity = application.getResources().getDisplayMetrics().scaledDensity;
                }
            }

            @Override
            public void onLowMemory() {

            }
        });
    }

    /**
     * 对指定Activity进行适配
     * @param context
     * @param designSize
     * @param matchBase
     * @param matchUnit
     */
    public static void match(@NonNull final Context context, final float designSize,
                             final int matchBase, final int matchUnit){
        if(designSize <= 0){
            throw new IllegalArgumentException("Design size is not correct");
        }
        final float targetDensity;
        if(matchBase == MATCH_BASE_WIDTH){
            targetDensity = sDefaultMetrics.widthPixels * 1f / designSize;
        }else {
            targetDensity = sDefaultMetrics.heightPixels * 1f / designSize;
        }
        final int targetDensityDpi = (int) (targetDensity * 160);
        final float targetScaledDensity = targetDensity * (sDefaultMetrics.density / sDefaultMetrics.scaledDensity);
        final DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        displayMetrics.density = targetDensity;
        displayMetrics.densityDpi = targetDensityDpi;
        displayMetrics.scaledDensity = targetScaledDensity;
    }
}
