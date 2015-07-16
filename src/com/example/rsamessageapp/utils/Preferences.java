package com.example.rsamessageapp.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.telephony.TelephonyManager;

public class Preferences {

    public static final String SHARED_PREFERENCES = "SHARED_PREFERENCES";
    public static final String RSA_GENERATED = "com.example.RSA_GENERATED";
    public static final String RSA_PUBLIC_KEY = "com.example.RSA_PUBLIC_KEY";
    public static final String RSA_PRIVATE_KEY = "com.example.RSA_PRIVATE_KEY";
    public static final String ENCRYPTED_MESSAGE = "com.example.ENCRYPTED_MESSAGE";
    public static final String FIRST_RUN = "com.example.FIRST_RUN";
    //public static final String BASE_URL = "http://192.168.0.106:8080/RSAMessageServer/rest/services/";
    public static final String BASE_URL = "https://evening-waters-8872.herokuapp.com/services/";

    public static SharedPreferences mPreferences;
    
    private static Context mContext;

    public static void init(Context context) {
    	mContext = context;
        mPreferences = context.getSharedPreferences(SHARED_PREFERENCES, 0);
    }

    public static boolean getBoolean(String key) {
        return mPreferences.getBoolean(key, false);
    }

    public static boolean getBoolean(String key, boolean defaultValue) {
        return mPreferences.getBoolean(key, defaultValue);
    }

    public static void putBoolean(String key, boolean bool) {
        mPreferences.edit().putBoolean(key, bool).commit();
    }

    public static void putString(String key, String s) {
        mPreferences.edit().putString(key, s).commit();
    }

    public static void putInteger(String key, Integer integer) {
        mPreferences.edit().putInt(key, integer).commit();
    }

    public static void clear() {
        mPreferences.edit().clear().commit();
    }

    public static void remove(String key) {
        mPreferences.edit().remove(key).commit();
    }

    public static String getString(String key) {
        return mPreferences.getString(key, null);
    }

    public static int getInteger(String key) {
        return mPreferences.getInt(key, 0);
    }
    
    public static String getPhoneNumber(){
    	TelephonyManager tMgr = (TelephonyManager)mContext.getSystemService(Context.TELEPHONY_SERVICE);
    	String mPhoneNumber = tMgr.getLine1Number();
    	
    	return mPhoneNumber;
    }

}
