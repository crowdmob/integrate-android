package com.crowdmob.installs;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

public class InstallsAndroidActivity extends Activity {
	public static final String CROWDMOB_URL = "https://deals.crowdmob.com/";	//
	public static final String APP_ID = "0";									//
	public static final String BID_PRICE_IN_CENTS = "0";						//
	public static final String PREFS_NAME = "InstallsAndroidPrefsFile";			// Shared preferences name.
	public static final String TAG = "InstallsAndroid";							//

	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        // Register this Android app installation with CrowdMob.  Only register on the first run of this app.
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        boolean firstRun = settings.getBoolean("firstRun", true);
        if (!firstRun) {
        	Log.i(TAG, "app has been run before; not registering with CrowdMob");
        } else {
        	Log.i(TAG, "app hasn't been run before; registering with CrowdMob");
        	
        	// Get the Android device's MAC address, and MD5 hash it.
        	Log.d(TAG, "getting wifi manager");
        	WifiManager wifiManager = (WifiManager) this.getSystemService(Context.WIFI_SERVICE);
        	Log.d(TAG, "getting wifi info");
        	WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        	Log.d(TAG, "getting MAC address");
        	String macAddress = wifiInfo.getMacAddress();
        	Log.i(TAG, "device MAC address: " + macAddress);
        	Log.d(TAG, "hashing MAC address");
        	String macAddressHash = "";
			try {
				MessageDigest digest = java.security.MessageDigest.getInstance("MD5");
	            digest.update(macAddress.getBytes());
	            byte messageDigest[] = digest.digest();
	            StringBuffer hexString = new StringBuffer();
	            for (int j = 0; j < messageDigest.length; j++) {
	                hexString.append(Integer.toHexString(0xFF & messageDigest[j]));
	            }
	            macAddressHash = hexString.toString();
	        	Log.i(TAG, "device MAC address hash: " + macAddressHash);
			} catch (NoSuchAlgorithmException e) {
			}

			// Issue a POST request with the device's MAC address hash to register the app installation with CrowdMob.
			if (!TextUtils.isEmpty(macAddressHash)) {
	        	HttpClient client = new DefaultHttpClient();
	        	HttpPost post = new HttpPost(CROWDMOB_URL);
	        	List<NameValuePair> pairs = new ArrayList<NameValuePair>();
	        	pairs.add(new BasicNameValuePair("app_id", APP_ID));
	        	pairs.add(new BasicNameValuePair("bid_price_in_cents", BID_PRICE_IN_CENTS));
	        	pairs.add(new BasicNameValuePair("mac_address_hash", macAddressHash));
	        	try {
					post.setEntity(new UrlEncodedFormEntity(pairs));
					HttpResponse response = client.execute(post);
					Integer statusCode = response.getStatusLine().getStatusCode();
					Log.i(TAG, "issued POST request, status code: " + statusCode);
				} catch (UnsupportedEncodingException e) {
				} catch (ClientProtocolException e) {
				} catch (IOException e) {
				}
			}
			
        	// SharedPreferences.Editor editor = settings.edit();
        	// editor.putBoolean("firstRun", false);
        	// editor.commit();
        }
    }
}