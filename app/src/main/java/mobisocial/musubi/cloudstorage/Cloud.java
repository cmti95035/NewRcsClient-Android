package mobisocial.musubi.cloudstorage;


import android.content.Context;

public interface Cloud {
    void backup(Context ctx);
    void restore(Context ctx);

    int STORAGE_DROPBOX = 1;
    int STORAGE_BAIDU = 2;

    String BACKUP_DIR = "/backup/";
    String ENCRYPT_FILE_POSTFIX = "_backup";
}
