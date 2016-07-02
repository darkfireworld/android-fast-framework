package com.darkgem.framework.support.kv;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Base64;
import android.util.Log;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * SQLite 数据，存储到数据库中
 */
public class KvStore {
    //上下文
    Context context;
    //sql
    KvSQLiteHelper sqLiteHelper;
    //缓存
    Map<String, String> cache = new ConcurrentHashMap<String, String>();

    /**
     * 创建一个 KV 数据库存储
     */
    public KvStore(Context context, String db, String password) {
        this.context = context;
        this.sqLiteHelper = new KvSQLiteHelper(context, db, password);
    }

    /**
     * 通过KEY，获取VALUE
     */
    @Nullable
    public synchronized String get(@NonNull String key) {
        //读取缓存
        String value = cache.get(key);
        if (value == null) {
            value = sqLiteHelper.get(key);
            if (value != null) {
                cache.put(key, value);
            }
        }
        return value;
    }

    /**
     * 插入一条记录
     */
    public synchronized void put(@NonNull String key, @NonNull String value) {
        //存储到数据库
        sqLiteHelper.put(key, value);
        //保存到cache
        cache.put(key, value);

    }

	/**
     * 删除
     */
    public synchronized void remove(@NonNull String key) {
        //清除数据库
        sqLiteHelper.remove(key);
        //清空Cache
        cache.remove(key);
    }
	
    /**
     * 清空数据库
     */
    public synchronized void clear() {
        sqLiteHelper.clear();
        cache.clear();
    }
	

    /**
     * SQL 打开工具
     */
    public static final class KvSQLiteHelper extends SQLiteOpenHelper {
        //SQL字段
        static class SQL {
            //表名
            @NonNull
            static final String TABLE = "TABLE_KEY_VALUE";
            @NonNull
            static final String KEY = "KEY";
            @Nullable
            static final String VALUE = "VALUE";
        }

        //密钥
        String password;

        public KvSQLiteHelper(Context context, String name, String password) {
            super(context, name, null, 1);
            this.password = password;
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            String sql = String.format("CREATE TABLE %s (" +
                            "%s TEXT NOT NULL ," +
                            "%s TEXT NULL     ," +
                            "PRIMARY KEY(%s)   " +
                            ")",
                    SQL.TABLE,
                    SQL.KEY, SQL.VALUE,
                    SQL.KEY);
            db.execSQL(sql);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        }

        /**
         * 获取VALUE
         */
        @Nullable
        public String get(@NonNull String key) {
            String encryptKey = encrypt(key);
            if (encryptKey == null) {
                return null;
            }
            String encryptValue = null;
            Cursor cursor = null;
            try {
                String sql = String.format("SELECT %s FROM %s WHERE %s = ?",
                        SQL.VALUE,
                        SQL.TABLE,
                        SQL.KEY);
                cursor = getWritableDatabase().rawQuery(sql, new String[]{encryptKey});
                while (cursor.moveToNext()) {
                    encryptValue = cursor.getString(0);
                }
            } finally {
                if (cursor != null)
                    cursor.close();
            }
            return decrypt(encryptValue);
        }

        /**
         * 插入/更新数据
         */
        public void put(@NonNull String key, @NonNull String value) {
            String sql = String.format("INSERT OR REPLACE INTO %s (%s ,%s ) VALUES(?,?)",
                    SQL.TABLE,
                    SQL.KEY, SQL.VALUE);
            String encryptKey = encrypt(key);
            String encryptValue = encrypt(value);
            if (encryptKey != null && encryptValue != null) {
                getWritableDatabase().execSQL(sql, new Object[]{encryptKey, encryptValue});
            }
        }

        /**
         * 删除
         */
        public void remove(@NonNull String key) {
            String sql = String.format("DELETE FROM %s WHERE %s = ?",
                    SQL.TABLE,
                    SQL.KEY);
            String encryptKey = encrypt(key);
            if (encryptKey != null) {
                getWritableDatabase().execSQL(sql, new Object[]{encryptKey});
            }
        }
		
        /**
         * 清空数据库
         */
        public void clear() {
            String sql = String.format("DELETE FROM %s", SQL.TABLE);
            getWritableDatabase().execSQL(sql);
        }

        /**
         * 加密
         */
        @Nullable
        String encrypt(String text) {
            try {
                if (text != null) {
                    return AESCrypt.encrypt(password, text);
                }
            } catch (GeneralSecurityException e) {
                Log.e(KvStore.class.getName(), e.getMessage());
            }
            return null;
        }

        /**
         * 解密
         */
        @Nullable
        String decrypt(String text) {
            try {
                if (text != null) {
                    return AESCrypt.decrypt(password, text);
                }
            } catch (GeneralSecurityException e) {
                Log.e(KvStore.class.getName(), e.getMessage());
            }
            return null;
        }

    }

    /**
     * Encrypt and decrypt messages using AES 256 bit encryption that are compatible with AESCrypt-ObjC and AESCrypt Ruby.
     * <p/>
     * Created by scottab on 04/10/2014.
     */
    public static final class AESCrypt {

        private static final String TAG = "AESCrypt";

        //AESCrypt-ObjC uses CBC and PKCS7Padding
        private static final String AES_MODE = "AES/CBC/PKCS7Padding";
        private static final String CHARSET = "UTF-8";

        //AESCrypt-ObjC uses SHA-256 (and so a 256-bit key)
        private static final String HASH_ALGORITHM = "SHA-256";

        //AESCrypt-ObjC uses blank IV (not the best security, but the aim here is compatibility)
        private static final byte[] ivBytes = {0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};

        //togglable log option (please turn off in live!)
        public static boolean DEBUG_LOG_ENABLED = false;


        /**
         * Generates SHA256 hash of the password which is used as key
         *
         * @param password used to generated key
         * @return SHA256 of the password
         */
        private static SecretKeySpec generateKey(final String password) throws NoSuchAlgorithmException, UnsupportedEncodingException {
            final MessageDigest digest = MessageDigest.getInstance(HASH_ALGORITHM);
            byte[] bytes = password.getBytes("UTF-8");
            digest.update(bytes, 0, bytes.length);
            byte[] key = digest.digest();

            log("SHA-256 key ", key);

            return new SecretKeySpec(key, "AES");
        }


        /**
         * Encrypt and encode message using 256-bit AES with key generated from password.
         *
         * @param password used to generated key
         * @param message  the thing you want to encrypt assumed String UTF-8
         * @return Base64 encoded CipherText
         * @throws GeneralSecurityException if problems occur during encryption
         */
        public static String encrypt(final String password, String message)
                throws GeneralSecurityException {

            try {
                final SecretKeySpec key = generateKey(password);

                log("message", message);

                byte[] cipherText = encrypt(key, ivBytes, message.getBytes(CHARSET));

                //NO_WRAP is important as was getting \n at the end
                String encoded = Base64.encodeToString(cipherText, Base64.NO_WRAP);
                log("Base64.NO_WRAP", encoded);
                return encoded;
            } catch (UnsupportedEncodingException e) {
                if (DEBUG_LOG_ENABLED)
                    Log.e(TAG, "UnsupportedEncodingException ", e);
                throw new GeneralSecurityException(e);
            }
        }


        /**
         * More flexible AES encrypt that doesn't encode
         *
         * @param key     AES key typically 128, 192 or 256 bit
         * @param iv      Initiation Vector
         * @param message in bytes (assumed it's already been decoded)
         * @return Encrypted cipher text (not encoded)
         * @throws GeneralSecurityException if something goes wrong during encryption
         */
        public static byte[] encrypt(final SecretKeySpec key, final byte[] iv, final byte[] message)
                throws GeneralSecurityException {
            final Cipher cipher = Cipher.getInstance(AES_MODE);
            IvParameterSpec ivSpec = new IvParameterSpec(iv);
            cipher.init(Cipher.ENCRYPT_MODE, key, ivSpec);
            byte[] cipherText = cipher.doFinal(message);

            log("cipherText", cipherText);

            return cipherText;
        }


        /**
         * Decrypt and decode ciphertext using 256-bit AES with key generated from password
         *
         * @param password                used to generated key
         * @param base64EncodedCipherText the encrpyted message encoded with base64
         * @return message in Plain text (String UTF-8)
         * @throws GeneralSecurityException if there's an issue decrypting
         */
        public static String decrypt(final String password, String base64EncodedCipherText)
                throws GeneralSecurityException {

            try {
                final SecretKeySpec key = generateKey(password);

                log("base64EncodedCipherText", base64EncodedCipherText);
                byte[] decodedCipherText = Base64.decode(base64EncodedCipherText, Base64.NO_WRAP);
                log("decodedCipherText", decodedCipherText);

                byte[] decryptedBytes = decrypt(key, ivBytes, decodedCipherText);

                log("decryptedBytes", decryptedBytes);
                String message = new String(decryptedBytes, CHARSET);
                log("message", message);


                return message;
            } catch (UnsupportedEncodingException e) {
                if (DEBUG_LOG_ENABLED)
                    Log.e(TAG, "UnsupportedEncodingException ", e);

                throw new GeneralSecurityException(e);
            }
        }


        /**
         * More flexible AES decrypt that doesn't encode
         *
         * @param key               AES key typically 128, 192 or 256 bit
         * @param iv                Initiation Vector
         * @param decodedCipherText in bytes (assumed it's already been decoded)
         * @return Decrypted message cipher text (not encoded)
         * @throws GeneralSecurityException if something goes wrong during encryption
         */
        public static byte[] decrypt(final SecretKeySpec key, final byte[] iv, final byte[] decodedCipherText)
                throws GeneralSecurityException {
            final Cipher cipher = Cipher.getInstance(AES_MODE);
            IvParameterSpec ivSpec = new IvParameterSpec(iv);
            cipher.init(Cipher.DECRYPT_MODE, key, ivSpec);
            byte[] decryptedBytes = cipher.doFinal(decodedCipherText);

            log("decryptedBytes", decryptedBytes);

            return decryptedBytes;
        }


        private static void log(String what, byte[] bytes) {
            if (DEBUG_LOG_ENABLED)
                Log.d(TAG, what + "[" + bytes.length + "] [" + bytesToHex(bytes) + "]");
        }

        private static void log(String what, String value) {
            if (DEBUG_LOG_ENABLED)
                Log.d(TAG, what + "[" + value.length() + "] [" + value + "]");
        }


        /**
         * Converts byte array to hexidecimal useful for logging and fault finding
         */
        private static String bytesToHex(byte[] bytes) {
            final char[] hexArray = {'0', '1', '2', '3', '4', '5', '6', '7', '8',
                    '9', 'A', 'B', 'C', 'D', 'E', 'F'};
            char[] hexChars = new char[bytes.length * 2];
            int v;
            for (int j = 0; j < bytes.length; j++) {
                v = bytes[j] & 0xFF;
                hexChars[j * 2] = hexArray[v >>> 4];
                hexChars[j * 2 + 1] = hexArray[v & 0x0F];
            }
            return new String(hexChars);
        }

        private AESCrypt() {
        }
    }
}
