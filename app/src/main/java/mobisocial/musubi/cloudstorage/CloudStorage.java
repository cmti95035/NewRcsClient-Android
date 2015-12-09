package mobisocial.musubi.cloudstorage;

import android.content.Context;

import org.json.JSONObject;

import mobisocial.musubi.model.MObject;

public interface CloudStorage {
	void setAccount(Context context);
	void saveMessages(JSONObject object);
	void login(Context context, int resultcode);
	void logout(Context context);
	boolean hasLinkedAccount(Context mContext);
	void saveMessages(MObject object, Context context);
	void saveMessages(MObject object);
}
