package mobisocial.musubi.cloudstorage;


import android.content.Context;

/**
 * The common interface for cloud storage. All the cloud storage need implement this
 * interface.
 */
public interface Cloud {

    /**
     * This method implements message back-up to the cloud storage.
     */
    void backup(Context ctx);

    /**
     * This method implements message restore to the cloud storage.
     */
    void restore(Context ctx);

    int STORAGE_DROPBOX = 1;
    int STORAGE_BAIDU = 2;

    String BACKUP_DIR = "/backup/";
    String ENCRYPT_FILE_POSTFIX = "_backup";
}
