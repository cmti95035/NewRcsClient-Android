package mobisocial.musubi.cloudstorage.baidu;

import android.app.ProgressDialog;
import android.content.Context;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.widget.Toast;

import com.baidu.pcs.BaiduPCSActionInfo;
import com.baidu.pcs.BaiduPCSClient;
import com.baidu.pcs.BaiduPCSStatusListener;

import java.io.File;
import java.io.FileOutputStream;

import mobisocial.musubi.App;
import mobisocial.musubi.cloudstorage.AccessTokenManager;
import mobisocial.musubi.cloudstorage.DownloadTask;
import mobisocial.musubi.model.helpers.DatabaseFile;

public class BaiduDownloadTask extends DownloadTask {

    private static final String TAG = "BaiduDownloadTask";

    public BaiduDownloadTask(Context context, String baiduPath, long length) {
        super(context);
        mPath = baiduPath;
        mFileLen = length;

        mDialog = new ProgressDialog(context);
        mDialog.setMax(100);
        mDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        mDialog.setProgress(0);
        mDialog.setMessage("Downloading file");
        mDialog.setCancelable(false);
        mDialog.show();
    }

    @Override
    protected String doInBackground(Void... params) {
        FileOutputStream fos = null;
        SQLiteOpenHelper helper = App.getDatabaseSource(mContext);
        File oldDb;

        try {
            helper.getWritableDatabase().beginTransaction();
            oldDb = mContext.getDatabasePath(DatabaseFile
                    .DEFAULT_DATABASE_NAME + ".to");

            BaiduPCSClient api = new BaiduPCSClient();
            api.setAccessToken(AccessTokenManager.getAccessToken());

            final BaiduPCSActionInfo.PCSSimplefiedResponse response = api
                    .downloadFileFromStream(mPath, oldDb.getAbsolutePath(), new
                            BaiduPCSStatusListener() {
                                @Override
                                public void onProgress(long bytes, long total) {
                                    publishProgress((int) (bytes / total *
                                            DOWN_LOAD_WEIGHT));
                                }

                                @Override
                                public long progressInterval() {
                                    return 2000;
                                }

                            });

            helper.getWritableDatabase().endTransaction();
            helper.close();

            String fileName = getBackupFileName(mPath);
            String ret = decrypt(oldDb, fileName, TAG);
            if (null == ret) {
                storePref();
                android.os.Process.killProcess(android.os.Process.myPid());
                return null;
            }
            mErrorMsg = "Failed to decrypt file: " + ret;
            // The AuthSession wasn't properly authenticated or user unlinked.
        } catch (Exception e) {
            Log.e(TAG, e.toString());
            mErrorMsg = e.toString();
        }
        return mErrorMsg;
    }

    @Override
    protected void onProgressUpdate(Integer... progress) {
        mDialog.setProgress(progress[0]);
    }

    @Override
    protected void onPostExecute(String result) {
        mDialog.dismiss();
        if (null == mErrorMsg) {
            showToast("Restore succeeded");
        } else {
            // Couldn't download it, so show an error
            showToast(mErrorMsg);
        }
    }

    private void showToast(String msg) {
        Toast error = Toast.makeText(mContext, msg, Toast.LENGTH_LONG);
        error.show();
    }
}
