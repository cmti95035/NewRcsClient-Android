package mobisocial.musubi.cloudstorage;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import mobisocial.musubi.R;
import mobisocial.musubi.cloudstorage.baidu.BaiduDownloadTask;
import mobisocial.musubi.cloudstorage.dropbox.*;

public class FileListActivity extends Activity {

    private FileAdapter mAdapter;
    private ListView fileList;
    private String mCloudName;
    AdapterView.OnItemClickListener listener;

    public static final String FILE_NAMES = "FileNames";
    public static final String FILE_LENGTHS = "FileLengths";
    public static final String CLOUD_NAME = "CloudName";
    public static final String CLOUD_NAME_BAIDU = "baidu";
    public static final String CLOUD_NAME_DROPBOX = "dropbox";

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        String[] data = null;
        long[] lengths = null;
        if (null != extras) {
            data = extras.getStringArray(FILE_NAMES);
            lengths = extras.getLongArray(FILE_LENGTHS);
            mCloudName = extras.getString(CLOUD_NAME);
        }

        setContentView(R.layout.file_list);

        fileList = (ListView) this.findViewById(R.id.file_list);
        mAdapter = new FileAdapter(this, data, lengths);
        fileList.setAdapter(mAdapter);
        listener = new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
                                    long arg3) {

                // get the file name and started downloading
                FileDescription fd = (FileDescription) mAdapter.getItem(arg2);
                String path = fd.getFileName();
                long len = fd.getBytes();
                DownloadTask downloader=null;

                if ( mCloudName.equals(CLOUD_NAME_DROPBOX)) {
                    downloader = new DropboxDownloadTask
                            (FileListActivity.this, mobisocial.musubi
                                    .cloudstorage.dropbox.Dropbox.getInstance()
                                    .getApi(), path, len);
                } else if (mCloudName.equals(CLOUD_NAME_BAIDU)) {
                    downloader = new BaiduDownloadTask(FileListActivity.this,
                            path, len);
                }
                if ( null != downloader) {
                    downloader.execute();
                }
            }
        };
        fileList.setOnItemClickListener(listener);
    }
}
