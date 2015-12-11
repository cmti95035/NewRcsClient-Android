package mobisocial.musubi.cloudstorage;

import android.app.ProgressDialog;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import mobisocial.musubi.App;
import mobisocial.musubi.model.helpers.DatabaseFile;

public abstract class UploadTask extends AsyncTask<Void, Long, Boolean> {
    protected String mPath;
    protected File mFile;

    protected long mFileLen;
    protected Context mContext;
    protected ProgressDialog mDialog;

    protected String mErrorMsg;
    protected boolean isDB = false;

    protected static final float UP_COPY_WEIGHT = 0.2f;
    protected static final float UP_LOAD_WEIGHT = 0.8f;
    protected static final String LOCAL_DIR = "/temp/";

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
                    + LOCAL_DIR;
            File backupDB = new File(extStorageDirectory, DatabaseFile.DEFAULT_DATABASE_NAME);
            File fileDirectory = new File(extStorageDirectory);
            fileDirectory.mkdirs();

            long file_size = currentDB.length();
            in = new FileInputStream(currentDB);
            out = new FileOutputStream(backupDB);
            byte[] buf = new byte[65536];
            int len;
            long so_far = 0;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
                so_far += len;
                publishProgress((long) (100 * so_far / (file_size + 1) *
                        UP_COPY_WEIGHT));
            }
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
}
