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

import mobisocial.musubi.Helpers;
import mobisocial.musubi.R;
import mobisocial.musubi.feed.iface.FeedAction;
import mobisocial.musubi.util.ObjFactory;
import mobisocial.socialkit.Obj;


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
