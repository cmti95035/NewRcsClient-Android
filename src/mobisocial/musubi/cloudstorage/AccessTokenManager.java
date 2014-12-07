/**
 * Copyright (c) 2011 Baidu.com, Inc. All Rights Reserved
 */
package mobisocial.musubi.cloudstorage;

import com.baidu.oauth.BaiduOAuth.BaiduOAuthResponse;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;


/**
 * ��Token�����Ϣ�Ĺ����࣬������ʼ�����洢�������Ӧ��token��Ϣ
 * ����AccessTokenManager�漰���ڶ��Activity�д��ݵģ�����ʵ����Parcelable�ӿ�
 * 
 * @author chenhetong(chenhetong@baidu.com)
 * 
 */
public class AccessTokenManager implements Parcelable {

    private static final String BAIDU_SDK_CONFIG = "baidu_sdk_config";

    //�־û�token��Ϣ�ĸ��ּ���
    private static final String BAIDU_SDK_CONFIG_PROP_ACCESS_TOKEN = "baidu_sdk_config_prop_access_token";

    private static final String BAIDU_SDK_CONFIG_PROP_CREATE_TIME = "baidu_sdk_config_prop_create_time";

    private static final String BAIDU_SDK_CONFIG_PROP_EXPIRE_SECONDS = "baidu_sdk_config_prop_expire_secends";

    //�ڶ��Activity�д���AccessTokenManager�ļ�ֵ
    private static final String KEY_ACCESS_TOKEN = "baidu_token_manager_access_token";

    private static final String KEY_EXPIRE_TIME = "baidu_token_manager_expire_time";


    //accessToken��Ϣ
    private String accessToken = null;

    //token����ʱ��
    private long expireTime = 0;

    //��ǰ�������Ļ���
    private Context context = null;

    /**
     * ����AccessTokenManager��
     * 
     * @param context ��ǰ�������Ļ�����ͨ��Ϊ����Activity��this��
     */
    public AccessTokenManager(Context context) {
        this.context = context;
        compareWithConfig();
    }

    /**
     * ͨ��Parcel������AccessTokenManager����Ҫ����Parcelable.Creator��
     * 
     * @param source Parcel ����Ϣ
     *
     */
    public AccessTokenManager(Parcel source) {
        Bundle bundle = Bundle.CREATOR.createFromParcel(source);
        if (bundle != null) {
            this.accessToken = bundle.getString(KEY_ACCESS_TOKEN);
            this.expireTime = bundle.getLong(KEY_EXPIRE_TIME);
        }
        compareWithConfig();
    }

    /**
     * ��鵱token��Ϣ�������ļ��Ƿ񱣳�һ�£�����һ����Ե�ǰ��token��Ϣ���г�ʼ��
     */
    private void compareWithConfig() {
        if (this.context == null) {
            return;
        }
        /**
         * �����õ�Ȩ����Ϣ���м�أ����ֶ��AccessTokenManager����֮��ģ�Ȩ����Ϣһ�¡�
         */
        final SharedPreferences sp = this.context.getSharedPreferences(BAIDU_SDK_CONFIG,
                Context.MODE_PRIVATE);
        sp.registerOnSharedPreferenceChangeListener(new OnSharedPreferenceChangeListener() {

            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                String acToken = sp.getString(BAIDU_SDK_CONFIG_PROP_ACCESS_TOKEN, null);
                if (accessToken != null && !accessToken.equals(acToken)) {
                        initToken();                   
                }
            }
        });

    }

    /**
     * ��SharedPreference�ж�ȡtoken���ݣ��������ж����ݵ���Ч��
     *
     */
    protected void initToken() {
        SharedPreferences sp = context.getSharedPreferences(BAIDU_SDK_CONFIG, Context.MODE_PRIVATE);
        if (sp == null) {
            return;
        }
        this.accessToken = sp.getString(BAIDU_SDK_CONFIG_PROP_ACCESS_TOKEN, null);      
        long expires = sp.getLong(BAIDU_SDK_CONFIG_PROP_EXPIRE_SECONDS, 0);
        long createTime = sp.getLong(BAIDU_SDK_CONFIG_PROP_CREATE_TIME, 0);
        long current = System.currentTimeMillis();
        this.expireTime = createTime + expires;
        if (expireTime != 0 && expireTime < current) {
            clearToken();
        }

    }

    /**
     * ���SharedPreference�е���������
     */
    protected void clearToken() {
        Editor editor = context.getSharedPreferences(BAIDU_SDK_CONFIG, Context.MODE_PRIVATE).edit();
        editor.remove(BAIDU_SDK_CONFIG_PROP_ACCESS_TOKEN);
        editor.remove(BAIDU_SDK_CONFIG_PROP_CREATE_TIME);
        editor.remove(BAIDU_SDK_CONFIG_PROP_EXPIRE_SECONDS);
        editor.commit();
        this.accessToken = null;
        this.expireTime = 0;
    }

    /**
     * ��token��Ϣ�洢��SharedPreference��
     * 
     * @param values token��Ϣ��key-value��ʽ
     */
    protected void storeToken(Bundle values) {
        if (values == null || values.isEmpty()) {
            return;
        }
        this.accessToken = values.getString("access_token");
        long expiresIn = Long.parseLong(values.getString("expires_in"));
        this.expireTime = System.currentTimeMillis() + expiresIn;
        Editor editor = context.getSharedPreferences(BAIDU_SDK_CONFIG, Context.MODE_PRIVATE).edit();
        editor.putString(BAIDU_SDK_CONFIG_PROP_ACCESS_TOKEN, this.accessToken);
        editor.putLong(BAIDU_SDK_CONFIG_PROP_CREATE_TIME, System.currentTimeMillis());
        editor.putLong(BAIDU_SDK_CONFIG_PROP_EXPIRE_SECONDS, expiresIn);
        editor.commit();

    }
    protected void storeToken(BaiduOAuthResponse values) {
        if (values == null) {
            return;
        }
        this.accessToken = values.getAccessToken();
        long expiresIn = Long.parseLong(values.getExpiresIn());
        this.expireTime = System.currentTimeMillis() + expiresIn;
        Editor editor = context.getSharedPreferences(BAIDU_SDK_CONFIG, Context.MODE_PRIVATE).edit();
        editor.putString(BAIDU_SDK_CONFIG_PROP_ACCESS_TOKEN, this.accessToken);
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

    /**
     * �жϵ�ǰ��token��Ϣ�Ƿ���Ч
     * 
     * @return true/false
     */
    protected boolean isSessionVaild()  {
        if (this.accessToken == null || this.expireTime == 0) {
            initToken();
        }
	    return this.accessToken != null && this.expireTime != 0
			    && System.currentTimeMillis() < this.expireTime;
    }

    /**
     * ��ȡAccessToken��Ϣ
     * 
     * @return accessToken
     *
     */
    public String getAccessToken() {
        if (this.accessToken == null) {
            initToken();
        }
        return this.accessToken;
    }

}
