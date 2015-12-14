package mobisocial.musubi.cloudstorage.dropbox;


import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.widget.Toast;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.exception.DropboxException;

import java.util.ArrayList;
import java.util.List;

import mobisocial.musubi.cloudstorage.CloudRestoreActivity;

public class DropboxListTask extends AsyncTask<Void, Integer, List<String>> {

    private final ProgressDialog mDialog;
    private static final String TAG = "DropboxListTask";
    private String mErrorMsg;
    private Context mContext;
    private DropboxAPI<?> mApi;
    private String mPath;
    private long[] mLens;

    public static final String DROPBOX_FILE_NAMES = "DropboxFileNames";
    public static final String DROPBOX_FILE_LENGTHS = "DropboxFileLengths";

    public DropboxListTask(Context context, DropboxAPI<?> api, String path) {

        mContext = context;
        mApi = api;
        mPath = path;

        mDialog = new ProgressDialog(context);
        mDialog.setMax(100);
        mDialog.setMessage("Retrieving directory info...");
        mDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        mDialog.setProgress(0);
        mDialog.setCancelable(false);
        mDialog.show();
    }

    @Override
    protected List<String> doInBackground(Void... params) {
        List<String> fnames = new ArrayList<String>();

        try {
            DropboxAPI.Entry dirent = mApi.metadata(mPath, 1000, null, true,
                    null);
            publishProgress(90);
            int i = 0;

            for (DropboxAPI.Entry ent : dirent.contents) {
                fnames.add(ent.path);
                mLens[i++] = ent.bytes;
            }
        } catch (DropboxException e) {
            mErrorMsg = "Unable to retrieve directory info. " + e.toString();
        }

        return fnames;
    }

    @Override
    protected void onProgressUpdate(Integer... progress) {

        mDialog.setProgress(progress[0]);
    }

    @Override
    protected void onPostExecute(List<String> result) {
        mDialog.dismiss();
        if (mErrorMsg != null && mErrorMsg.length() > 0) {
            showToast(mErrorMsg);
        } else {
            if (result != null && result.size() > 0) {
                if (mContext instanceof CloudRestoreActivity) {
                    String[] res = result.toArray(new String[0]);
                    ((CloudRestoreActivity) mContext).onListingReceived(res,
                            mLens);
                }
            } else {
                showToast("No buck-up found on Dropbox.");
            }
        }
    }

    private void showToast(String msg) {
        Toast error = Toast.makeText(mContext, msg, Toast.LENGTH_LONG);
        error.show();
    }
}
