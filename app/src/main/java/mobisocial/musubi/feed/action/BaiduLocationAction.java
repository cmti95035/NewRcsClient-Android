/*
 * Copyright 2012 The Stanford MobiSocial Laboratory
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package mobisocial.musubi.feed.action;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import mobisocial.musubi.App;
import mobisocial.musubi.Helpers;
import mobisocial.musubi.R;
import mobisocial.musubi.feed.action.CameraAction.CameraCaptureTask;
import mobisocial.musubi.feed.iface.FeedAction;
import mobisocial.musubi.model.DbRelation;
import mobisocial.musubi.model.MApp;
import mobisocial.musubi.model.helpers.AppManager;
import mobisocial.musubi.obj.ObjHelpers;
import mobisocial.musubi.obj.action.EditPhotoAction.EditCallout;
import mobisocial.musubi.objects.AppObj;
import mobisocial.musubi.objects.PictureObj;
import mobisocial.musubi.objects.VideoObj;
import mobisocial.musubi.service.WizardStepHandler;
import mobisocial.musubi.ui.fragments.AppSelectDialog;
import mobisocial.musubi.ui.fragments.AppSelectDialog.MusubiWebApp;
import mobisocial.musubi.ui.util.IntentProxyActivity;
import mobisocial.musubi.util.ActivityCallout;
import mobisocial.musubi.util.InstrumentedActivity;
import mobisocial.musubi.util.ObjFactory;
import mobisocial.musubi.util.PhotoTaker;
import mobisocial.musubi.util.UriImage;
import mobisocial.musubi.util.BaiduLocation.MyLocationListener;
import mobisocial.socialkit.Obj;
import mobisocial.socialkit.musubi.DbObj;
import mobisocial.socialkit.musubi.Musubi;
import mobisocial.socialkit.obj.MemObj;

import org.json.JSONException;
import org.json.JSONObject;
import org.mobisocial.corral.ContentCorral;
import org.mobisocial.corral.CorralDownloadClient;

import android.app.Activity;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.location.LocationClientOption.LocationMode;


/**
 * Captures an image to share with a feed.
 *
 */
public class BaiduLocationAction extends FeedAction {
    private static final String TAG = "BaiduLocationAction";

    private Uri mFeedUri;
    private String mType;
    
    private LocationMode tempMode = LocationMode.Hight_Accuracy;
	private String tempcoor="gcj02";
	
	public LocationClient mLocationClient;
	public MyLocationListener mMyLocationListener;
	
	

    @Override
    public String getName() {
        return "Camera";
    }

    @Override
    public Drawable getIcon(Context c) {
        return c.getResources().getDrawable(R.drawable.ic_attach_baidu_location_holo_light);
    }

    @Override
    public void onClick(final Context context, final Uri feedUri) {
        mFeedUri = feedUri;
        mLocationClient = new LocationClient(context.getApplicationContext());
		mMyLocationListener = new MyLocationListener();
		mLocationClient.registerLocationListener(mMyLocationListener);
		InitLocation();
		mLocationClient.start();
		Toast.makeText(context.getApplicationContext(),"Locating...", Toast.LENGTH_LONG).show();
    }
    
	private void InitLocation(){
		LocationClientOption option = new LocationClientOption();
		option.setLocationMode(tempMode);//���ö�λģʽ
		option.setCoorType(tempcoor);//���صĶ�λ����ǰٶȾ�γ�ȣ�Ĭ��ֵgcj02
		option.setScanSpan(5000);//���÷���λ����ļ��ʱ��Ϊ5000ms
		option.setIsNeedAddress(true);
		mLocationClient.setLocOption(option);
		
	}
	

	/**
	 * ʵ��ʵλ�ص�����
	 */
	public class MyLocationListener implements BDLocationListener {

		@Override
		public void onReceiveLocation(BDLocation location) {
			//Receive Location 
			StringBuffer sb = new StringBuffer(256);
			sb.append("time : ");
			sb.append(location.getTime());
			sb.append("\nerror code : ");
			sb.append(location.getLocType());
			sb.append("\nlatitude : ");
			sb.append(location.getLatitude());
			sb.append("\nlontitude : ");
			sb.append(location.getLongitude());
			sb.append("\nradius : ");
			sb.append(location.getRadius());
			if (location.getLocType() == BDLocation.TypeGpsLocation){
				sb.append("\nspeed : ");
				sb.append(location.getSpeed());
				sb.append("\nsatellite : ");
				sb.append(location.getSatelliteNumber());
				sb.append("\ndirection : ");
				sb.append("\naddr : ");
				sb.append(location.getAddrStr());
				sb.append(location.getDirection());
			} else if (location.getLocType() == BDLocation.TypeNetWorkLocation){
				sb.append("\naddr : ");
				sb.append(location.getAddrStr());
				//��Ӫ����Ϣ
				sb.append("\noperationers : ");
				sb.append(location.getOperators());
			}
			
			 new BaiduLocationTask(sb.toString()).execute();
			 mLocationClient.stop();
			Log.i("BaiduLocationApiDem", sb.toString());
		}
	}
	

    @Override
    public boolean isActive(Context c) {
        return true;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, final Intent data) {
        
        super.onActivityResult(requestCode, resultCode, data);
    }
    
    
    class BaiduLocationTask extends AsyncTask<Void, Void, Boolean> {
        Throwable mError;
        Obj mObj;
        String mComment;

        public BaiduLocationTask(String comment) {
            mComment = comment;
            mObj = null;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            
        	if (mComment.length() > 0) {
                if (Patterns.WEB_URL.matcher(mComment.trim()).matches()) {
                    // TODO: proper progress notification using async task..
                    
                }
                mObj = ObjFactory.objForText(mComment);
        	}
            

            if (mObj == null) {
                return false;
            }

            Helpers.sendToFeed(getActivity(), mObj, mFeedUri);
            return true;
        }

        @Override
        protected void onPostExecute(Boolean result) {
        	
            if (result) {
                Helpers.emailUnclaimedMembers(getActivity(), mObj, mFeedUri);
            } else {
                Toast.makeText(getActivity(), "Failed to send location.",
                        Toast.LENGTH_SHORT).show();
            }
        }
        
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null) {
            mFeedUri = savedInstanceState.getParcelable("feed");
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelable("feed", mFeedUri);
    }
	@Override
	public void onClick(Context context, Uri mFeedUri, boolean mIsFeedSnap) {
		// TODO Auto-generated method stub
		onClick(context, mFeedUri);
		
	}
}
