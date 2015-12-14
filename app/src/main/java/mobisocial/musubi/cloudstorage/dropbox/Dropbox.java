package mobisocial.musubi.cloudstorage.dropbox;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.android.AuthActivity;
import com.dropbox.client2.exception.DropboxException;
import com.dropbox.client2.session.AccessTokenPair;
import com.dropbox.client2.session.AppKeyPair;

import java.util.ArrayList;
import java.util.List;

import mobisocial.musubi.cloudstorage.Cloud;

/**
 * The new Dropbox storage client using core API.
 *
 */
public final class Dropbox implements Cloud {

    private static final String TAG = "Dropbox";
    private static final String APP_KEY = "3bduqwpgg381rlj";
    private static final String APP_SECRET = "h8a2ka5f2vm8ttk";

    private static final String ACCOUNT_PREFS_NAME = "prefs";
    private static final String ACCESS_KEY_NAME = "ACCESS_KEY";
    private static final String ACCESS_SECRET_NAME = "ACCESS_SECRET";

    DropboxAPI<AndroidAuthSession> mApi;
    private boolean hasBackupStarted = false;
    private boolean hasRestoreStarted = false;

    private Context mContext;
    private static Dropbox instance;

    private Dropbox(){ }

    public static synchronized Dropbox getInstance() {
        if (null == instance) {
            instance = new Dropbox();
        }
        return instance;
    }

    private void logOut() {
        // Remove credentials from the session
        mApi.getSession().unlink();
        // Clear our stored keys
        clearKeys();
    }

    private void auth(Context ctx) {
        // We create a new AuthSession so that we can use the Dropbox API.
        AndroidAuthSession session = buildSession();
        mApi = new DropboxAPI<AndroidAuthSession>(session);
        checkAppKeySetup();
        mApi.getSession().startOAuth2Authentication(mContext);
    }

    @Override
    public void backup(Context ctx) {
        mContext = ctx;
        hasBackupStarted = true;
        auth(mContext);
    }

    @Override
    public void restore(Context ctx) {
        mContext = ctx;
        hasRestoreStarted = true;
        auth(mContext);
    }

    /**
     * Back from authentication screen.
     * Called by (Activity) mContext.onResume()
     */
    public void resumeFromAuth() {

        if (hasBackupStarted) {
            hasBackupStarted = false;
            postAuth(true);
        } else if (hasRestoreStarted) {
            hasRestoreStarted = false;
            postAuth(false);
        }
    }

    private void postAuth(Boolean isBackup) {
        AndroidAuthSession session = mApi.getSession();

        // Dropbox authentication completes properly.
        if (null!=session && session.authenticationSuccessful()) {
            try {
                // Mandatory call to complete the auth
                session.finishAuthentication();

                // Store it locally in our app for later use
                storeAuth(session);

                // Backup, upload files
                if (isBackup) {
                    upload();
                } else {    // Restore, download file
                    // first, expand files to be select
                    // upon selection, download file and restore it
                    download();
                }
            } catch (IllegalStateException e) {
                showToast("Couldn't authenticate with Dropbox:" + e.getLocalizedMessage());
                Log.e(TAG, "Error authenticating", e);
            }
        }
    }

    private void download() {
        DropboxListTask lister = new DropboxListTask(mContext, mApi, BACKUP_DIR);
        lister.execute();
    }

    private void upload() {
        DropboxUploadTask uploader = new DropboxUploadTask(mContext, mApi, BACKUP_DIR, null);
        uploader.execute();
    }

    private void checkAppKeySetup() {

        // Check if the app has set up its manifest properly.
        Intent testIntent = new Intent(Intent.ACTION_VIEW);
        String scheme = "db-" + APP_KEY;
        String uri = scheme + "://" + AuthActivity.AUTH_VERSION + "/test";
        testIntent.setData(Uri.parse(uri));
        PackageManager pm = mContext.getPackageManager();
        if (0 == pm.queryIntentActivities(testIntent, 0).size()) {
            showToast("URL scheme in your app's " +
                    "manifest is not set up correctly. You should have a " +
                    "com.dropbox.client2.android.AuthActivity with the " +
                    "scheme: " + scheme);
        }
    }

    private void showToast(String msg) {
        Toast error = Toast.makeText(mContext, msg, Toast.LENGTH_LONG);
        error.show();
    }

    /**
     * Shows keeping the access keys returned from Trusted Authenticator in a local
     * store, rather than storing user name & password, and re-authenticating each
     * time (which is not to be done, ever).
     */
    private void loadAuth(AndroidAuthSession session) {
        SharedPreferences prefs = mContext.getSharedPreferences
                (ACCOUNT_PREFS_NAME, 0);
        String key = prefs.getString(ACCESS_KEY_NAME, null);
        String secret = prefs.getString(ACCESS_SECRET_NAME, null);
        if (key == null || secret == null || key.length() == 0 || secret.length() == 0) return;

        if (key.equals("oauth2:")) {
            // If the key is set to "oauth2:", then we can assume the token is for OAuth 2.
            session.setOAuth2AccessToken(secret);
        } else {
            // Still support using old OAuth 1 tokens.
            session.setAccessTokenPair(new AccessTokenPair(key, secret));
        }
    }

    /**
     * Shows keeping the access keys returned from Trusted Authenticator in a local
     * store, rather than storing user name & password, and re-authenticating each
     * time (which is not to be done, ever).
     */
    private void storeAuth(AndroidAuthSession session) {
        // Store the OAuth 2 access token, if there is one.
        String oauth2AccessToken = session.getOAuth2AccessToken();
        if (oauth2AccessToken != null) {
            SharedPreferences prefs = mContext.getSharedPreferences
                    (ACCOUNT_PREFS_NAME, 0);
            SharedPreferences.Editor edit = prefs.edit();
            edit.putString(ACCESS_KEY_NAME, "oauth2:");
            edit.putString(ACCESS_SECRET_NAME, oauth2AccessToken);
            edit.commit();
            return;
        }
        // Store the OAuth 1 access token, if there is one.  This is only necessary if
        // you're still using OAuth 1.
        AccessTokenPair oauth1AccessToken = session.getAccessTokenPair();
        if (oauth1AccessToken != null) {
            SharedPreferences prefs = mContext.getSharedPreferences
                    (ACCOUNT_PREFS_NAME, 0);
            SharedPreferences.Editor edit = prefs.edit();
            edit.putString(ACCESS_KEY_NAME, oauth1AccessToken.key);
            edit.putString(ACCESS_SECRET_NAME, oauth1AccessToken.secret);
            edit.commit();
        }
    }

    private void clearKeys() {
        SharedPreferences prefs = mContext.getSharedPreferences
                (ACCOUNT_PREFS_NAME, 0);
        SharedPreferences.Editor edit = prefs.edit();
        edit.clear();
        edit.commit();
    }

    private AndroidAuthSession buildSession() {
        AppKeyPair appKeyPair = new AppKeyPair(APP_KEY, APP_SECRET);
        AndroidAuthSession session = new AndroidAuthSession(appKeyPair);
        loadAuth(session);
        return session;
    }

}
