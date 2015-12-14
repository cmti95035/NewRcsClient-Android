package mobisocial.musubi.cloudstorage;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mobisocial.musubi.R;
import mobisocial.musubi.cloudstorage.baidu.Baidu;
import mobisocial.musubi.cloudstorage.dropbox.Dropbox;

public class CloudBackupActivity extends Activity {

    private static final String TAG = "CloudBackupActivity";

    static CloudAdapter adapter;
    ListView cloud_storage_list ;
    OnItemClickListener listener;
    Cloud mCloud = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cloud_storage);

        AccessTokenManager.initToken(this);

        cloud_storage_list = (ListView)this.findViewById(R.id.cloud_storage_list);
        adapter = new CloudAdapter(this);
        cloud_storage_list.setAdapter(adapter);
        listener = new OnItemClickListener(){

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
                                    long arg3) {

                    switch(arg2){
                        case 0:
                            mCloud = Dropbox.getInstance();
                            break;
                        case 1:
                            AccessTokenManager.initToken(CloudBackupActivity
                                    .this);
                            mCloud = Baidu.getInstance();
                            break;
                        default:
                            mCloud = null;
                    }
                    if ( null!=mCloud ) {
                        mCloud.backup(CloudBackupActivity.this);
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
}
