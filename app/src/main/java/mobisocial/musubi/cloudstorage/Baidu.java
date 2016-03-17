package mobisocial.musubi.cloudstorage;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

import com.baidu.oauth.BaiduOAuth;
import com.baidu.oauth.BaiduOAuth.BaiduOAuthResponse;
import com.baidu.pcs.BaiduPCSActionInfo;
import com.baidu.pcs.BaiduPCSClient;
import com.baidu.pcs.BaiduPCSStatusListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

import mobisocial.musubi.model.MObject;
import mobisocial.musubi.objects.PictureObj;

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
	public void setAccount(Context context) {
		// TODO Auto-generated method stub
		mbUiThreadHandler = new Handler();
	}

	@Override
	/**
	 * Handles Baidu cloud storage authentication.
	 */
	public void login(final Context context, int resultcode) {		// TODO
	// Auto-generated method stub

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
	/**
	 * Logout from Baidu cloud storage.
	 */
	public void logout(final Context context) {
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
	
	@Override
	public boolean hasLinkedAccount(Context mContext) {
		// TODO Auto-generated method stub
		return AccessTokenManager.isSessionVaild(mContext);
	}

	
	 public  void writeTxtFile(String strcontent, File file)
	 {
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
	 
	 public String uriToFilePath(String myImageUrl, Context context){
	     Uri uri = Uri.parse(myImageUrl);
	     
	 
	     String[] proj = { MediaStore.Images.Media.DATA };   
	     Cursor actualimagecursor = context.getContentResolver().query(uri,proj,null,null,null);  
	     int actual_image_column_index = actualimagecursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);   
	     actualimagecursor.moveToFirst();   
	     
	 
	     return actualimagecursor.getString(actual_image_column_index); 
	 }
	
	@Override
	public void saveMessages(final MObject object,final Context context) {
		
		
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

					writeTxtFile(object.GetCloudJson().toString(), file);
					
		    		BaiduPCSClient api = new BaiduPCSClient();
		    		api.setAccessToken(mbOauth);
		    		String path = "/"+object.feedId_+"/"+object.timestamp_+"" +
							".json";
					
		    		
		    		final BaiduPCSActionInfo.PCSFileInfoResponse response = api.uploadFile(file.getAbsolutePath(), mbRootPath + path, new BaiduPCSStatusListener(){

						@Override
						public void onProgress(long bytes, long total) {
							// TODO Auto-generated method stub

				    		Log.e("BAodu", "total: " + total + "    sent:" +
									bytes);
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
							//String imagepath = uriToFilePath(uri,context);
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
						    		Log.e("BAodu", "total: " + total + "    " +
											"sent:" + bytes);
						    							
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
	public void saveMessages(JSONObject object) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void saveMessages(MObject object) {
		// TODO Auto-generated method stub

	}

	public void saveImages(MObject object, String absolutePath) {
		// TODO Auto-generated method stub
		
	}
}
