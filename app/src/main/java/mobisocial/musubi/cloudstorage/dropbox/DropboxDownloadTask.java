package mobisocial.musubi.cloudstorage.dropbox;

import android.app.ProgressDialog;
import android.content.Context;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Process;
import android.widget.Toast;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.ProgressListener;
import com.dropbox.client2.exception.DropboxException;
import com.dropbox.client2.exception.DropboxIOException;
import com.dropbox.client2.exception.DropboxParseException;
import com.dropbox.client2.exception.DropboxPartialFileException;
import com.dropbox.client2.exception.DropboxServerException;
import com.dropbox.client2.exception.DropboxUnlinkedException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import mobisocial.musubi.App;
import mobisocial.musubi.cloudstorage.DownloadTask;
import mobisocial.musubi.model.helpers.DatabaseFile;

/**
 * Asynchronously download the backup file to dropbox cloud storage.
 */
public class DropboxDownloadTask extends DownloadTask {

    private DropboxAPI<?> mApi;
    private static final String TAG = "DropboxDownloadTask";

    public DropboxDownloadTask(Context context, DropboxAPI<?> api,
                               String dropboxPath, long length) {
        super(context);
        mApi = api;
        mPath = dropboxPath;
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
        File oldDb;

        try {
            oldDb = mContext.getDatabasePath(DatabaseFile
                    .DEFAULT_DATABASE_NAME + ".to");

            fos = new FileOutputStream(oldDb);
            DropboxAPI.DropboxFileInfo info = mApi.getFile(mPath, null,
                    fos, new ProgressListener() {
                        @Override
                        public long progressInterval() {
                            // Update the progress bar every other second or so
                            return 2000;
                        }

                        @Override
                        public void onProgress(long bytes, long total) {
                            publishProgress((int) (bytes / mFileLen *
                                    DOWN_LOAD_WEIGHT));
                        }
                    });

            String fileName = getBackupFileName(mPath);
            String ret = decrypt(oldDb, fileName, TAG);
            if (null == ret) {
                storePref();
                android.os.Process.killProcess(Process.myPid());
                return null;
            }
            mErrorMsg = "Failed to decrypt file: " + ret;
        } catch (DropboxUnlinkedException e) {
            // The AuthSession wasn't properly authenticated or user unlinked.
        } catch (DropboxPartialFileException e) {
            // We canceled the operation
            mErrorMsg = "Download canceled";
        } catch (DropboxServerException e) {
            // This gets the Dropbox error, translated into the user's language
            mErrorMsg = e.body.userError;
            if (mErrorMsg == null) {
                mErrorMsg = e.body.error;
            }
        } catch (DropboxIOException e) {
            // Happens all the time, probably want to retry automatically.
            mErrorMsg = "Network error.  Try again.";
        } catch (DropboxParseException e) {
            // Probably due to Dropbox server restarting, should retry
            mErrorMsg = "Dropbox error.  Try again.";
        } catch (DropboxException e) {
            // Unknown error
            mErrorMsg = "Unknown error.  Try again.";
        } catch (Exception e) {
            mErrorMsg = "Unknown non-dropbox error: " + e.toString();
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                }
            }
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
