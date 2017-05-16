package com.demo.floatwindowdemo;

import android.app.AppOpsManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;

import com.socks.library.KLog;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements OnClickListener {
    private static final int REQUEST_CODE_12 = 12;
    private Handler mHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.start_float_window).setOnClickListener(this);

        if (!isFloatWindowOpAllowed(this)) {
            Toast.makeText(this, "无悬浮窗权限，即将跳转权限设置", Toast.LENGTH_SHORT).show();
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    toSettingActivity();
                }
            }, 3000);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.start_float_window:
                List<String> stringList = new ArrayList<>();
                stringList.add("Xiaomi");
                stringList.add("Meizu");
                stringList.add("HUAWEI");

                if (stringList.contains(Build.MANUFACTURER)) {
                    if (isFloatWindowOpAllowed(this)) {//已经开启
                        switchActivity();
                    } else {
                        toSettingActivity();
                    }
                } else {//其他手机
                    if (Build.VERSION.SDK_INT >= 23 && !Settings.canDrawOverlays(this)) {
                        Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
                        startActivityForResult(intent, REQUEST_CODE_12);
                    } else {
                        switchActivity();
                    }
                }
                break;
            default:
                break;
        }
    }

    /**
     * 跳转到miui的权限管理页面
     */
    private void gotoMiuiPermission() {
        Intent i = new Intent("miui.intent.action.APP_PERM_EDITOR");
        ComponentName componentName = new ComponentName("com.miui.securitycenter", "com.miui.permcenter.permissions.AppPermissionsEditorActivity");
        i.setComponent(componentName);
        i.putExtra("extra_pkgname", getPackageName());
        try {
            startActivity(i);
        } catch (Exception e) {
            e.printStackTrace();
            gotoMeizuPermission();
        }
    }

    /**
     * 华为的权限管理页面
     */
    private void gotoHuaweiPermission() {
        try {
            Intent intent = new Intent();
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            ComponentName comp = new ComponentName("com.huawei.systemmanager", "com.huawei.permissionmanager.ui.MainActivity");//华为权限管理
            intent.setComponent(comp);
            startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
            startActivity(getAppDetailSettingIntent());
        }

    }

    /**
     * 跳转到魅族的权限管理系统
     */
    private void gotoMeizuPermission() {
        Intent intent = new Intent("com.meizu.safe.security.SHOW_APPSEC");
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        intent.putExtra("packageName", BuildConfig.APPLICATION_ID);
        try {
            startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
            startActivity(getAppDetailSettingIntent());
        }
    }

    /**
     * 华为手机管家通知管理页面
     * com.huawei.systemmanager/com.huawei.notificationmanager.ui.NotificationManagmentActivity
     */
    public void goNotificationManager() {
        try {
            Intent intent = new Intent("com.vincent.julie");
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            ComponentName comp = new ComponentName("com.huawei.systemmanager", "com.huawei.notificationmanager.ui.NotificationManagmentActivity");
            intent.setComponent(comp);
            startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
            startActivity(getAppDetailSettingIntent());
        }
    }

    /**
     * 获取应用详情页面intent
     *
     * @return
     */
    private Intent getAppDetailSettingIntent() {
        Intent localIntent = new Intent();
        localIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        localIntent.setAction("android.settings.APPLICATION_DETAILS_SETTINGS");
        localIntent.setData(Uri.fromParts("package", getPackageName(), null));
        return localIntent;
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_12) {
            if (Build.VERSION.SDK_INT >= 23) {
                if (!Settings.canDrawOverlays(this)) {
                    Toast.makeText(this, "权限授予失败,无法开启悬浮窗", Toast.LENGTH_SHORT).show();
                } else {
                    switchActivity();
                }
            }
        }
    }

    /**
     * 跳转Activity
     */
    private void switchActivity() {
        startService(new Intent(MainActivity.this, FloatWindowService.class));
        finish();
    }

    /**
     * 判断悬浮窗权限
     *
     * @param context
     * @return
     */
    public static boolean isFloatWindowOpAllowed(Context context) {
        if (Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
            AppOpsManager manager = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
            try {
                Method method = manager.getClass().getDeclaredMethod("checkOp", int.class, int.class, String.class);
                int property = (Integer) method.invoke(manager, 24, Binder.getCallingUid(), context.getPackageName());
                return AppOpsManager.MODE_ALLOWED == property;
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            if ((context.getApplicationInfo().flags & 1 << 27) == 1 << 27) {
                return true;
            }
        }
        return false;
    }

    public void toSettingActivity() {
        if (TextUtils.equals(Build.MANUFACTURER, "HUAWEI")) {
            KLog.e("打开华为权限管理");
//            goNotificationManager();
            gotoHuaweiPermission();
        } else if (TextUtils.equals(Build.MANUFACTURER, "Meizu")) {
            KLog.e("打开魅族权限管理");
            gotoMeizuPermission();
        } else if (TextUtils.equals(Build.MANUFACTURER, "Xiaomi")) {
            KLog.e("打开小米权限管理");
            gotoMiuiPermission();
        } else {
            KLog.e("打开应用详情页");
            startActivity(getAppDetailSettingIntent());
        }
    }
}
