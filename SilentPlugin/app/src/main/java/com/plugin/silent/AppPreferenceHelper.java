package com.plugin.silent;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;

import java.util.Map;

/**
 * 用于管理Preference数据类
 */
public class AppPreferenceHelper {

    public static final String PREFERENCES = "preference";

    /**
     * key constants
     */
    public class PreferenceKeys {

    }

    private static AppPreferenceHelper mInstance;
    private SharedPreferences mPreferences;
    private Editor mEditor;

    /**
     * 单例获取方法
     *
     * @param context 上下文
     * @return 单例对象
     */
    public synchronized static AppPreferenceHelper getInstance(
            final Context context) {
        if (mInstance == null) {
            mInstance = new AppPreferenceHelper(context);
        }
        return mInstance;
    }

    public SharedPreferences getPreferences() {
        return mPreferences;
    }

    private AppPreferenceHelper(final Context context) {
        mPreferences = context.getSharedPreferences(PREFERENCES,
                Activity.MODE_PRIVATE);
        mEditor = mPreferences.edit();
    }

    public SharedPreferences getPreferences(Context context, String prefName) {
        return context.getSharedPreferences(prefName, Activity.MODE_PRIVATE);
    }

    public Map<String, ?> getAll() {
        return mPreferences.getAll();
    }

    public boolean contains(String key) {
        return mPreferences.contains(key);
    }

    public boolean getBoolean(String key, boolean defValue) {
        return mPreferences.getBoolean(key, defValue);
    }

    public float getFloat(String key, float defValue) {
        return mPreferences.getFloat(key, defValue);
    }

    public int getInt(String key, int defValue) {
        return mPreferences.getInt(key, defValue);
    }

    public long getLong(String key, long defValue) {
        return mPreferences.getLong(key, defValue);
    }

    public String getString(String key, String defValue) {
        return mPreferences.getString(key, defValue);
    }

    public void registerOnSharedPreferenceChangeListener(
            OnSharedPreferenceChangeListener listener) {
        mPreferences.registerOnSharedPreferenceChangeListener(listener);
    }

    public void unregisterOnSharedPreferenceChangeListener(
            OnSharedPreferenceChangeListener listener) {
        mPreferences.unregisterOnSharedPreferenceChangeListener(listener);
    }

    public boolean putBoolean(String key, boolean b) {
        mEditor.putBoolean(key, b);
        return mEditor.commit();
    }

    public boolean putInt(String key, int i) {
        mEditor.putInt(key, i);
        return mEditor.commit();
    }

    public boolean putFloat(String key, float f) {
        mEditor.putFloat(key, f);
        return mEditor.commit();
    }

    public boolean putLong(String key, long l) {
        mEditor.putLong(key, l);
        return mEditor.commit();
    }

    public boolean putString(String key, String s) {
        mEditor.putString(key, s);
        return mEditor.commit();
    }

    /**
     * 移除一个键
     *
     * @param key
     * @return
     */
    public boolean removeKey(String key) {
        mEditor.remove(key);
        return mEditor.commit();
    }

}
