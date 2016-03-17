package mobisocial.musubi.cloudstorage;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import com.chinamobile.cloudStorageProxy.server.ActionsRequestBuilders;
import com.chinamobile.cloudStorageProxy.server.BackupRecord;
import com.chinamobile.cloudStorageProxy.server.EncryptionKey;
import com.linkedin.r2.RemoteInvocationException;
import com.linkedin.r2.transport.common.Client;
import com.linkedin.r2.transport.common.bridge.client.TransportClientAdapter;
import com.linkedin.r2.transport.http.client.HttpClientFactory;
import com.linkedin.restli.client.ActionRequest;
import com.linkedin.restli.client.Response;
import com.linkedin.restli.client.ResponseFuture;
import com.linkedin.restli.client.RestClient;

import java.util.Collections;
import java.util.Date;

import mobisocial.musubi.ui.SettingsActivity;
import mobisocial.musubi.ui.fragments.IpSetDialog;

/**
 * Common utils for backup data to cloud storage and restore data from cloud
 * storage.
 */
public class Utils {

    private static ActionsRequestBuilders actionsRequestBuilders = new
            ActionsRequestBuilders();
    private static final String TAG = "Utils";
    private static final HttpClientFactory http = new HttpClientFactory();
    private static final Client r2Client = new TransportClientAdapter(
            http.getClient(Collections.<String, String>emptyMap()));
    private static final String BASE_URL_HEAD = "http://";
    private static final String BASE_URL_TAIL = ":7777/cloudStorageProxy/";
    private static RestClient restClient;

    private static synchronized RestClient getRestClient(Context context) {
        if (null == restClient) {
            SharedPreferences pref = context.getSharedPreferences(
                    SettingsActivity.PREFS_NAME, Context.MODE_PRIVATE);
            String server = pref.getString(IpSetDialog.ADS_SERVER_IP,
                    "cmti-webrtc.com");
            restClient = new RestClient(r2Client, BASE_URL_HEAD+server+BASE_URL_TAIL);
        }
        return restClient;
    }

    //

    /**
     * Generate an ID. The ID must be persistent through device reset, so we use hardware
     * related properties. The priority is: IMEI/MEID > IMSI > Mac Address
     * @return the ID generated from hardware properties
     */
    public static String getId(Context context) {
        String id;

        TelephonyManager telephonyManager =
                (TelephonyManager) context.getSystemService(Context
                        .TELEPHONY_SERVICE);

        id = telephonyManager.getDeviceId();
        if (id != null) {
            return id;
        }

        id = telephonyManager.getSubscriberId();
        if (id != null) {
            return id;
        }

        WifiManager wifiManager =
                (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        WifiInfo wInfo = wifiManager.getConnectionInfo();
        id = wInfo.getMacAddress();
        return id;
    }

    /**
     * Reqeust an encryption key from the server/
     * @return the encryption key
     */
    public static EncryptionKey requestKey(Context context) {
        String userId = getId(context);
        ActionRequest<EncryptionKey> actionRequest = actionsRequestBuilders
                .actionRequestKey().userIdParam(userId).build();

        try {
            ResponseFuture<EncryptionKey> responseFuture = getRestClient
                    (context)
                    .sendRequest(actionRequest);
            Response<EncryptionKey> response = responseFuture.getResponse();

            EncryptionKey encryptionKey = response.getEntity();
            System.out.println("\nrequestKey returns: " + (encryptionKey ==
                    null ? "null" : encryptionKey));

            return encryptionKey;
        } catch (RemoteInvocationException e) {
            Log.e(TAG, "requestKey: " + e.toString());
            return null;
        }
    }

    /**
     * Once data is successfully backed up to the cloud storage, insert a
     * back up record on the server. So server keep the mapping table for
     * <backup_file, encryption_key>. When we need restore the data, server
     * can retrieve the encryption key accordingly.
     * @param backupRecord the backup record needs to be inserted
     * @return true if backup record has been inserted successfully, false
     * otherwise
     */
    public static Boolean insertBackupRecord(Context context, BackupRecord
                                             backupRecord) {
        ActionRequest<Boolean> actionRequest = actionsRequestBuilders
                .actionInsertBackupRecord().backupRecordParam(backupRecord)
                .build();

        try {
            ResponseFuture<Boolean> responseFuture = getRestClient(context)
                    .sendRequest
                    (actionRequest);
            Response<Boolean> response = responseFuture.getResponse();

            Boolean result = response.getEntity();
            System.out.println("\nrequestKey returns: " + (result == null ?
                    "null" : result));

            return result;
        } catch (RemoteInvocationException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Retrieve a backup record from the server based on user id and time stamp
     * @param userId user ID based on hardware properties
     * @param timestamp the time stamp when the data was backed
     * @return the backup record based on user id and time stamp
     */
    public static BackupRecord retrieveBackupRecord(Context context, String
                                                    userId, Long
            timestamp) {
        ActionRequest<BackupRecord> actionRequest = actionsRequestBuilders
                .actionRetrieveBackupRecord().userIdParam(userId)
                .timestampParam(timestamp).build();

        try {
            ResponseFuture<BackupRecord> responseFuture = getRestClient(context)
                    .sendRequest(actionRequest);
            Response<BackupRecord> response = responseFuture.getResponse();

            BackupRecord backupRecord = response.getEntity();
            System.out.println("\nretrieveBackupRecord returns: " +
                    (backupRecord == null ? "null" : backupRecord));

            return backupRecord;
        } catch (RemoteInvocationException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Parse the file name and retrieve the time stamp
     * @param fileName the backup file name with a time stamp being a part of it
     * @return the time stamp retrieved from the file name
     */
    public static long getTimestamp(String fileName) {
        return Long.parseLong(fileName.substring(fileName.lastIndexOf(".") +
                1, fileName
                .length()));
    }

    public static void showToast(Context context, String msg) {
        Toast error = Toast.makeText(context, msg, Toast.LENGTH_LONG);
        error.show();
    }
}
