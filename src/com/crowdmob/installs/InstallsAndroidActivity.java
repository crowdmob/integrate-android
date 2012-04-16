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
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

public class InstallsAndroidActivity extends Activity {
	private static final String PREFS_NAME = "InstallsAndroidPrefsFile";
	private static final String TAG = "InstallsAndroid";
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        // Register this Android app installation with CrowdMob.  Only register on the first run of this app.
        if (isFirstRun()) {
        	String macAddress = getMacAddress();
        	String macAddressHash = hashMacAddress(macAddress);
        	new RegisterWithCrowdMob().execute(macAddressHash);
			// completedFirstRun();
        }
    }
    
    private boolean isFirstRun() {
    	Log.d(TAG, "has app been run before?");
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        boolean firstRun = settings.getBoolean("firstRun", true);
        Log.d(TAG, firstRun ? "app hasn't been run before" : "app has been run before");
        return firstRun;
    }
    
    private void completedFirstRun() {
    	Log.d(TAG, "successfully completed first run");
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
    	SharedPreferences.Editor editor = settings.edit();
    	editor.putBoolean("firstRun", false);
    	editor.commit();
    	Log.d(TAG, "saved successful completion of first run");
    }
    
    private String getMacAddress() {
    	Log.d(TAG, "getting wifi manager");
    	WifiManager wifiManager = (WifiManager) this.getSystemService(Context.WIFI_SERVICE);
    	Log.d(TAG, "got wifi manager: " + wifiManager);
    	
    	Log.d(TAG, "getting wifi info");
    	WifiInfo wifiInfo = wifiManager.getConnectionInfo();
    	Log.d(TAG, "got wifi info: " + wifiInfo);
    	
    	Log.d(TAG, "getting MAC address");
    	String macAddress = wifiInfo.getMacAddress();
    	Log.d(TAG, "got MAC address: " + macAddress);

    	if (macAddress == null) {
    		macAddress = "";
    	}
    	return macAddress;
    }

    private String hashMacAddress(String macAddress) {
    	String macAddressHash = "";
		try {
			Log.d(TAG, "getting digest instance");
			MessageDigest digest = java.security.MessageDigest.getInstance("SHA-256");
			Log.d(TAG, "got digest instance: " + digest);
			
            Log.d(TAG, "creating message digest");
            byte messageDigest[] = digest.digest(macAddress.getBytes());
            Log.d(TAG, "created message digest: " + messageDigest);
            
            Log.d(TAG, "creating hex string buffer");
            StringBuffer hexString = new StringBuffer();
            Log.d(TAG, "created hex string buffer: " + hexString);
            
            Log.d(TAG, "populating hex string buffer");
            for (int j = 0; j < messageDigest.length; j++) {
                hexString.append(Integer.toHexString(0xFF & messageDigest[j]));
            }
            Log.d(TAG, "populated hex string buffer: " + hexString);
            
            Log.d(TAG, "converting hex string buffer to MAC address hash");
            macAddressHash = hexString.toString();
        	Log.d(TAG, "converted hex string buffer to MAC address hash: " + macAddressHash);
		} catch (NoSuchAlgorithmException e) {
		}
		return macAddressHash;
    }
}

class RegisterWithCrowdMob extends AsyncTask<String, Void, Integer> {
	private static final String CROWDMOB_URL = "https://deals.crowdmob.com/";
	private static final String APP_ID = "0";
	private static final String BID_PRICE_IN_CENTS = "0";
	private static final String TAG = "InstallsAndroid";
	
	@Override
	protected Integer doInBackground(String... macAddressHash) {
		// Issue a POST request with the device's MAC address hash to register the app installation with CrowdMob.
		Log.d(TAG, "registering app installation with CrowdMob");
    	HttpClient client = new DefaultHttpClient();
    	HttpPost post = new HttpPost(CROWDMOB_URL);
    	List<NameValuePair> pairs = new ArrayList<NameValuePair>();
    	pairs.add(new BasicNameValuePair("app_id", APP_ID));
    	pairs.add(new BasicNameValuePair("bid_price_in_cents", BID_PRICE_IN_CENTS));
    	pairs.add(new BasicNameValuePair("mac_address_hash", macAddressHash[0]));
    	Integer statusCode = null;
    	try {
			post.setEntity(new UrlEncodedFormEntity(pairs));
			HttpResponse response = client.execute(post);
			statusCode = response.getStatusLine().getStatusCode();
			Log.i(TAG, "issued POST request, status code: " + statusCode);
		} catch (UnsupportedEncodingException e) {
			Log.e(TAG, "caught UnsupportedEncodingException");
		} catch (ClientProtocolException e) {
			Log.e(TAG, "caught ClientProtocolException");
		} catch (IOException e) {
			Log.e(TAG, "caught IOException");
		}
    	if (statusCode != null) {
    		Log.d(TAG, "registered app installation with CrowdMob");
    	}
    	return statusCode;
	}	
}