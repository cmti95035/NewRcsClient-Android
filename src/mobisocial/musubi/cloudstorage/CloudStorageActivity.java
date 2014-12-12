package mobisocial.musubi.cloudstorage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import mobisocial.musubi.R;

import android.os.Bundle;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class CloudStorageActivity extends Activity {

	 
	
    private static final int REQUEST_LINK_TO_DBX = 0;
    
    private static String baiduApiKey = "jmWK4EfYlQtMpUbWcU2GRlWF"; //your api_key";
    public static String baiduAccessToken = null;
    
    
	public  enum CloudStorage {NONE,DROPBOX,BAIDU,BOX};
	public static CloudStorage CurrentCloudStorage = CloudStorage.NONE;
	ListView cloud_storage_list ;
	static MyAdapter adapter ;
	OnItemClickListener listener;
	private static List<Map<String, Object>> mData;
	
	Baidu baidu = null;
	Dropbox dp = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.cloud_storage);
		
		baidu = new Baidu();
		dp  = new Dropbox();
		
		AccessTokenManager.initToken(this);
		
		
		CurrentCloudStorage = getIsCloudStorageConnected();
		
		cloud_storage_list = (ListView)this.findViewById(R.id.cloud_storage_list);
		mData = getData();
        adapter = new MyAdapter(this);
        cloud_storage_list.setAdapter(adapter);
        listener = new OnItemClickListener(){

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				if(CurrentCloudStorage == CloudStorage.NONE){
					switch(arg2){
					case 0:
						ConnectToDropbox();
						break;
					case 1:
						ConnectToBaidu();
						break;
					case 2:
						ConnectTobox();
						break;
					}
				}
				
			}
        	
        };
		cloud_storage_list.setOnItemClickListener(listener);
	}
	
	protected void ConnectToDropbox() {
		// TODO Auto-generated method stub
		dp.SetAccount(getApplicationContext());
		if (dp.hasLinkedAccount()) {
			setIsCloudStorageConnected(CloudStorage.DROPBOX);  
		}else{
			dp.Login(this,REQUEST_LINK_TO_DBX);
		}
		
	}

	
	@Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
		Log.e("dd",requestCode+" "+resultCode );
		
		
        if (requestCode == REQUEST_LINK_TO_DBX) {
            if (resultCode == Activity.RESULT_OK) {
            	setIsCloudStorageConnected(CloudStorage.DROPBOX);            	
            	Log.e("ddd","Link to Dropbox success.");
            	
            	JSONObject json = new JSONObject();
            	try {
					json.put("test", "fdsafdsafsd");
					dp.SaveMeseages(json);
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
            	
            } else {
                Toast.makeText(this, "Link to Dropbox failed or was cancelled.", 1000);
                Log.e("ddd","Link to Dropbox failed or was cancelled.");
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }


	protected void ConnectToBaidu() {
		// TODO Auto-generated method stub		
		baidu.SetAccount(this);
		baidu.Login(this, 1);		
	}

	protected void ConnectTobox() {
		// TODO Auto-generated method stub
		
	}

	private static List<Map<String, Object>> getData() {
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		Log.i("come here",CurrentCloudStorage.name());
		
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("cloud_storage_name", "Dropbox");
        map.put("cloud_storage_icon", R.drawable.dropbox);
        if(CurrentCloudStorage == CloudStorage.NONE || CurrentCloudStorage == CloudStorage.DROPBOX)
        	list.add(map);
 
        map = new HashMap<String, Object>();
        map.put("cloud_storage_name", "Baidu");
        map.put("cloud_storage_icon", R.drawable.baidupcs);
        if(CurrentCloudStorage == CloudStorage.NONE || CurrentCloudStorage == CloudStorage.BAIDU)
        	list.add(map);
 
        map = new HashMap<String, Object>();
        map.put("cloud_storage_name", "Box");
        map.put("cloud_storage_icon", R.drawable.box);
        if(CurrentCloudStorage == CloudStorage.NONE || CurrentCloudStorage == CloudStorage.BOX)
        	list.add(map);
         
        return list;
	}
	
	
	public final class ViewHolder{
        public ImageView cloud_storage_icon;
        public TextView cloud_storage_name;
        public ImageView cloud_storage_lose;
    }
	
	public class MyAdapter extends BaseAdapter{

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
             
             
            holder.cloud_storage_icon.setBackgroundResource((Integer)mData.get(position).get("cloud_storage_icon"));
            holder.cloud_storage_name.setText((String)mData.get(position).get("cloud_storage_name"));
            holder.cloud_storage_lose.setVisibility(( CurrentCloudStorage != CloudStorage.NONE )?View.VISIBLE:View.INVISIBLE);
             
            holder.cloud_storage_lose.setOnClickListener(new View.OnClickListener() {
                 
                @Override
                public void onClick(View v) {
                	setIsCloudStorageConnected(CloudStorage.NONE);
                	
                }
            });
             
             
            return convertView;
		}
	
	}


	private CloudStorage getIsCloudStorageConnected() {
		// TODO Auto-generated method stub
		dp.SetAccount(getApplicationContext());		
		if (dp.hasLinkedAccount()) {
			return CloudStorage.DROPBOX;
		}
		
		if(baidu.hasLinkedAccount(this)){
			return CloudStorage.BAIDU;
		}
		return CloudStorage.NONE;
	}
	
	private void setIsCloudStorageConnected(CloudStorage storage) {
		// TODO Auto-generated method stub
		
		if(CurrentCloudStorage == CloudStorage.DROPBOX && storage == CloudStorage.NONE){
			dp.SetAccount(getApplicationContext());		
			if (dp.hasLinkedAccount()) {
				dp.Logout(this);
			}
		}
		
		if(CurrentCloudStorage == CloudStorage.BAIDU && storage == CloudStorage.NONE){
			if(baidu.hasLinkedAccount(this))
				baidu.Logout(this);
		}
		CurrentCloudStorage = storage;
		mData = getData();
    	adapter.notifyDataSetChanged();
	}

	public static void setBaiduConnected(boolean connected){
		if(connected){
			CurrentCloudStorage = CloudStorage.BAIDU;
			mData = getData();
	    	adapter.notifyDataSetChanged();
		}else{
			CurrentCloudStorage = CloudStorage.NONE;
			mData = getData();
	    	adapter.notifyDataSetChanged();
		}
	}
	

}
