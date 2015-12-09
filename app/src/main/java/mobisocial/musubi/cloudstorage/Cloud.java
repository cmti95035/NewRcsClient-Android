package mobisocial.musubi.cloudstorage;


import android.content.Context;

public interface Cloud {
    //void setContext(Context ctx);
    void backup();
    void restore();
    //void save();

    int STORAGE_DROPBOX = 1;
    int STORAGE_BAIDU = 2;

}
