package com.darkgem.framework.io.kv;

import android.content.Context;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.darkgem.framework.support.kit.FileKit;
import com.darkgem.framework.support.kv.KvStore;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Key-Value 存储
 */
public class KvIo {
    /**
     * 基础类
     */
    static abstract class AbsKv {
        //命名空间
        enum Bucket {
            PROFILE("PROFILE");
            String name;

            Bucket(String name) {
                this.name = name;
            }

            public String getName() {
                return name;
            }
        }

        KvStore kvStore;

        public void init(KvStore kvStore) {
            this.kvStore = kvStore;
        }

        /**
         * 提供JSON支持
         */
        protected JSONObject get(String key) {
            String value = kvStore.get(getKey(key));
            if (value != null) {
                return JSON.parseObject(value);
            } else {
                return new JSONObject();
            }
        }

        /**
         * 提供JSON支持
         */
        protected void put(String key, JSONObject jsonObject) {
            kvStore.put(getKey(key), jsonObject.toJSONString());
        }

        /**
         * 提供JSON支持
         */
        protected void remove(String key) {
            kvStore.remove(getKey(key));
        }

        /**
         * 获取实现对象的Bucket类型
         */
        abstract protected Bucket getBucket();

        private String getKey(String key) {
            return String.format("%s:%s", getBucket().getName(), key);
        }
    }

    /**
     * 系统配置类型的处理工具
     */
    public static class ProfileKv extends AbsKv {

        @Override
        protected Bucket getBucket() {
            return Bucket.PROFILE;
        }

        //KEY
        class KEY {
            /**
             * 最后清空缓存时间
             * {
             * time:Long
             * }
             */
            static final String LAST_TIME_FOR_CLEAR_CACHE = "LAST_TIME_FOR_CLEAR_CACHE";
            /**
             * 最后同步版本信息时间
             * {
             * time:Long
             * }
             */
            static final String LAST_TIME_FOR_SYNC_APP_VERSION = "LAST_TIME_FOR_SYNC_APP_VERSION";
        }

        /**
         * 获取最后一次清空缓存时间
         */
        public long getLastTimeForClearCache() {
            JSONObject jsonObject = get(KEY.LAST_TIME_FOR_CLEAR_CACHE);
            Long time = jsonObject.getLong("time");
            return time != null ? time : 0l;
        }

        /**
         * 设置最后一次清空缓存时间
         */
        public void setLastTimeForClearCache(long time) {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("time", time);
            put(KEY.LAST_TIME_FOR_CLEAR_CACHE, jsonObject);
        }

        /**
         * 获取App检测检测时间戳
         */
        public long getLastTimeForSyncAppVersion() {
            JSONObject jsonObject = get(KEY.LAST_TIME_FOR_SYNC_APP_VERSION);
            Long time = jsonObject.getLong("time");
            return time != null ? time : 0l;
        }

        /**
         * 设置App检测检测时间戳
         */
        public void setLastTimeForSyncAppVersion(long time) {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("time", time);
            put(KEY.LAST_TIME_FOR_SYNC_APP_VERSION, jsonObject);
        }

    }

    //密码
    final static String PASSWORD = "www.android.com";
    //数据库名称
    final static String DB_NAME = "key_value.db";
    static volatile KvIo instance;
    //上下文
    Context context;
    //KV
    Map<Class<? extends AbsKv>, AbsKv> kvMap = new HashMap<Class<? extends AbsKv>, AbsKv>();

    public KvIo(Context context) {
        this.context = context;
        //初始化KV数据库
        KvStore kvStore = new KvStore(context, FileKit.getAppDirInMain(context, FileKit.AppDirTypeInMain.DB) + File.separator + DB_NAME, PASSWORD);
        //配置 需要加载的AbsKv
        List<Class<? extends AbsKv>> clzList = new ArrayList<Class<? extends AbsKv>>();
        //添加注册的类型
        {
            clzList.add(ProfileKv.class);
        }
        for (Class<? extends AbsKv> clz : clzList) {
            try {
                AbsKv kv = clz.newInstance();
                kv.init(kvStore);
                kvMap.put(clz, kv);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    static public KvIo getInstance(Context context) {
        if (instance == null) {
            synchronized (KvIo.class) {
                if (instance == null) {
                    instance = new KvIo(context);
                }
            }
        }
        return instance;
    }

    /**
     * 获取某一个空间下的KV
     */
    public <T extends AbsKv> T getKv(Class<T> t) {
        return (T) kvMap.get(t);
    }
}
