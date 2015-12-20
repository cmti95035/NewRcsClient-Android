package mobisocial.musubi.cloudstorage;

import android.app.ProgressDialog;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

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

public abstract class UploadTask extends AsyncTask<Void, Integer, Boolean> {
    protected String mPath;
    protected File mFile;

    protected long mFileLen;
    protected Context mContext;
    protected ProgressDialog mDialog;

    protected String mErrorMsg;
    protected boolean isDB = false;

    protected static final int UP_COPY_WEIGHT = 20;
    protected static final int UP_ENCRYPT_WEIGHT = 30;
    protected static final int UP_LOAD_WEIGHT = 50;
    protected static final String LOCAL_BACKUP_DIR = "/temp/backup/";

    protected File copyDbLocal(Context myActivity, String TAG) {
        FileInputStream in = null;
        FileOutputStream out = null;
        File result;

        SQLiteDatabase db = App.getDatabaseSource(myActivity)
                .getWritableDatabase();
        db.beginTransaction();
        try {
            File currentDB = myActivity.getDatabasePath(DatabaseFile
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

            long file_size = currentDB.length();
            in = new FileInputStream(currentDB);
            out = new FileOutputStream(backupDB);
            byte[] buf = new byte[65536];
            int len;
            long so_far = 0;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
                so_far += len;
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

    protected File encrypt(Context myActivity, File file, String TAG) {
        // Use mock key for now
        // Creates a new Crypto object with customized implementations of
        // a key chain as well as native library.
        Crypto crypto = new Crypto(new CloudedKeyChain(true), new
                SystemNativeCryptoLibrary());

        // Check for whether the crypto functionality is available
        // This might fail if android does not load libaries correctly.
        // Tested and worked on Samsung S3 and LG G2. Failed on Samsung S6
        if (!crypto.isAvailable()) {
            return null;
        }

        try {
            String timeStamp = String.valueOf(System.currentTimeMillis());

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

    protected File getDBFile(Context myActivity, String TAG) {
        File plain = copyDbLocal(myActivity, TAG);
        if ( null != plain) {
            return encrypt(myActivity, plain, TAG);
        }
        return null;
    }
}
