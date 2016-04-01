package mobisocial.musubi.test;


import android.util.Base64;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

/*
 * by haoyuheng
 * JsonGernerator is class to gernerate the sig_key and the cypto_key
 * instead of get if from the server
 * here just used to test.
 */


public class JsonGernerator {

	public static final String TAG = "JsonGernerator";
	public static JSONObject GerneratorAphidResult(long time){
		JSONObject jsonObj = new JSONObject();
		try {
			jsonObj.put("sig_key", Base64.encodeToString("sig_key1234".getBytes(),Base64.DEFAULT));
			jsonObj.put("crypto_key", Base64.encodeToString("crypto_key1234".getBytes(),Base64.DEFAULT));
			jsonObj.put("time", time);
		} catch (JSONException e) {
			Log.e(TAG, e.toString());
		}
		return jsonObj;
	}
	
}
