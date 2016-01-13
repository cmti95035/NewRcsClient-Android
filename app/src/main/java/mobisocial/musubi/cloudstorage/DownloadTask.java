package mobisocial.musubi.cloudstorage;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;

import com.chinamobile.cloudStorageProxy.server.BackupRecord;
import com.facebook.crypto.Crypto;
import com.facebook.crypto.Entity;
import com.facebook.crypto.util.SystemNativeCryptoLibrary;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;

import mobisocial.crypto.CloudedKeyChain;
import mobisocial.musubi.service.WizardStepHandler;

public abstract class DownloadTask extends AsyncTask<Void, Integer, String> {

    protected Context mContext;
    protected static final int DOWN_LOAD_WEIGHT = 75;

    public DownloadTask(Context context) {
        mContext = context.getApplicationContext();
    }

    protected void storePref() {
        SharedPreferences settings = mContext.getSharedPreferences
                (WizardStepHandler.WIZARD_PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean(WizardStepHandler.DO_RESTORE, true);
        editor.commit();
    }

    protected String decrypt(File file, String fileName, String TAG) {
        // Use mock key for now
        // Creates a new Crypto object with customized implementations of
        // a key chain as well as native library.

        String err = null;
        try {
            File newFile = new File(file.getAbsolutePath() + "restore");
            FileInputStream fileStream = new FileInputStream(file);

            long ts = Utils.getTimestamp(fileName);
            String id = Utils.getId(mContext);
            BackupRecord backupRecord = Utils.retrieveBackupRecord(id, ts);

            CloudedKeyChain keyChain = new CloudedKeyChain(backupRecord
                    .getHash(), ts);
            Crypto crypto = new Crypto(keyChain, new
                    SystemNativeCryptoLibrary());

            // Check for whether the crypto functionality is available
            // This might fail if android does not load libaries correctly.
            // Tested and worked on Samsung S3 and LG G2. Failed on Samsung S6
            if (!crypto.isAvailable()) {
                return null;
            }

            InputStream inputStream = crypto.getCipherInputStream(fileStream,
                    new Entity(fileName));
            FileOutputStream outputStream = new FileOutputStream(newFile);

            // Read into a byte array.
            int len;
            byte[] buffer = new byte[65536];

            // You must read the entire stream to completion.
            // The verification is done at the end of the stream.
            // Thus not reading till the end of the stream will cause
            // a security bug.
            while ((len = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, len);
            }

            inputStream.close();
            outputStream.close();
            publishProgress(100);

        } catch (Exception e) {
            err = e.toString();
        } finally {
            if (file.exists()) {
                file.delete();
            }
        }
        return err;
    }

    protected String getBackupFileName(String path) {
        return path.substring(path.lastIndexOf("/") + 1, path.length());
    }
}
