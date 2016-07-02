package com.darkgem.framework.io.mcr;

import android.content.ContentResolver;
import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.os.Handler;
import android.os.Looper;
import android.provider.ContactsContract;
import android.util.Log;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * 读取手机通讯录
 */
public class McrIo {
    static McrIo instance;
    Context context;
    List<Mcr> mcrList = new LinkedList<Mcr>();
    Map<String, Mcr> mcrMap = new HashMap<String, Mcr>();

    public McrIo(Context context) {
        this.context = context;
        //注册监听器
        this.context.getContentResolver().registerContentObserver(ContactsContract.Contacts.CONTENT_URI, true,
                new ContentObserver(new Handler(Looper.getMainLooper())) {
                    @Override
                    public void onChange(boolean selfChange) {
                        super.onChange(selfChange);
                        refresh();
                    }
                });
    }

    public static synchronized McrIo getInstance(Context context) {
        if (instance == null)
            instance = new McrIo(context);
        return instance;
    }

    private synchronized void refresh() {
        //清空原先数据
        mcrList.clear();
        mcrMap.clear();
        //读取手机通讯录
        {
            Cursor cursor = null;
            try {
                ContentResolver resolver = context.getContentResolver();
                cursor = resolver.query(
                        ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                        new String[]{ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME, ContactsContract.CommonDataKinds.Phone.NUMBER},
                        null,
                        null,
                        null);
                if (cursor == null || cursor.getCount() == 0) {
                    return;
                }

                int PHONES_NUMBER_INDEX = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
                int PHONES_DISPLAY_NAME_INDEX = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);

                if (cursor.getCount() > 0) {

                    while (cursor.moveToNext()) {
                        String number = cursor.getString(PHONES_NUMBER_INDEX);
                        String name = cursor.getString(PHONES_DISPLAY_NAME_INDEX);
                        //判空，避免读取到错误的数据
                        if (number == null || name == null) {
                            continue;
                        }
                        //替换所有的空格
                        number = number.replace(" ", "");
                        //去除首位空格
                        number = number.trim();
                        //剔除+86
                        if (number.startsWith("+86")) {
                            number = number.substring(3);
                        }
                        if (number.matches("^\\d{11}$")) {
                            if (mcrMap.get(number) == null) {
                                Mcr mcr = new Mcr(number, name);
                                mcrList.add(mcr);
                                mcrMap.put(number, mcr);
                            }
                        }
                    }
                }

            } catch (Exception e) {
                Log.e(McrIo.class.getName(), e.getMessage(), e);
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
        }
    }

    /**
     * 获取手机通讯录， 如果返回size = 0， 则表示没有权限，或者真的为空
     */
    public synchronized List<Mcr> selectMcrList() {
        if (mcrList == null || mcrList.size() == 0) {
            refresh();
        }
        //拷贝，避免多线程不一致
        List<Mcr> ret = new LinkedList<Mcr>();
        for (Mcr mcr : mcrList) {
            ret.add(mcr);
        }
        return ret;
    }

    /**
     * 返回手机号码是否在通讯录中
     */
    public synchronized boolean isMcr(String phone) {
        if (mcrMap.isEmpty()) {
            refresh();
        }
        return mcrMap.get(phone) != null;
    }
}
