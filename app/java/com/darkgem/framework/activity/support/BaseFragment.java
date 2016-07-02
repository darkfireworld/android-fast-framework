package com.darkgem.framework.activity.support;

import android.app.Activity;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

abstract public class BaseFragment extends Fragment {
    /**
     * 显示TOAST
     */
    public void showToast(final String text) {
        final Activity activity = getActivity();
        if (!TextUtils.isEmpty(text) && activity != null) {
            activity.runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    Toast.makeText(activity.getApplicationContext(), text, Toast.LENGTH_SHORT).show();
                }
            });

        }
    }

    /**
     * 显示TOAST
     */
    public void showToast(final int resId) {
        final Activity activity = getActivity();
        if (resId > 0 && activity != null) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(activity.getApplicationContext(), resId, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    /**
     * 隐藏软键盘输入
     */
    public void hideSoftKeyboard() {
        final Activity activity = getActivity();
        if (activity != null) {
            InputMethodManager manager = ((InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE));
            if (manager != null && activity.getCurrentFocus() != null) {
                manager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), InputMethodManager.RESULT_UNCHANGED_SHOWN);
            }
        }

    }

    /**
     * 显示输入键盘
     */
    public void showSoftKeyboard(View view) {
        final Activity activity = getActivity();
        if (activity != null) {
            if (view != null) {
                view.requestFocus();
                InputMethodManager manager = ((InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE));
                if (manager != null) {
                    manager.showSoftInput(view, InputMethodManager.RESULT_UNCHANGED_SHOWN);
                }
            }
        }
    }
}
