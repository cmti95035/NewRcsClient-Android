package mobisocial.musubi.cloudstorage.baidu;

import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.baidu.oauth.BaiduOAuth;
import com.baidu.pcs.BaiduPCSActionInfo;
import com.baidu.pcs.BaiduPCSClient;
import com.baidu.pcs.BaiduPCSStatusListener;

import org.json.JSONException;

import java.io.File;
import java.io.IOException;

import mobisocial.musubi.cloudstorage.AccessTokenManager;
import mobisocial.musubi.cloudstorage.Cloud;
import mobisocial.musubi.cloudstorage.UploadTask;

public final class Baidu implements Cloud {
    // the default root folder
    /*
     * mbRootPath should be your app_path, please instead of
     * "/apps/pcstest_oauth"
     */
    private final static String mbRootPath = "/apps/StickyPOMI";

    // api_key";
    // the api key
    /*
     * mbApiKey should be your app_key, please instead of "your api_key"
     */
    private static String mbApiKey = "jmWK4EfYlQtMpUbWcU2GRlWF"; //your
    private static Baidu instance;
    private String mbOauth = null;
    // the handler
    private Handler mbUiThreadHandler = null;
    private boolean hasBackupStarted = false;
    private boolean hasRestoreStarted = false;
    private Context mContext;

    private Baidu() {
        mbUiThreadHandler = new Handler();
    }

    public static synchronized Baidu getInstance() {
        if (null == instance) {
            instance = new Baidu();
        }
        return instance;
    }

    public void login(final Context context) {

        if (AccessTokenManager.isSessionVaild(mContext)) {
            postLogin();
            return;
        }

        BaiduOAuth oauthClient = new BaiduOAuth();
        oauthClient.startOAuth(context, mbApiKey, new BaiduOAuth
                .OAuthListener() {
            @Override
            public void onException(String msg) {
                Toast.makeText(context.getApplicationContext(), "Login failed" +
                        " " +
                        "" + msg, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onComplete(BaiduOAuth.BaiduOAuthResponse response) {
                if (null != response) {
                    mbOauth = response.getAccessToken();
                    AccessTokenManager.storeToken(response);

                    /*
                    Toast.makeText(context.getApplicationContext(), "Token: "
                            + mbOauth + "    User name:" + response
                            .getUserName(), Toast.LENGTH_SHORT).show();*/

                    postLogin();
                }
            }

            @Override
            public void onCancel() {
                Toast.makeText(context.getApplicationContext(), "Login " +
                        "cancelled", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void postLogin() {
        if (hasBackupStarted) {
            hasBackupStarted = false;
            doBackup();
        } else if (hasRestoreStarted) {
            hasRestoreStarted = false;
            doRestore();
        }
    }


    public void logout(final Context context) {
        if (null != mbOauth) {
            /**
             * you can call this method to logout in Android 2.X
             */
            Thread workThread = new Thread(new Runnable() {
                @Override
                public void run() {

                    BaiduOAuth oauth = new BaiduOAuth();

                    final boolean ret = oauth.logout(mbOauth);
                    mbUiThreadHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            AccessTokenManager.clearToken();
                            Toast.makeText(context.getApplicationContext(),
                                    "Logout " + ret, Toast.LENGTH_SHORT).show();
                        }
                    });

                }
            });

            workThread.start();
        }
    }

    @Override
    public void backup(Context ctx) {
        mContext = ctx;
        hasBackupStarted = true;
        login(mContext);
    }

    @Override
    public void restore(Context ctx) {
        mContext = ctx;
        hasRestoreStarted = true;
        login(mContext);
    }

    private void doBackup() {
        mbOauth = AccessTokenManager.getAccessToken();
        if (null != mbOauth) {
            BaiduUploadTask uploader=new BaiduUploadTask(mContext,
                    "/apps/musubi/",
                    null);
            uploader.execute();
        }
    }

    private void doRestore() {
        mbOauth = AccessTokenManager.getAccessToken();
        if (null != mbOauth) {
            BaiduListTask lister=new BaiduListTask(mContext,
                    "/apps/musubi/");
            lister.execute();
        }
    }
}
