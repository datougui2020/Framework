package com.framework.util;

import android.app.Activity;
import android.app.AppOpsManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.service.notification.NotificationListenerService;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationManagerCompat;
import android.text.TextUtils;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Set;

/**
 * @author YobertJomi
 * className RequestPermissionsUtil Android6.0请求权限工具类
 * created at  2016/9/26  10:18
 */
public class RequestPermissionsUtil {
    private static volatile RequestPermissionsUtil instance;
    public static int PERMISSION_LOCATION = 1;
    public static int PERMISSION_CAMERA = 2;
    public static int PERMISSION_READ_EXTERNAL_STORAGE = 3;
    public static int PERMISSION_WRITE_EXTERNAL_STORAGE = 4;
    public static int PERMISSION_WRITE_READ_EXTERNAL_STORAGE = 5;
    public static int PERMISSION_CALL_PHONE = 6;

    private RequestPermissionsUtil() {
    }

    public static RequestPermissionsUtil getInstance() {
        if (null == instance) {
            synchronized (RequestPermissionsUtil.class) {
                if (null == instance) {
                    instance = new RequestPermissionsUtil();
                }
            }
        }
        return instance;
    }

    /**
     * @param activity    activity
     * @param permissions new String{ Manifest.permission.ACCESS_COARSE_LOCATION }
     * @return true 需要申请权限，false已有权限
     */
    public boolean checkPermissions(Activity activity, String[] permissions) {
        if (null != permissions) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(activity, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * @param activity    activity
     * @param permissions new String{ Manifest.permission.ACCESS_COARSE_LOCATION }
     * @return false 需要申请权限，true已有权限
     */
    public boolean checkPermissionsThenRequest(Activity activity, String[] permissions,
                                               int requestCode) {
        if (null != permissions) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(activity, permission) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(activity, permissions, requestCode);
                    return false;
                }
            }
        }
        return true;
    }
    /**
     * @param activity
     * @param permissions new String{ Manifest.permission.ACCESS_COARSE_LOCATION }
     */
    //    private void checkPermissions(Activity activity, String[] permissions, int requestCode) {
    //        if (null != permissions) {
    //            List<String> listPermission = new ArrayList<String>();
    //            String[] permissionsTemp;
    //            for (String permission : permissions) {
    //                if (ActivityCompat.checkSelfPermission(activity, permission) != PackageManager
    // .PERMISSION_GRANTED) {
    //                    listPermission.add(permission);
    //                }
    //            }
    //            if (listPermission.size() > 0) {
    //                permissionsTemp = (String[]) listPermission.toArray();
    //                ActivityCompat.requestPermissions(activity, permissionsTemp, requestCode);
    //            }
    //        }
    //    }

    /**
     * 以下代码可以跳转到应用详情，可以通过应用详情跳转到权限界面(6.0系统测试可用)
     *
     * @param packageName 包名，当前应用context.getPackageName(),其他的传入字符串
     */
    public void showInstalledAppDetailSettingIntent(Context context, String packageName) {
        Intent localIntent = new Intent();
        localIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (Build.VERSION.SDK_INT >= 9) {// 2.3（ApiLevel 9）以上，使用SDK提供的接口
            localIntent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            localIntent.setData(Uri.fromParts("package", TextUtils.isEmpty(packageName) ?
                    context.getPackageName() :
                    packageName, null));
        } else if (Build.VERSION.SDK_INT <= 8) {// 2.3以下，使用非公开的接口（API9查看InstalledAppDetails源码）
            // 2.2和2.1中，InstalledAppDetails使用的APP_PKG_NAME不同。pkg(2.2 API8)/com.android.settings
            // .ApplicationPkgName
            // (2.1 API7及以下)
            localIntent.setAction(Intent.ACTION_VIEW);
            localIntent.setClassName("com.android.settings", "com.android.settings" +
                    ".InstalledAppDetails");
            localIntent.putExtra(Build.VERSION.SDK_INT == 8 ? "pkg" : "com.android.settings" +
                            ".ApplicationPkgName",
                    TextUtils.isEmpty(packageName) ? context.getPackageName() : packageName);
        }
        context.startActivity(localIntent);
    }

    /**
     * 以下代码可以跳转到应用详情，可以通过应用详情跳转到权限界面(6.0系统测试可用)
     * 当前应用
     */
    public void showCurrentAppDetailSettingIntent(Context context) {
        showInstalledAppDetailSettingIntent(context, context.getPackageName());
    }

    /**
     * 打开通知侦听设置页面
     *
     * @param
     */
    public void openNotificationListenSettings(Context context) {
        try {
            Intent intent;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP_MR1) {
                intent = new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS);
            } else {
                intent = new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS");
            }
            context.startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 是否有通知侦听权限
     *
     * @param
     */
    public boolean isNotificationListenerEnabled(Context context) {
        Set<String> packageNames = NotificationManagerCompat.getEnabledListenerPackages(context);
        if (packageNames.contains(context.getPackageName())) {
            return true;
        }
        return false;
    }

    /**
     * 解决
     * NotificationListenerService不能监听到通知
     * 该方法使用前提是 NotificationListenerService 已经被用户授予了权限，否则无效
     *
     * @param context
     * @param cls
     */
    public void toggleNotificationListenerService(Context context, Class<?
            extends NotificationListenerService> cls) {
        PackageManager pm = context.getPackageManager();
        pm.setComponentEnabledSetting(new ComponentName(context, cls),
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
        pm.setComponentEnabledSetting(new ComponentName(context, cls),
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
    }

    /**
     * 是否有普通通知权限
     *
     * @param
     */
    public boolean areNotificationsEnabled(Context context) {
        return NotificationManagerCompat.from(context).areNotificationsEnabled();
    }

    /**
     * 是否有普通通知权限
     *
     * @param
     */
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public boolean isNotificationEnabled(Context context) {
        final String CHECK_OP_NO_THROW = "checkOpNoThrow";
        final String OP_POST_NOTIFICATION = "OP_POST_NOTIFICATION";

        AppOpsManager mAppOps = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);

        ApplicationInfo appInfo = context.getApplicationInfo();

        String pkg = context.getApplicationContext().getPackageName();

        int uid = appInfo.uid;

        Class appOpsClass = null; /* Context.APP_OPS_MANAGER */

        try {

            appOpsClass = Class.forName(AppOpsManager.class.getName());

            Method checkOpNoThrowMethod = appOpsClass.getMethod(CHECK_OP_NO_THROW, Integer.TYPE,
                    Integer.TYPE, String
                    .class);

            Field opPostNotificationValue = appOpsClass.getDeclaredField(OP_POST_NOTIFICATION);
            int value = (int) opPostNotificationValue.get(Integer.class);

            return ((int) checkOpNoThrowMethod.invoke(mAppOps, value, uid, pkg) == AppOpsManager.MODE_ALLOWED);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 跳转到设置通知界面
     */
    public void requestSetNotification(Context context) {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Intent intent = new Intent();
            intent.setAction(Settings.ACTION_APP_NOTIFICATION_SETTINGS);
            intent.putExtra(Settings.EXTRA_APP_PACKAGE, context.getPackageName());
            context.startActivity(intent);
        } else if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Intent intent = new Intent();
            intent.setAction("android.settings.APP_NOTIFICATION_SETTINGS");
            intent.putExtra("app_package", context.getPackageName());
            intent.putExtra("app_uid", context.getApplicationInfo().uid);
            context.startActivity(intent);
        } else if (android.os.Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT) {
            Intent intent = new Intent();
            intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            intent.addCategory(Intent.CATEGORY_DEFAULT);
            intent.setData(Uri.parse("package:" + context.getPackageName()));
            context.startActivity(intent);
        } else {
            //跳转到详情
            showCurrentAppDetailSettingIntent(context);
        }
    }

    /**
     * 打开setting-忽略电池优化
     *
     * @param context context
     */
    public void openIgnoreBatteryOptimization(Context context) {
        if (context != null) {
            Intent intent2;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                intent2 = new Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS);
                context.startActivity(intent2);
            }
        }
    }

    /**
     * 打开setting-忽略电池优化
     *
     * @param context context
     */
    public void requestIgnoreBatteryOptimization(Context context) {
        if (context != null) {
            Intent intent = new Intent();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                intent.setAction(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                intent.setData(Uri.parse("package:" + context.getPackageName()));
                context.startActivity(intent);
            }
        }
    }

    /**
     * 将用户引导至安装未知应用界面。（8.0及以上）
     *
     * @param activity    Activity
     * @param requestCode 跟申请普通权限一样onRequestPermissionsResult
     */
    public void requestManageUnknownAppSources(Activity activity, int requestCode) {
        if (activity != null) {
            Intent intent;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                intent = new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES);
                activity.startActivityForResult(intent, requestCode);
            }
        }
    }
}
