package mobisocial.musubi.cloudstorage.baidu;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

import com.baidu.pcs.BaiduPCSActionInfo;
import com.baidu.pcs.BaiduPCSClient;

import java.util.ArrayList;
import java.util.List;

import mobisocial.musubi.cloudstorage.AccessTokenManager;
import mobisocial.musubi.cloudstorage.CloudRestoreActivity;
import static mobisocial.musubi.cloudstorage.Utils.showToast;

/**
 * Asynchronously list all the backup file from Baidu cloud storage that
 * associate with this user.
 */
public class BaiduListTask  extends AsyncTask<Void, Integer, List<String>> {

    private final ProgressDialog mDialog;
    private static final String TAG = "BaiduListTask";
    private String mErrorMsg;
    private Context mContext;
    private String mPath;
    private List<Long> mLens = new ArrayList<> ();

    public BaiduListTask(Context context, String path) {

        mContext = context;
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
            BaiduPCSClient api = new BaiduPCSClient();
            api.setAccessToken(AccessTokenManager.getAccessToken());

            final BaiduPCSActionInfo.PCSListInfoResponse ret = api.list
                    (mPath, "time", "desc");

            for ( BaiduPCSActionInfo.PCSCommonFileInfo pcsCommonFileInfo :
                    ret.list) {
                fnames.add(pcsCommonFileInfo.path);
                mLens.add(pcsCommonFileInfo.size);
            }
        } catch (Exception e) {
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
                    for ( int i= 0; i<size; i++) {
                        lens[i] = mLens.get(i);
                    }
                    ((CloudRestoreActivity) mContext).onBaiduListingReceived
                            (res,  lens);
                }
            } else {
                showToast(mContext, "No buck-up record found on Baidu.");
            }
        }
    }
}



