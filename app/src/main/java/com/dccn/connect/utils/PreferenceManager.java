package com.dccn.connect.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import com.dccn.connect.models.User;
import com.google.gson.Gson;

/**
 * PreferenceManager - Handles local data storage using SharedPreferences
 */
public class PreferenceManager {
    
    private static final String PREF_NAME = "DCCNConnectPrefs";
    private static final String KEY_USER = "current_user";
    private static final String KEY_DEVICE_ID = "device_id";
    private static final String KEY_FIRST_LAUNCH = "first_launch";
    
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private Gson gson;
    
    public PreferenceManager(Context context) {
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        gson = new Gson();
    }
    
    /**
     * Save current user to preferences
     */
    public void saveUser(User user) {
        if (user != null) {
            String userJson = gson.toJson(user);
            editor.putString(KEY_USER, userJson);
            editor.apply();
        }
    }
    
    /**
     * Get current user from preferences
     */
    public User getUser() {
        String userJson = sharedPreferences.getString(KEY_USER, null);
        if (!TextUtils.isEmpty(userJson)) {
            try {
                return gson.fromJson(userJson, User.class);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }
    
    /**
     * Clear current user data
     */
    public void clearUser() {
        editor.remove(KEY_USER);
        editor.apply();
    }
    
    /**
     * Check if user is logged in
     */
    public boolean isUserLoggedIn() {
        return getUser() != null;
    }
    
    /**
     * Save device ID
     */
    public void saveDeviceId(String deviceId) {
        editor.putString(KEY_DEVICE_ID, deviceId);
        editor.apply();
    }
    
    /**
     * Get device ID
     */
    public String getDeviceId() {
        return sharedPreferences.getString(KEY_DEVICE_ID, null);
    }
    
    /**
     * Check if this is the first app launch
     */
    public boolean isFirstLaunch() {
        return sharedPreferences.getBoolean(KEY_FIRST_LAUNCH, true);
    }
    
    /**
     * Mark first launch as completed
     */
    public void setFirstLaunchCompleted() {
        editor.putBoolean(KEY_FIRST_LAUNCH, false);
        editor.apply();
    }
    
    /**
     * Clear all preferences
     */
    public void clearAll() {
        editor.clear();
        editor.apply();
    }
    
    /**
     * Save a string value
     */
    public void saveString(String key, String value) {
        editor.putString(key, value);
        editor.apply();
    }
    
    /**
     * Get a string value
     */
    public String getString(String key, String defaultValue) {
        return sharedPreferences.getString(key, defaultValue);
    }
    
    /**
     * Save a boolean value
     */
    public void saveBoolean(String key, boolean value) {
        editor.putBoolean(key, value);
        editor.apply();
    }
    
    /**
     * Get a boolean value
     */
    public boolean getBoolean(String key, boolean defaultValue) {
        return sharedPreferences.getBoolean(key, defaultValue);
    }
    
    /**
     * Save an integer value
     */
    public void saveInt(String key, int value) {
        editor.putInt(key, value);
        editor.apply();
    }
    
    /**
     * Get an integer value
     */
    public int getInt(String key, int defaultValue) {
        return sharedPreferences.getInt(key, defaultValue);
    }
    
    /**
     * Save a long value
     */
    public void saveLong(String key, long value) {
        editor.putLong(key, value);
        editor.apply();
    }
    
    /**
     * Get a long value
     */
    public long getLong(String key, long defaultValue) {
        return sharedPreferences.getLong(key, defaultValue);
    }
}

