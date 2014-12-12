package mobisocial.musubi.cloudstorage;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

import mobisocial.musubi.model.MObject;
import mobisocial.musubi.objects.PictureObj;

import org.codehaus.jackson.map.ext.JodaDeserializers.DateTimeDeserializer;
import org.json.JSONException;
import org.json.JSONObject;

import com.baidu.oauth.BaiduOAuth;
import com.baidu.oauth.BaiduOAuth.BaiduOAuthResponse;
import com.baidu.pcs.BaiduPCSActionInfo;
import com.baidu.pcs.BaiduPCSClient;
import com.baidu.pcs.BaiduPCSStatusListener;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

public class Baidu implements CloudStorage{

	// the api key
    /*
     * mbApiKey should be your app_key, please instead of "your api_key"
     */
    private static String mbApiKey = "jmWK4EfYlQtMpUbWcU2GRlWF"; //your api_key";
    
    // the default root folder
    /*
     * mbRootPath should be your app_path, please instead of "/apps/pcstest_oauth"
     */
    private final static String mbRootPath =  "/apps/musubi";
	
    private String mbOauth = null;
    // the handler
    private Handler mbUiThreadHandler = null;
	@Override
	public void SetAccount(Context context) {
		// TODO Auto-generated method stub
		mbUiThreadHandler = new Handler();
	}

	@Override
	public void Login(final Context context, int resultcode) {		// TODO Auto-generated method stub
	   
		BaiduOAuth oauthClient = new BaiduOAuth();		
		oauthClient.startOAuth(context, mbApiKey, new BaiduOAuth.OAuthListener() {
			@Override
			public void onException(String msg) {
				Toast.makeText(context.getApplicationContext(), "Login failed " + msg, Toast.LENGTH_SHORT).show();
			}
			@Override
			public void onComplete(BaiduOAuthResponse response) {
				if(null != response){
					mbOauth = response.getAccessToken();					

					AccessTokenManager.storeToken(response);
					
					CloudStorageActivity.baiduAccessToken = mbOauth;
					CloudStorageActivity.setBaiduConnected(true);
					Toast.makeText(context.getApplicationContext(), "Token: " + mbOauth + "    User name:" + response.getUserName(), Toast.LENGTH_SHORT).show();
				}
			}
			@Override
			public void onCancel() {
				Toast.makeText(context.getApplicationContext(), "Login cancelled", Toast.LENGTH_SHORT).show();
			}
		});
	}

	

	@Override
	public void Logout(final Context context) {
		// TODO Auto-generated method stub
		if(null != mbOauth){    	
    	    /**
    	     * you can call this method to logout in Android 2.X
    	     */
    		Thread workThread = new Thread(new Runnable(){
				@Override
				public void run() {
					
		    		BaiduOAuth oauth = new BaiduOAuth();
		    	   
		    		final boolean ret = oauth.logout(mbOauth);
		    		mbUiThreadHandler.post(new Runnable(){
		    			@Override
						public void run(){
		    				CloudStorageActivity.baiduAccessToken = null;
		    				AccessTokenManager.clearToken();
	    					Toast.makeText(context.getApplicationContext(), "Logout " + ret, Toast.LENGTH_SHORT).show();
		    			}
		    		});	
		    		
				}
    		});
    		
    		workThread.start();
    	}
	}
	
	 private void mkdir(final String path1){
	    	if(null != mbOauth){
	    		Thread workThread = new Thread(new Runnable(){
					public void run() {
			    		BaiduPCSClient api = new BaiduPCSClient();
			    		api.setAccessToken(mbOauth);
			    		String path = mbRootPath + "/" + path1;
			    		final BaiduPCSActionInfo.PCSFileInfoResponse ret = api.makeDir(path);			    		
					}
				});				 
	    		workThread.start();
	    	}
	 }

	@Override
	public boolean hasLinkedAccount(Context mContext) {
		// TODO Auto-generated method stub
		if(AccessTokenManager.isSessionVaild(mContext)){
			return true;
		}
		return false;
	}

	
	 public  void WriteTxtFile(String strcontent,File file)
	 {
	      //每次写入时，都换行写
	      String strContent=strcontent+"\n";
	      try {
	    	   
	           RandomAccessFile raf = new RandomAccessFile(file, "rw");
	           raf.seek(file.length());
	           raf.write(strContent.getBytes());
	           raf.close();
	      } catch (Exception e) {
	           Log.e("TestFile", "Error on write File."+e.getMessage());
	          }
	 }
	 
	 public String UriToFilePath(String myImageUrl, Context context){
	     Uri uri = Uri.parse(myImageUrl);
	     
	 
	     String[] proj = { MediaStore.Images.Media.DATA };   
	     Cursor actualimagecursor = context.getContentResolver().query(uri,proj,null,null,null);  
	     int actual_image_column_index = actualimagecursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);   
	     actualimagecursor.moveToFirst();   
	     
	 
	     return actualimagecursor.getString(actual_image_column_index); 
	 }
	
	@Override
	public void SaveMeseages(final MObject object,final Context context) {
		
		
		// TODO Auto-generated method stub
		mbOauth = AccessTokenManager.getAccessToken();
    	if(null != mbOauth){

    		Thread workThread = new Thread(new Runnable(){
				public void run() {
					
				   String filename = System.currentTimeMillis()+".json";
		    	   final File file = new File(context.getFilesDir(),filename);
		           if (!file.exists()) {
		            Log.d("TestFile", "Create the file:" + filename);
		            try {
						file.createNewFile();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
		           }
					//String tmpFile = "/mnt/sdcard/zzzz.jpg";
				    
					WriteTxtFile(object.GetCloudJson().toString(),file);
					
		    		BaiduPCSClient api = new BaiduPCSClient();
		    		api.setAccessToken(mbOauth);
		    		String path = "/";
		    		path = "/"+object.feedId_+"/"+object.timestamp_+".json";
					
		    		
		    		final BaiduPCSActionInfo.PCSFileInfoResponse response = api.uploadFile(file.getAbsolutePath(), mbRootPath + path, new BaiduPCSStatusListener(){

						@Override
						public void onProgress(long bytes, long total) {
							// TODO Auto-generated method stub
							
							
							final long bs = bytes;
							final long tl = total;

				    		Log.e("BAodu", "total: " + tl + "    sent:" + bs);
				    		file.delete();					
						}
						
						@Override
						public long progressInterval(){
							return 1000;
						}
		    		});
		    		
		    		if(object.type_.equals(PictureObj.TYPE)){
		    			
		    			Log.e("BAIdu",object.json_);
		    			try {
							String imagepath =  new JSONObject(object.json_).getString("AppPath");
							Log.e("BAIdu",imagepath);
							//String imagepath = UriToFilePath(uri,context);
							//Log.e("BAIdu",imagepath);
							File image = new File(imagepath);
							
							String imagename = image.getName();
							String prefix=imagename.substring(imagename.lastIndexOf(".")+1); 
							path = "/"+object.feedId_+"/"+object.timestamp_+"."+prefix;
							Log.e("BAIdu",path);
							final BaiduPCSActionInfo.PCSFileInfoResponse response1 = api.uploadFile(imagepath, mbRootPath + path, new BaiduPCSStatusListener(){

								@Override
								public void onProgress(long bytes, long total) {
									// TODO Auto-generated method stub
									
									
									final long bs = bytes;
									final long tl = total;

						    		Log.e("BAodu", "total: " + tl + "    sent:" + bs);
						    							
								}
								
								@Override
								public long progressInterval(){
									return 1000;
								}
				    		});
						} catch (JSONException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
		    			
		    			
		    		}
		    		
				}
			});
			 
    		workThread.start();
    	}
		
	}

	@Override
	public void SaveMeseages(JSONObject object) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void SaveMeseages(MObject object) {
		// TODO Auto-generated method stub
		
	}

	public void SaveImages(MObject object, String absolutePath) {
		// TODO Auto-generated method stub
		
	}


}
