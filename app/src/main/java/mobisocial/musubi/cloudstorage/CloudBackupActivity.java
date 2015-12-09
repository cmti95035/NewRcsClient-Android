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
import mobisocial.musubi.cloudstorage.dropbox.Dropbox;

import mobisocial.musubi.R;

public class CloudBackupActivity extends Activity {

    private static final String TAG = "CloudBackupActivity";
    private static final String CLOUD_STORAGE_NAME = "cloud_storage_name";
    private static final String CLOUD_STORAGE_ICON = "cloud_storage_icon";
    private static final List<Map<String, Object>> mData = new ArrayList<Map<String, Object>>();

    private boolean dropboxStarted = false;

    static MyAdapter adapter;

    ListView cloud_storage_list ;
    OnItemClickListener listener;
    Cloud mCloud = null;


    static {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put(CLOUD_STORAGE_NAME, "Dropbox");
        map.put(CLOUD_STORAGE_ICON, R.drawable.dropbox);
        mData.add(map);

        map = new HashMap<String, Object>();
        map.put(CLOUD_STORAGE_NAME, "Baidu");
        map.put(CLOUD_STORAGE_ICON, R.drawable.baidupcs);
        mData.add(map);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cloud_storage);

        AccessTokenManager.initToken(this);

        cloud_storage_list = (ListView)this.findViewById(R.id.cloud_storage_list);
        adapter = new MyAdapter(this);
        cloud_storage_list.setAdapter(adapter);
        listener = new OnItemClickListener(){

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
                                    long arg3) {

                    switch(arg2){
                        case 0:
                            mCloud = new Dropbox(CloudBackupActivity.this);
                            break;
                        case 1:
                            mCloud = new Baidu();
                            break;
                        default:
                            mCloud = null;
                    }
                    if ( null!=mCloud ) {
                        mCloud.backup();
                    }
            }
        };
        cloud_storage_list.setOnItemClickListener(listener);
    }

  /**
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.e(TAG, "requestCode:" + requestCode + ", " + "resultCode:" +
                resultCode);

        if (resultCode == Activity.RESULT_OK && null!=mCloud) {
            mCloud.save();
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }**/

    @Override
    protected void onResume() {
        super.onResume();
        if (mCloud instanceof mobisocial.musubi.cloudstorage.dropbox.Dropbox) {
            ((mobisocial.musubi.cloudstorage.dropbox.Dropbox) mCloud)
                    .resumeFromAuth();
        }
    }

    private final class ViewHolder{
        public ImageView cloud_storage_icon;
        public TextView cloud_storage_name;
        public ImageView cloud_storage_lose;
    }

    private class MyAdapter extends BaseAdapter{

        private LayoutInflater mInflater;


        public MyAdapter(Context context){
            this.mInflater = LayoutInflater.from(context);
        }
        @Override
        public int getCount() {
            // TODO Auto-generated method stub
            return mData.size();
        }

        @Override
        public Object getItem(int arg0) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public long getItemId(int arg0) {
            // TODO Auto-generated method stub
            return arg0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // TODO Auto-generated method stub
            ViewHolder holder = null;

            // cloud_storage_lose is the cross to remove the storage link, it
            // can be used to show the last back-up stamp -- Charlie
            if (convertView == null) {

                holder=new ViewHolder();

                convertView = mInflater.inflate(R.layout.cloud_storage_item, null);
                holder.cloud_storage_icon = (ImageView)convertView.findViewById(R.id.cloud_storage_icon);
                holder.cloud_storage_name = (TextView)convertView.findViewById(R.id.cloud_storage_name);
                holder.cloud_storage_lose = (ImageView)convertView.findViewById(R.id.cloud_storage_lose);
                convertView.setTag(holder);

            }else {

                holder = (ViewHolder)convertView.getTag();
            }


            holder.cloud_storage_icon.setBackgroundResource((Integer) mData
                    .get(position).get(CLOUD_STORAGE_ICON));
            holder.cloud_storage_name.setText((String) mData.get(position)
                    .get(CLOUD_STORAGE_NAME));

            // If last backup timestamp is available, change it
            holder.cloud_storage_lose.setVisibility(View.INVISIBLE);

            return convertView;
        }
    }
}
