package mobisocial.musubi.cloudstorage;

import com.dropbox.sync.android.DbxAccountManager;

import android.app.Activity;
import android.content.Context;
import android.widget.Button;
import android.widget.TextView;


public class Dropbox implements CloudStorage {

	

	private DbxAccountManager mDbxAcctMgr;
	
	public Dropbox(){
		
	}
	
	@Override
	public void SetAccount(Context context, String appKey, String appSecret) {
		// TODO Auto-generated method stub
		mDbxAcctMgr = DbxAccountManager.getInstance(context, appKey, appSecret);
	}
	
	@Override
	public void Login(Context context,int resultcode) {
		// TODO Auto-generated method stub
		mDbxAcctMgr.startLink((Activity)context, resultcode);
	}
	
	@Override
	public void SaveMeseages() {
		// TODO Auto-generated method stub
		
	}

	public boolean hasLinkedAccount() {
		// TODO Auto-generated method stub
		return mDbxAcctMgr.hasLinkedAccount();
	}

	public void Logout(Context context) {
		// TODO Auto-generated method stub
		mDbxAcctMgr.unlink();
	}

}
