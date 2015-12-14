package mobisocial.musubi.cloudstorage.dropbox;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import mobisocial.musubi.R;
import mobisocial.musubi.cloudstorage.FileAdapter;

public class DropboxListActivity extends Activity {

    private FileAdapter mAdapter;
    private ListView fileList;
    AdapterView.OnItemClickListener listener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        String[] data = null;
        long[] lengths = null;
        if (null != extras) {
            data = extras.getStringArray(DropboxListTask.DROPBOX_FILE_NAMES);
            lengths = extras.getLongArray(DropboxListTask
                    .DROPBOX_FILE_LENGTHS);
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
            }
        };
        fileList.setOnItemClickListener(listener);
    }
}
