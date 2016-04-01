package mobisocial.musubi.cloudstorage.dropbox;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;

import com.chinamobile.cloudStorageProxy.server.BackupRecord;
import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.DropboxAPI.UploadRequest;
import com.dropbox.client2.ProgressListener;
import com.dropbox.client2.exception.DropboxException;
import com.dropbox.client2.exception.DropboxFileSizeException;
import com.dropbox.client2.exception.DropboxIOException;
import com.dropbox.client2.exception.DropboxParseException;
import com.dropbox.client2.exception.DropboxPartialFileException;
import com.dropbox.client2.exception.DropboxServerException;
import com.dropbox.client2.exception.DropboxUnlinkedException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import mobisocial.musubi.cloudstorage.UploadTask;
import mobisocial.musubi.cloudstorage.Utils;

/**
 * Asynchronously upload the backup file to dropbox cloud storage.
 */
public class DropboxUploadTask extends UploadTask {
    private DropboxAPI<?> mApi;
    private UploadRequest mRequest;
    private static final String TAG = "DropboxUploadTask";

    public DropboxUploadTask(Context context, DropboxAPI<?> api, String
            dropboxPath, File file) {
        mContext = context.getApplicationContext();

        if (null == file) {
            isDB = true;
        } else {
            mFile = file;
            mFileLen = mFile.length();
        }

        mApi = api;
        mPath = dropboxPath;

        mDialog = new ProgressDialog(context);
        mDialog.setMax(100);
        mDialog.setMessage("Uploading backup files...");
        mDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        mDialog.setProgress(0);
        mDialog.setButton(ProgressDialog.BUTTON_POSITIVE, "Cancel", new
                OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // This will cancel the putFile operation
                        mRequest.abort();
                    }
                });
        mDialog.show();
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        try {
            if (isDB) {
                mFile = getDBFile(TAG);
                if (null == mFile) {
                    mErrorMsg = "Failed to prepare file before uploading";
                    return false;
                }
                mFileLen = mFile.length();
            }

            // By creating a request, we get a handle to the putFile operation,
            // so we can cancel it later if we want to
            FileInputStream fis = new FileInputStream(mFile);
            String path = mPath + mFile.getName();
            mRequest = mApi.putFileOverwriteRequest(path, fis, mFile.length(),
                    new ProgressListener() {
                        @Override
                        public long progressInterval() {
                            // Update the progress bar every other second
                            return 2000;
                        }

                        @Override
                        public void onProgress(long bytes, long total) {
                            int prog = (int) (bytes / mFileLen *
                                    UP_LOAD_WEIGHT) +
                                    UP_COPY_WEIGHT + UP_ENCRYPT_WEIGHT;
                            publishProgress(prog);
                        }
                    });

            if (mRequest != null) {
                mRequest.upload();
                Utils.insertBackupRecord(mContext, new BackupRecord().setTimestamp
                        (Utils.getTimestamp(mFile.getName())).setUserId(Utils.getId
                        (mContext))
                        .setBackupFileName(mPath + mFile.getName()));
                return true;
            }

        } catch (DropboxUnlinkedException e) {
            // This session wasn't authenticated properly or user unlinked
            mErrorMsg = "This app wasn't authenticated properly.";
        } catch (DropboxFileSizeException e) {
            // File size too big to upload via the API
            mErrorMsg = "This file is too big to upload";
        } catch (DropboxPartialFileException e) {
            // We canceled the operation
            mErrorMsg = "Upload canceled";
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
        } catch (FileNotFoundException e) {
        }
        return false;
    }

    @Override
    protected void onProgressUpdate(Integer... progress) {
        mDialog.setProgress(progress[0].intValue());
    }

}





