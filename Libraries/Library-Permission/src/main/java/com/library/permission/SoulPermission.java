package com.library.permission;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import androidx.annotation.CheckResult;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.util.Log;

import com.library.permission.bean.Permission;
import com.library.permission.bean.Permissions;
import com.library.permission.bean.Special;
import com.library.permission.callbcak.CheckRequestPermissionListener;
import com.library.permission.callbcak.CheckRequestPermissionsListener;
import com.library.permission.callbcak.CheckStatusCallBack;
import com.library.permission.callbcak.RequestPermissionListener;
import com.library.permission.callbcak.SpecialPermissionListener;
import com.library.permission.checker.CheckerFactory;
import com.library.permission.debug.PermissionDebug;
import com.library.permission.request.PermissionRequester;

import java.util.LinkedList;
import java.util.List;

import static android.os.Build.VERSION_CODES.KITKAT;
import static android.os.Build.VERSION_CODES.O;

public class SoulPermission {

    private static final String TAG = SoulPermission.class.getSimpleName();

    private static SoulPermission instance;

    private static Application globalContext;

    private static boolean alreadyInit;

    private PermissionActivityLifecycle lifecycle;

    /**
     * 获取 Permission 对象
     */
    public static SoulPermission getInstance() {
        if (null == instance) {
            synchronized (SoulPermission.class) {
                if (instance == null) {
                    instance = new SoulPermission();
                }
            }
        }
        return instance;
    }

    /**
     * 设置debug
     * 可见日志打印
     * 当自动初始化失败后，有toast 提示
     */
    public static void setDebug(boolean isDebug) {
        PermissionDebug.setDebug(isDebug);
    }

    /**
     * init
     * no necessary
     * invoke it when auto init failed
     *
     * @see #setDebug(boolean)
     */
    public static void init(@NonNull Application application) {
        if (alreadyInit) {
            PermissionDebug.w(TAG, "already init");
            return;
        }
        globalContext = application;
        getInstance().registerLifecycle(globalContext);
        alreadyInit = true;
        PermissionDebug.d(TAG, "user init");
    }

    /**
     * 检查权限
     *
     * @param permission 权限名称
     * @return 返回检查的结果
     * @see #checkPermissions
     */
    public Permission checkSinglePermission(@NonNull String permission) {
        return checkPermissions(permission)[0];
    }

    /**
     * 一次检查多项权限
     *
     * @param permissions 权限名称 ,可检测多个
     * @return 返回检查的结果
     */
    public Permission[] checkPermissions(@NonNull String... permissions) {
        List<com.library.permission.bean.Permission> resultPermissions = new LinkedList<>();
        for (String permission : permissions) {
            int isGranted = checkPermission(getContext(), permission)
                    ? PackageManager.PERMISSION_GRANTED
                    : PackageManager.PERMISSION_DENIED;
            resultPermissions.add(new Permission(permission, isGranted, false));
        }
        return PermissionTools.convert(resultPermissions);
    }

    /**
     * 检查特殊权限，譬如通知
     *
     * @param special 特殊权限枚举
     * @return 检查结果
     * @see Special
     */
    public boolean checkSpecialPermission(Special special) {
        return CheckerFactory.create(getContext(), special).check();
    }

    /**
     * 单个权限的检查与申请
     * 在敏感操作前，先检查权限和请求权限，当完成操作后可做后续的事情
     *
     * @param permissionName 权限名称 例如：Manifest.permission.CALL_PHONE
     * @param listener       请求之后的回调
     * @see #checkAndRequestPermissions
     */
    public void checkAndRequestPermission(@NonNull final String permissionName,
                                          @NonNull final CheckRequestPermissionListener listener) {
        checkAndRequestPermissions(Permissions.build(permissionName),
                new CheckRequestPermissionsListener() {
            @Override
            public void onAllPermissionGranted(com.library.permission.bean.Permission[] allPermissions) {
                listener.onPermissionGranted(allPermissions[0]);
            }

            @Override
            public void onPermissionDenied(com.library.permission.bean.Permission[] refusedPermissions) {
                listener.onPermissionDenied(refusedPermissions[0]);
            }
        });
    }

    /**
     * 多个权限的检查与申请
     * 在敏感操作前，先检查权限和请求权限，当完成操作后可做后续的事情
     *
     * @param permissions 多个权限的申请  Permissions.build(Manifest.permission.CALL_PHONE,Manifest
     *                    .permission.CAMERA)
     * @param listener    请求之后的回调
     */
    public void checkAndRequestPermissions(@NonNull Permissions permissions,
                                           @NonNull final CheckRequestPermissionsListener listener) {
        //check permission first
        com.library.permission.bean.Permission[] checkResult =
                checkPermissions(permissions.getPermissionsString());
        //get refused permissions
        final com.library.permission.bean.Permission[] refusedPermissionList =
                filterRefusedPermissions(checkResult);
        // all permissions ok
        if (refusedPermissionList.length == 0) {
            PermissionDebug.d(TAG, "all permissions ok");
            listener.onAllPermissionGranted(checkResult);
            return;
        }
        //can request runTime permission
        if (canRequestRunTimePermission()) {
            requestPermissions(Permissions.build(refusedPermissionList), listener);
        } else {
            PermissionDebug.d(TAG, "some permission refused but can not request");
            listener.onPermissionDenied(refusedPermissionList);
        }
    }

    /**
     * 检查和请求特殊权限
     *
     * @param special  特殊权限、系统弹窗，未知来源
     *                 {@link Special }
     * @param listener 请求回调
     */
    public void checkAndRequestPermission(@NonNull Special special,
                                          @NonNull SpecialPermissionListener listener) {
        boolean permissionResult = checkSpecialPermission(special);
        if (permissionResult) {
            listener.onGranted(special);
            return;
        }
        int currentOsVersion = Build.VERSION.SDK_INT;
        switch (special) {
            case UNKNOWN_APP_SOURCES:
                if (currentOsVersion < O) {
                    listener.onDenied(special);
                    return;
                }
                break;
            case SYSTEM_ALERT:
            case NOTIFICATION:
            default:
                if (currentOsVersion < KITKAT) {
                    listener.onDenied(special);
                    return;
                }
                break;
        }
        requestSpecialPermission(special, listener);
    }

    /**
     * 获得全局applicationContext
     */
    public Context getContext() {
        return globalContext;
    }

    /**
     * 提供当前栈顶可用的Activity
     *
     * @return the top Activity in your app
     */
    @Nullable
    @CheckResult
    public Activity getTopActivity() {
        Activity result = null;
        try {
            result = lifecycle.getActivity();
        } catch (Exception e) {
            if (PermissionDebug.isDebug()) {
                PermissionTools.toast(getContext(), e.toString());
                Log.e(TAG, e.toString());
            }
        }
        return result;
    }

    /**
     * 到系统权限设置页，已经适配部分手机系统，逐步更新
     */
    public void goPermissionSettings() {
        PermissionTools.jumpPermissionPage(getContext());
    }

    void autoInit(Application application) {
        if (null != globalContext) {
            return;
        }
        globalContext = application;
        registerLifecycle(globalContext);
    }

    private SoulPermission() {
    }

    private void registerLifecycle(Application context) {
        if (null != lifecycle) {
            context.unregisterActivityLifecycleCallbacks(lifecycle);
        }
        lifecycle = new PermissionActivityLifecycle();
        context.registerActivityLifecycleCallbacks(lifecycle);
    }

    /**
     * 筛选出被拒绝的权限
     */
    private com.library.permission.bean.Permission[] filterRefusedPermissions(com.library.permission.bean.Permission[] in) {
        final List<com.library.permission.bean.Permission> out = new LinkedList<>();
        for (com.library.permission.bean.Permission permission : in) {
            boolean isPermissionOk = permission.isGranted();
            //add refused permission
            if (!isPermissionOk) {
                out.add(permission);
            }
        }
        PermissionDebug.d(TAG, "refusedPermissionList.size" + out.size());
        return PermissionTools.convert(out);
    }

    /**
     * 是否满足请求运行时权限的条件
     */
    private boolean canRequestRunTimePermission() {
        return !PermissionTools.isOldPermissionSystem(getContext());
    }

    private boolean checkPermission(Context context, String permission) {
        return CheckerFactory.create(context, permission).check();
    }

    private void checkStatusBeforeDoSomething(final CheckStatusCallBack callBack) {
        //check container status
        final Activity activity;
        try {
            activity = lifecycle.getActivity();
        } catch (Exception e) {
            //activity status error do not request
            if (PermissionDebug.isDebug()) {
                PermissionTools.toast(getContext(), e.toString());
                Log.e(TAG, e.toString());
            }
            callBack.onStatusError();
            return;
        }
        //check MainThread
        if (!PermissionTools.assertMainThread()) {
            PermissionDebug.w(TAG, "do not request permission in other thread");
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    callBack.onStatusOk(activity);
                }
            });
            return;
        }
        //can do
        callBack.onStatusOk(activity);
    }

    private void requestPermissions(final Permissions permissions,
                                    final CheckRequestPermissionsListener listener) {
        checkStatusBeforeDoSomething(new CheckStatusCallBack() {
            @Override
            public void onStatusOk(Activity activity) {
                requestRuntimePermission(activity, permissions.getPermissions(), listener);
            }

            @Override
            public void onStatusError() {
                //do nothing
            }
        });
    }

    private void requestRuntimePermission(final Activity activity,
                                          final com.library.permission.bean.Permission[] permissionsToRequest, final CheckRequestPermissionsListener listener) {
        PermissionDebug.d(TAG, "start to request permissions size= " + permissionsToRequest.length);
        new PermissionRequester(activity)
                .withPermission(permissionsToRequest)
                .request(new RequestPermissionListener() {
                    @Override
                    public void onPermissionResult(com.library.permission.bean.Permission[] permissions) {
                        //this list contains all the refused permissions after request
                        List<Permission> refusedListAfterRequest = new LinkedList<>();
                        for (Permission requestResult : permissions) {
                            if (!requestResult.isGranted()) {
                                refusedListAfterRequest.add(requestResult);
                            }
                        }
                        if (refusedListAfterRequest.size() == 0) {
                            PermissionDebug.d(TAG, "all permission are request ok");
                            listener.onAllPermissionGranted(permissionsToRequest);
                        } else {
                            PermissionDebug.d(TAG,
                                    "some permission are refused size=" + refusedListAfterRequest.size());
                            listener.onPermissionDenied(PermissionTools.convert(refusedListAfterRequest));
                        }
                    }
                });
    }

    private void requestSpecialPermission(final Special specialPermission,
                                          final SpecialPermissionListener listener) {
        checkStatusBeforeDoSomething(new CheckStatusCallBack() {
            @Override
            public void onStatusOk(Activity activity) {
                new PermissionRequester(activity)
                        .withPermission(specialPermission)
                        .request(listener);
            }

            @Override
            public void onStatusError() {
                //do nothing
            }
        });
    }
}
