package mobisocial.musubi.cloudstorage.baidu;

import android.app.ProgressDialog;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.baidu.pcs.BaiduPCSActionInfo;
import com.baidu.pcs.BaiduPCSClient;
import com.baidu.pcs.BaiduPCSStatusListener;

import java.io.File;

import mobisocial.musubi.cloudstorage.AccessTokenManager;
import mobisocial.musubi.cloudstorage.UploadTask;

public class BaiduUploadTask extends UploadTask {

    private static final String TAG = "BaiduUploadTask";
    private Context mContext;


    public BaiduUploadTask(Context context, String
            baiduPath, File file) {
        // We set the context this way so we don't accidentally leak activities
        mContext = context.getApplicationContext();

        if (null==file) {
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
            if (isDB) {
                mFile = copyDbLocal(/*mContext, */TAG);
                if (null == mFile) {
                    mErrorMsg = "Failed to copy file before uploading";
                    return false;
                }
                mFileLen = mFile.length();
            }

            // By creating a request, we get a handle to the putFile operation,
            // so we can cancel it later if we want to
            String timeStamp = String.valueOf(System.currentTimeMillis());
            String path = mPath + mFile.getName() + "."+ timeStamp;

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
                            return 1000;
                        }
                    });


        } catch (Exception e) {
            Log.e(TAG, e.toString());
            mErrorMsg = e.toString();
            return false;
        }
        return true;
    }



    @Override
    protected void onProgressUpdate(Integer... progress) {
        int percent = (int) ((UP_LOAD_WEIGHT  * (double) progress[0] /
                mFileLen + UP_COPY_WEIGHT)* 100.0 + 0.5);
        mDialog.setProgress(percent);
    }

    @Override
    protected void onPostExecute(Boolean result) {
        mDialog.dismiss();
        if (result) {
            showToast("Backup successfully uploaded");
        } else {
            showToast(mErrorMsg);
        }
    }

    private void showToast(String msg) {
        Toast error = Toast.makeText(mContext, msg, Toast.LENGTH_LONG);
        error.show();
    }

}
