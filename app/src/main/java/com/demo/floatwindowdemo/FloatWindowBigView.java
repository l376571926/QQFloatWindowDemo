package com.demo.floatwindowdemo;

import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.LinearLayout;
import android.widget.TimePicker;

import com.socks.library.KLog;

public class FloatWindowBigView extends LinearLayout implements View.OnClickListener, TimePickerDialog.OnTimeSetListener {

    private Context context;
    /**
     * 记录大悬浮窗的宽度
     */
    public static int viewWidth;

    /**
     * 记录大悬浮窗的高度
     */
    public static int viewHeight;

    public FloatWindowBigView(final Context context) {
        super(context);
        this.context = context;
        LayoutInflater.from(context).inflate(R.layout.float_window_big, this);
        View view = findViewById(R.id.big_window_layout);
        viewWidth = view.getLayoutParams().width;
        viewHeight = view.getLayoutParams().height;
        findViewById(R.id.close).setOnClickListener(this);
        findViewById(R.id.back).setOnClickListener(this);

        findViewById(R.id.changeInputMethodBtn).setOnClickListener(this);

        findViewById(R.id.button1).setOnClickListener(this);
        findViewById(R.id.button2).setOnClickListener(this);
//        findViewById(R.id.button3).setOnClickListener(this);
        findViewById(R.id.button4).setOnClickListener(this);
        findViewById(R.id.button5).setOnClickListener(this);
        findViewById(R.id.button6).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        Intent intent = null;
        switch (v.getId()) {
            case R.id.close:
                // 点击关闭悬浮窗的时候，移除所有悬浮窗，并停止Service
                MyWindowManager.removeBigWindow(context);
                MyWindowManager.removeSmallWindow(context);
                intent = new Intent(getContext(), FloatWindowService.class);
                context.stopService(intent);
                break;
            case R.id.back:
                // 点击返回的时候，移除大悬浮窗，创建小悬浮窗
                MyWindowManager.removeBigWindow(context);
                MyWindowManager.createSmallWindow(context);
                break;
            case R.id.changeInputMethodBtn://切换输入法
                ((InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE)).showInputMethodPicker();
                break;
            case R.id.button1:
                intent = new Intent(Settings.ACTION_DATE_SETTINGS);//设置系统时间
                break;
            case R.id.button2:
                intent = new Intent(Settings.ACTION_LOCALE_SETTINGS);//切换系统语言
                break;
//            case R.id.button3:
//                intent = new Intent(Settings.ACTION_INPUT_METHOD_SETTINGS);//设置可用的输入法
//                break;
            case R.id.button4:
                intent = new Intent(Settings.ACTION_APPLICATION_SETTINGS);// 跳转到应用列表
                break;
            case R.id.button5:
                intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);//  跳转到辅助功能
                break;
            case R.id.button6:
                intent = new Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS);//开发者选项
                break;
            default:
                break;
        }
        if (intent != null && v.getId() != R.id.close) {
            // 点击返回的时候，移除大悬浮窗，创建小悬浮窗
            MyWindowManager.removeBigWindow(context);
            MyWindowManager.createSmallWindow(context);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            getContext().startActivity(intent);
        }
    }

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        KLog.e(view + " " + hourOfDay + " " + minute);
    }
}
