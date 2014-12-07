package mobisocial.musubi.cloudstorage;

import android.app.Activity;
import android.content.Context;

public interface CloudStorage {
	public void SetAccount(Context context, String appKey, String appSecret);
	public void SaveMeseages();
	void Login(Context context, int resultcode);
	void Logout(Context context);
	public boolean hasLinkedAccount() ;
}
