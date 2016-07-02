package com.darkgem.framework.activity.splash;

import android.os.Bundle;
import com.darkgem.app.R;
import com.darkgem.framework.activity.support.BaseActivity;
import com.darkgem.framework.io.app.ApplicationIo;

public class SplashActivity extends BaseActivity {
    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        //初始化
        ApplicationIo.getInstance(this).tryInitializeCore();
        //登入
        ApplicationIo.getInstance(this).recordToken("18767122350", "ABCD");
    }

    /**
     * 避免检测，因为是第一个页面
     */
    @Override
    protected void checkApplicationInitialized() {
        //初始化该页面
        ActivityManager.initialize();
    }
}
