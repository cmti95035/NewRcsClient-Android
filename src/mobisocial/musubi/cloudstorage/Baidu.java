package mobisocial.musubi.cloudstorage;

import com.baidu.oauth.BaiduOAuth;
import com.baidu.oauth.BaiduOAuth.BaiduOAuthResponse;
import com.baidu.pcs.BaiduPCSActionInfo;
import com.baidu.pcs.BaiduPCSClient;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
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
	public void SetAccount(Context context, String appKey, String appSecret) {
		// TODO Auto-generated method stub
		mbUiThreadHandler = new Handler();
		mbApiKey = appKey;
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

					CloudStorageActivity.accessTokenManager.storeToken(response);
					
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
	public void SaveMeseages() {
		// TODO Auto-generated method stub
		
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
		    				CloudStorageActivity.accessTokenManager.clearToken();
	    					Toast.makeText(context.getApplicationContext(), "Logout " + ret, Toast.LENGTH_SHORT).show();
		    			}
		    		});	
		    		
				}
    		});
    		
    		workThread.start();
    	}
	}
	
	 private void mkdir(){
	    	if(null != mbOauth){
	    		Thread workThread = new Thread(new Runnable(){
					public void run() {
			    		BaiduPCSClient api = new BaiduPCSClient();
			    		api.setAccessToken(mbOauth);
			    		String path = mbRootPath + "/" + "JakeDu";
			    		final BaiduPCSActionInfo.PCSFileInfoResponse ret = api.makeDir(path);			    		
					}
				});				 
	    		workThread.start();
	    	}
	 }

	@Override
	public boolean hasLinkedAccount() {
		// TODO Auto-generated method stub
		if(CloudStorageActivity.accessTokenManager.isSessionVaild()){
			return true;
		}
		return false;
	}

}
