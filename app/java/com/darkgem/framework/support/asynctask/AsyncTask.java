package com.darkgem.framework.support.asynctask;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import com.darkgem.framework.support.kit.ThreadPoolKit;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 任务
 */
abstract public class AsyncTask<Param, Progress, Result> {

    protected abstract Result doInBackground(Param... args);

    protected void onPostExecute(Result result) {

    }

    protected void onProgressUpdate(Progress... pg) {
    }

    public void execute(Param... args) {
        ThreadManager.getInstance().run(this, args);
    }

    /**
     * Created by Administrator on 2015/6/24.
     */
    public static class ThreadManager {
        static ThreadManager instance;
        ExecutorService pool;
        Handler handler;

        public ThreadManager() {
            //无上限线程池
            this.pool = ThreadPoolKit.createThreadPoolExecutor(0, Integer.MAX_VALUE, 60l, TimeUnit.SECONDS, "app-async-task");
            this.handler = new Handler(Looper.getMainLooper());
        }

        static synchronized public ThreadManager getInstance() {
            if (instance == null)
                instance = new ThreadManager();
            return instance;
        }

        /**
         * 运行任务
         */
        private void run(final AsyncTask task, final Object... args) {
            pool.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        final Object result = task.doInBackground(args);
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                task.onPostExecute(result);
                            }
                        });
                    } catch (Exception e) {
                        Log.e(ThreadManager.class.getName(), e.getMessage(), e);
                    }
                }
            });
        }

    }
}
