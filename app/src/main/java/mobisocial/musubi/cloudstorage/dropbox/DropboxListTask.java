package mobisocial.musubi.cloudstorage.dropbox;


import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.exception.DropboxException;

import java.util.ArrayList;
import java.util.List;

import mobisocial.musubi.cloudstorage.CloudRestoreActivity;

import static mobisocial.musubi.cloudstorage.Utils.showToast;

/**
 * Asynchronously list all the backup file from dropbox cloud storage that
 * associate with this user.
 */
public class DropboxListTask extends AsyncTask<Void, Integer, List<String>> {

    private final ProgressDialog mDialog;
    private static final String TAG = "DropboxListTask";
    private String mErrorMsg;
    private Context mContext;
    private DropboxAPI<?> mApi;
    private String mPath;
    private List<Long> mLens = new ArrayList<>();

    public DropboxListTask(Context context, DropboxAPI<?> api, String path) {

        mContext = context;
        mApi = api;
        mPath = path;

        mDialog = new ProgressDialog(context);
        mDialog.setMessage("Retrieving directory info...");
        mDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mDialog.setCancelable(false);
        mDialog.show();
    }

    @Override
    protected List<String> doInBackground(Void... params) {
        List<String> fnames = new ArrayList<>();

        try {
            DropboxAPI.Entry dirent = mApi.metadata(mPath, 1000, null, true,
                    null);

            for (DropboxAPI.Entry ent : dirent.contents) {
                fnames.add(ent.path);
                mLens.add(ent.bytes);
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
            showToast(mContext, mErrorMsg);
        } else {
            if (result != null && result.size() > 0) {
                if (mContext instanceof CloudRestoreActivity) {
                    String[] res = result.toArray(new String[0]);
                    int size = mLens.size();
                    long[] lens = new long[size];
                    for (int i = 0; i < size; i++) {
                        lens[i] = mLens.get(i);
                    }
                    ((CloudRestoreActivity) mContext).onDropboxListingReceived
                            (res,
                                    lens);
                }
            } else {
                showToast(mContext, "No buck-up record found on Dropbox.");
            }
        }
    }
}
