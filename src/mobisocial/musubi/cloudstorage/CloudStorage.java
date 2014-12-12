package mobisocial.musubi.cloudstorage;

import mobisocial.musubi.model.MObject;

import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;

public interface CloudStorage {
	public void SetAccount(Context context);
	public void SaveMeseages(JSONObject object);
	void Login(Context context, int resultcode);
	void Logout(Context context);
	boolean hasLinkedAccount(Context mContext);
	void SaveMeseages(MObject object, Context context);
	void SaveMeseages(MObject object);
}
