package mobisocial.musubi.cloudstorage;

import android.content.Context;

import org.json.JSONObject;

import mobisocial.musubi.model.MObject;

/**
 * This class stores login credentials and handles things like
 * cloud storage authentication.
 */

public interface CloudStorage {
	void setAccount(Context context);
	void saveMessages(JSONObject object);

    /**
     * Login to the cloud storage
     * @param context the context
     * @param resultcode the result code from cloud storage login
     */
	void login(Context context, int resultcode);

    /**
     * logout from the cloud storage
     */
	void logout(Context context);
	boolean hasLinkedAccount(Context mContext);
	void saveMessages(MObject object, Context context);
	void saveMessages(MObject object);
}
