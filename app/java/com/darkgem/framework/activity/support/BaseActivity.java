package com.darkgem.framework.activity.support;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;
import com.darkgem.framework.io.app.ApplicationIo;
import com.umeng.analytics.MobclickAgent;

import java.util.Stack;

abstract public class BaseActivity extends FragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //检测基础数据是否初始化完成
        checkApplicationInitialized();
        //竖屏幕
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        //加入堆栈管理
        ActivityManager.getInstance().push(this);
    }

    /**
     * 检测基础数据是否初始化完成
     */
    protected void checkApplicationInitialized() {
        //检测是否初始化完成，如果没有进行初始化，则跳到LaunchUI页面
        //目的是为了防止 App 被Kill，然后从Home返回App，导致Top Activity
        //显示，而Root Activity 没有 被onCreate 的错误，
        //http://stackoverflow.com/questions/14375720/android-destroying-activities-killing-processes
        if (!ActivityManager.initialized()) {
            //重启
            ApplicationIo.getInstance(this).kill(true);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ActivityManager.getInstance().remove(this);
    }

    /**
     * 重写这个方法，避免 Fragment 状态保留
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        //super.onSaveInstanceState(outState);
    }

    /**
     * 显示TOAST
     */
    public void showToast(final String text) {
        if (!TextUtils.isEmpty(text)) {
            runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();
                }
            });

        }
    }

    /**
     * 显示TOAST
     */
    public void showToast(final int resId) {
        if (resId > 0) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getApplicationContext(), resId, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    /**
     * 隐藏软键盘输入
     */
    public void hideSoftKeyboard() {
        InputMethodManager manager = ((InputMethodManager) this.getSystemService(Activity.INPUT_METHOD_SERVICE));
        if (manager != null && getCurrentFocus() != null) {
            manager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.RESULT_UNCHANGED_SHOWN);
        }
    }

    /**
     * 显示输入键盘
     */
    public void showSoftKeyboard(View view) {
        if (view != null) {
            view.requestFocus();
            InputMethodManager manager = ((InputMethodManager) this.getSystemService(Activity.INPUT_METHOD_SERVICE));
            if (manager != null) {
                manager.showSoftInput(view, InputMethodManager.RESULT_UNCHANGED_SHOWN);
            }
        }
    }

    /**
     * Activity 管理类
     */
    public static class ActivityManager {

        //实例
        volatile static ActivityManager instance;
        //Activity 堆栈
        Stack<Activity> activityStack = new Stack<Activity>();

        /**
         * 初始化
         */
        static public void initialize() {
            if (instance == null) {
                synchronized (ApplicationIo.class) {
                    if (instance == null) {
                        instance = new ActivityManager();
                    }
                }
            }
        }

        /**
         * 是否初始化过该IO
         */
        public static synchronized boolean initialized() {
            return instance != null;
        }

        /**
         * 获取对象
         */
        static public ActivityManager getInstance() {
            if (instance == null) {
                throw new RuntimeException(String.format("initialized %s ?", BaseActivity.class.getName()));
            }
            return instance;
        }

        /**
         * 添加一个 Activity
         */
        public void push(Activity activity) {
            activityStack.push(activity);
        }

        /**
         * 删除一个Activity
         */
        public void remove(Activity activity) {
            activityStack.remove(activity);
        }
    }
}
