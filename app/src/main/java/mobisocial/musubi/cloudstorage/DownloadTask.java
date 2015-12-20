package mobisocial.musubi.cloudstorage;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;

import mobisocial.musubi.service.WizardStepHandler;

public abstract class DownloadTask extends AsyncTask<Void, Long, String> {

    protected static final String LOCAL_RESTORE_DIR = "/temp/restore/";
    protected Context mContext;
    private static final String TAG = "DownloadTask";
    protected static final float DOWN_COPY_WEIGHT = 0.25f;
    protected static final float DOWN_LOAD_WEIGHT = 0.75f;

    protected void storePref() {
        SharedPreferences settings = mContext.getSharedPreferences
                (WizardStepHandler.WIZARD_PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean(WizardStepHandler.DO_RESTORE, true);
        editor.commit();
    }
}
