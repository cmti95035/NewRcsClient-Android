/**
 * Copyright (c) 2011 Baidu.com, Inc. All Rights Reserved
 */
package mobisocial.musubi.cloudstorage;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

import com.baidu.oauth.BaiduOAuth.BaiduOAuthResponse;



public class AccessTokenManager implements Parcelable {

    private static final String BAIDU_SDK_CONFIG = "baidu_sdk_config";

    private static final String BAIDU_SDK_CONFIG_PROP_ACCESS_TOKEN = "baidu_sdk_config_prop_access_token";

    private static final String BAIDU_SDK_CONFIG_PROP_CREATE_TIME = "baidu_sdk_config_prop_create_time";

    private static final String BAIDU_SDK_CONFIG_PROP_EXPIRE_SECONDS = "baidu_sdk_config_prop_expire_secends";

    private static final String KEY_ACCESS_TOKEN = "baidu_token_manager_access_token";

    private static final String KEY_EXPIRE_TIME = "baidu_token_manager_expire_time";


    private static String accessToken = null;

    private static long expireTime = 0;

    private static Context context = null;


    public AccessTokenManager(Context mcontext) {
        context = mcontext;
        compareWithConfig();
    }

    public AccessTokenManager(Parcel source) {
        Bundle bundle = Bundle.CREATOR.createFromParcel(source);
        if (bundle != null) {
            this.accessToken = bundle.getString(KEY_ACCESS_TOKEN);
            this.expireTime = bundle.getLong(KEY_EXPIRE_TIME);
        }
        compareWithConfig();
    }


    private static void compareWithConfig() {
        if (context == null) {
            return;
        }

        final SharedPreferences sp = context.getSharedPreferences(BAIDU_SDK_CONFIG,
                Context.MODE_PRIVATE);
        sp.registerOnSharedPreferenceChangeListener(new OnSharedPreferenceChangeListener() {

            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                String acToken = sp.getString(BAIDU_SDK_CONFIG_PROP_ACCESS_TOKEN, null);
                if (accessToken != null && !accessToken.equals(acToken)) {
                        initToken(context);                   
                }
            }
        });

    }

    protected static void initToken(Context mcontext) {
    	context = mcontext;
        compareWithConfig();
        SharedPreferences sp = context.getSharedPreferences(BAIDU_SDK_CONFIG, Context.MODE_PRIVATE);
        if (sp == null) {
            return;
        }
        accessToken = sp.getString(BAIDU_SDK_CONFIG_PROP_ACCESS_TOKEN, null);      
        long expires = sp.getLong(BAIDU_SDK_CONFIG_PROP_EXPIRE_SECONDS, 0);
        long createTime = sp.getLong(BAIDU_SDK_CONFIG_PROP_CREATE_TIME, 0);
        long current = System.currentTimeMillis();
        expireTime = createTime + expires;
        if (expireTime != 0 && expireTime < current) {
            clearToken();
        }

    }

    protected static void clearToken() {
        Editor editor = context.getSharedPreferences(BAIDU_SDK_CONFIG, Context.MODE_PRIVATE).edit();
        editor.remove(BAIDU_SDK_CONFIG_PROP_ACCESS_TOKEN);
        editor.remove(BAIDU_SDK_CONFIG_PROP_CREATE_TIME);
        editor.remove(BAIDU_SDK_CONFIG_PROP_EXPIRE_SECONDS);
        editor.commit();
        accessToken = null;
        expireTime = 0;
    }

    protected void storeToken(Bundle values) {
        if (values == null || values.isEmpty()) {
            return;
        }
        accessToken = values.getString("access_token");
        long expiresIn = Long.parseLong(values.getString("expires_in"));
        expireTime = System.currentTimeMillis() + expiresIn;
        Editor editor = context.getSharedPreferences(BAIDU_SDK_CONFIG, Context.MODE_PRIVATE).edit();
        editor.putString(BAIDU_SDK_CONFIG_PROP_ACCESS_TOKEN, accessToken);
        editor.putLong(BAIDU_SDK_CONFIG_PROP_CREATE_TIME, System.currentTimeMillis());
        editor.putLong(BAIDU_SDK_CONFIG_PROP_EXPIRE_SECONDS, expiresIn);
        editor.commit();

    }
    protected static void storeToken(BaiduOAuthResponse values) {
        if (values == null) {
            return;
        }
        accessToken = values.getAccessToken();
        long expiresIn = Long.parseLong(values.getExpiresIn());
        expireTime = System.currentTimeMillis() + expiresIn;
        Editor editor = context.getSharedPreferences(BAIDU_SDK_CONFIG, Context.MODE_PRIVATE).edit();
        editor.putString(BAIDU_SDK_CONFIG_PROP_ACCESS_TOKEN, accessToken);
        editor.putLong(BAIDU_SDK_CONFIG_PROP_CREATE_TIME, System.currentTimeMillis());
        editor.putLong(BAIDU_SDK_CONFIG_PROP_EXPIRE_SECONDS, expiresIn);
        editor.commit();

    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        Bundle bundle = new Bundle();
        if (this.accessToken != null) {
            bundle.putString(KEY_ACCESS_TOKEN, this.accessToken);
        }
        if (this.expireTime != 0) {
            bundle.putLong(KEY_EXPIRE_TIME, this.expireTime);
        }
        bundle.writeToParcel(dest, flags);
    }

    public static final Parcelable.Creator<AccessTokenManager> CREATOR = new Parcelable.Creator<AccessTokenManager>() {

        @Override
        public AccessTokenManager createFromParcel(Parcel source) {
            return new AccessTokenManager(source);
        }

        @Override
        public AccessTokenManager[] newArray(int size) {
            return new AccessTokenManager[size];
        }

    };

    protected static boolean isSessionVaild(Context mContext)  {
        if (accessToken == null || expireTime == 0) {
            initToken(mContext);
        }
	    return accessToken != null && expireTime != 0
			    && System.currentTimeMillis() < expireTime;
    }

    public static String getAccessToken() {
        if (accessToken == null) {
            initToken(context);
        }
        return accessToken;
    }

}
