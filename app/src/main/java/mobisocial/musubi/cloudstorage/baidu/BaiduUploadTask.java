package mobisocial.musubi.cloudstorage.baidu;

import android.app.ProgressDialog;
import android.content.Context;
import android.util.Log;

import com.baidu.pcs.BaiduPCSActionInfo;
import com.baidu.pcs.BaiduPCSClient;
import com.baidu.pcs.BaiduPCSStatusListener;
import com.chinamobile.cloudStorageProxy.server.BackupRecord;

import java.io.File;

import mobisocial.musubi.cloudstorage.AccessTokenManager;
import mobisocial.musubi.cloudstorage.UploadTask;
import mobisocial.musubi.cloudstorage.Utils;

/**
 * Asynchronously upload the backup file to baidu cloud storage.
 */
public class BaiduUploadTask extends UploadTask {

    private static final String TAG = "BaiduUploadTask";

    public BaiduUploadTask(Context context, String
            baiduPath, File file) {
        // We set the context this way so we don't accidentally leak activities
        mContext = context.getApplicationContext();

        if (null == file) {
            isDB = true;
        } else {
            mFile = file;
            mFileLen = mFile.length();
        }

        mPath = baiduPath;

        mDialog = new ProgressDialog(context);
        mDialog.setMax(100);
        mDialog.setMessage("Uploading backup files...");
        mDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        mDialog.setProgress(0);
        mDialog.setCancelable(false);
        mDialog.show();
    }

    @Override
    protected Boolean doInBackground(Void... params) {

        try {
            if (isDB && !prepareDB(TAG)) {
                return false;
            }

            String path = mPath + mFile.getName();

            BaiduPCSClient api = new BaiduPCSClient();
            api.setAccessToken(AccessTokenManager.getAccessToken());


            final BaiduPCSActionInfo.PCSFileInfoResponse response =
                    api.uploadFile(mFile.getAbsolutePath(), path, new
                            BaiduPCSStatusListener() {

                                @Override
                                public void onProgress(long bytes, long total) {
                                    publishProgress((int) bytes);
                                }

                                @Override
                                public long progressInterval() {
                                    return 2000;
                                }
                            });


        } catch (Exception e) {
            Log.e(TAG, e.toString());
            mErrorMsg = e.toString();
            return false;
        }
        Utils.insertBackupRecord(mContext, new BackupRecord().setTimestamp
                (Utils.getTimestamp(mFile.getName())).setUserId(Utils.getId
                (mContext))
                .setBackupFileName(mPath + mFile.getName()));
        return true;
    }


    @Override
    protected void onProgressUpdate(Integer... progress) {
        int percent = (int) (UP_LOAD_WEIGHT * (double) progress[0] /
                mFileLen) + UP_COPY_WEIGHT + UP_ENCRYPT_WEIGHT;
        mDialog.setProgress(percent);
    }
}
