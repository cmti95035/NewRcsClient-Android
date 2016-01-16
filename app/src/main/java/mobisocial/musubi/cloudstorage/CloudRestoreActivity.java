package mobisocial.musubi.cloudstorage;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import mobisocial.musubi.R;

public class CloudRestoreActivity extends Activity {

    private static final String TAG = "CloudRestoreActivity";
    static CloudAdapter adapter;

    ListView cloud_storage_list;
    AdapterView.OnItemClickListener listener;
    Cloud mCloud = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cloud_storage);

        cloud_storage_list = (ListView) this.findViewById(R.id.cloud_storage_list);
        adapter = new CloudAdapter(this);
        cloud_storage_list.setAdapter(adapter);
        listener = new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
                                    long arg3) {

                switch (arg2) {
                    case 0:
                        mCloud = mobisocial.musubi.cloudstorage.dropbox.Dropbox.getInstance();
                        break;
                    case 1:
                        AccessTokenManager.initToken(CloudRestoreActivity
                                .this);
                        mCloud = mobisocial.musubi.cloudstorage.baidu.Baidu.getInstance();
                        break;
                    default:
                        mCloud = null;
                }
                if (null != mCloud) {
                    mCloud.restore(CloudRestoreActivity.this);
                }
            }
        };
        cloud_storage_list.setOnItemClickListener(listener);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mCloud instanceof mobisocial.musubi.cloudstorage.dropbox.Dropbox) {
            ((mobisocial.musubi.cloudstorage.dropbox.Dropbox) mCloud)
                    .resumeFromAuth();
        }
    }

    public void onDropboxListingReceived(String[] files, long[] lens) {
        Intent intent = new Intent(this, FileListActivity.class);
        intent.putExtra(FileListActivity.FILE_NAMES,files);
        intent.putExtra(FileListActivity.FILE_LENGTHS,lens);
        intent.putExtra(FileListActivity.CLOUD_NAME,FileListActivity
                .CLOUD_NAME_DROPBOX);
        startActivity(intent);
    }

    public void onBaiduListingReceived(String[] files, long[] lens) {
        Intent intent = new Intent(this, FileListActivity.class);
        intent.putExtra(FileListActivity.FILE_NAMES,files);
        intent.putExtra(FileListActivity.FILE_LENGTHS,lens);
        intent.putExtra(FileListActivity.CLOUD_NAME,FileListActivity
                .CLOUD_NAME_BAIDU);
        startActivity(intent);
    }
}
