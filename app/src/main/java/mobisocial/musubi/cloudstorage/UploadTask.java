package mobisocial.musubi.cloudstorage;

import android.app.ProgressDialog;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import com.chinamobile.cloudStorageProxy.server.BackupRecord;
import com.facebook.crypto.Crypto;
import com.facebook.crypto.Entity;
import com.facebook.crypto.util.SystemNativeCryptoLibrary;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import mobisocial.crypto.CloudedKeyChain;
import mobisocial.musubi.App;
import mobisocial.musubi.model.helpers.DatabaseFile;

/**
 * Asynchronously upload the backup file to the cloud storage. It also requests
 * the encryption key from the server and encrypt the file before uploading.
 */
public abstract class UploadTask extends AsyncTask<Void, Integer, Boolean> {
    protected String mPath;
    protected File mFile;
    protected long mFileLen;

    protected ProgressDialog mDialog;
    protected String mErrorMsg;
    protected boolean isDB = false;

    public Context mContext;

    protected static final int UP_COPY_WEIGHT = 10;
    protected static final int UP_ENCRYPT_WEIGHT = 30;
    protected static final int UP_LOAD_WEIGHT = 60;
    protected static final String LOCAL_BACKUP_DIR = "/temp/backup/";

    /**
     * Make a copy of the local database file.
     * @param TAG the tag from the caller, usually the caller's class name
     * @return the copied file
     */
    protected File copyDbLocal(String TAG) {

        FileInputStream in = null;
        FileOutputStream out = null;
        File result;

        SQLiteDatabase db = App.getDatabaseSource(mContext)
                .getWritableDatabase();
        db.beginTransaction();
        try {
            File currentDB = mContext.getDatabasePath(DatabaseFile
                    .DEFAULT_DATABASE_NAME);

            String extStorageDirectory = Environment
                    .getExternalStorageDirectory().toString()
                    + LOCAL_BACKUP_DIR;
            File backupDB = new File(extStorageDirectory, DatabaseFile
                    .DEFAULT_DATABASE_NAME);
            File fileDirectory = new File(extStorageDirectory);
            if (!fileDirectory.exists()) {
                fileDirectory.mkdirs();
            }

            in = new FileInputStream(currentDB);
            out = new FileOutputStream(backupDB);
            byte[] buf = new byte[65536];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            publishProgress(UP_COPY_WEIGHT);
            result = backupDB;
        } catch (Exception e) {
            Log.e(TAG, "Failure backing up to local SD card", e);
            return null;
        } finally {
            db.endTransaction();
            try {
                if (in != null) in.close();
                if (out != null) in.close();
            } catch (IOException e) {
                Log.e(TAG, "failed to close streams for local backup", e);
            }
        }
        return result;
    }

    /**
     * Encrpt the bakcup file before uploading it.
     * @param file the backup data file
     * @param TAG the tag from caller, usually the caller's class name
     * @return the encrypted the backup file
     */
    protected File encrypt(File file, String TAG) {
        // Creates a new Crypto object with customized implementations of
        // a key chain as well as native library.
        CloudedKeyChain keyChain = new CloudedKeyChain(mContext);
        String timeStamp = String.valueOf(keyChain.getTimestamp());

        try {
            // This might fail if android does not load libaries correctly.
            // Or there is no native support at all.
            // Tested and worked on Samsung S3 and LG G2. Failed on Samsung S6

            Crypto crypto = new Crypto(keyChain, new
                    SystemNativeCryptoLibrary());

            File newFile = new File(file.getAbsolutePath() + "." + timeStamp);

            OutputStream fileStream = new BufferedOutputStream(
                    new FileOutputStream(newFile));

            InputStream inputStream = new BufferedInputStream(new
                    FileInputStream(file));

            // Creates an output stream which encrypts the data as
            // it is written to it and writes it out to the file.
            OutputStream outputStream = crypto.getCipherOutputStream(
                    fileStream, new Entity(newFile.getName()));


            byte[] buff = new byte[65536];
            int len;
            while ((len = inputStream.read(buff)) != -1) {
                outputStream.write(buff, 0, len);
            }
            inputStream.close();
            outputStream.close();
            file.delete();
            publishProgress(UP_ENCRYPT_WEIGHT + UP_COPY_WEIGHT);
            return newFile;
        } catch (Exception e) {
            Log.e(TAG, "Failed to encrypt the backup file", e);
        }
        return null;
    }

    /**
     * Prepare the backup file to be uploaded, it take two steps:
     * 1. copy the local database file
     * 2. encrypt the copied local database file
     * @param TAG the tag from caller, usually the caller's class name
     * @return the encrypted back-up file
     */
    protected File getDBFile(String TAG) {
        File plain = copyDbLocal(TAG);
        if (null != plain) {
            return encrypt(plain, TAG);
        }
        return null;
    }

    protected void showToast(String msg) {
        Utils.showToast(mContext, msg);
    }

    protected boolean prepareDB(String TAG) {
        mFile = getDBFile(TAG);
        if (null == mFile) {
            mErrorMsg = "Failed to copy file before uploading";
            return false;
        }
        mFileLen = mFile.length();
        return true;
    }

    @Override
    protected void onPostExecute(Boolean result) {
        mDialog.dismiss();
        if (result) {
            showToast("Backup successfully uploaded");
        } else {
            showToast(mErrorMsg);
        }

        if (null != mFile) {
            mFile.delete();
        }
    }

}
