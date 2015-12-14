package mobisocial.musubi.cloudstorage;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Process;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import mobisocial.musubi.App;
import mobisocial.musubi.model.helpers.DatabaseFile;
import mobisocial.musubi.service.WizardStepHandler;

public abstract class DownloadTask extends AsyncTask<Void, Long, String> {

    protected static final String LOCAL_RESTORE_DIR = "/temp/restore/";
    protected Context mContext;
    private static final String TAG = "DownloadTask";
    protected static final float DOWN_COPY_WEIGHT = 0.25f;
    protected static final float DOWN_LOAD_WEIGHT = 0.75f;

    protected String restoreDB() {

        FileInputStream in = null;
        FileOutputStream out = null;
        SQLiteOpenHelper helper_;

        helper_ = App.getDatabaseSource(mContext);

        try {
            helper_.getWritableDatabase().beginTransaction();
            String extStorageDirectory = Environment
                    .getExternalStorageDirectory().toString() +
                    LOCAL_RESTORE_DIR;
            String dbPath = extStorageDirectory + DatabaseFile
                    .DEFAULT_DATABASE_NAME;


            File newDb = new File(dbPath);
            File oldDb = mContext.getDatabasePath(DatabaseFile
                    .DEFAULT_DATABASE_NAME + ".torestore");
            if (!newDb.exists()) {
                throw new RuntimeException("Backup database not found");
            }
            in = new FileInputStream(newDb);
            out = new FileOutputStream(oldDb);
            long file_size = newDb.length();
            byte[] buf = new byte[65536];
            int len;
            long so_far = 0;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
                so_far += len;
                //publishProgress((int) (100 * so_far / (file_size + 1)));
            }
            in.close();
            out.close();
            //kill because the process so that it restarts and finishes the
            // restore

            SharedPreferences settings = mContext.getSharedPreferences
                    (WizardStepHandler.WIZARD_PREFS_NAME, 0);
            SharedPreferences.Editor editor = settings.edit();
            editor.putBoolean(WizardStepHandler.DO_RESTORE, true);
            editor.commit();

            android.os.Process.killProcess(Process.myPid());
            return null;
        } catch (Exception e) {
            Log.e(TAG, "Failure restoring from SD card", e);
            return e.toString();
        } finally {
            try {
                if (in != null) out.close();
                if (out != null) out.close();
            } catch (IOException e) {
                Log.e(TAG, "failed to close streams for backup", e);
            }
        }
    }
}
