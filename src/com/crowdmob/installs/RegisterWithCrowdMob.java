/*---------------------------------------------------------------------------*\
 |  RegisterWithCrowdMob.java                                                |
 |                                                                           |
 |  Copyright (c) 2012, CrowdMob, Inc., original authors.                    |
 |                                                                           |
 |      File created/modified by:                                            |
 |          Raj Shah <raj@crowdmob.com>                                      |
\*---------------------------------------------------------------------------*/

package com.crowdmob.installs;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
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

import android.content.Context;
import android.content.SharedPreferences;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;

public class RegisterWithCrowdMob {
	private static final String DELIMITER = ",";
	private static final String TAG = "RegisterWithCrowdMob";

	public static void trackAppInstallation(Context context, String privateKey, String publicKey, String appId, String bidPriceInCents) {
        // Register this Android app installation with CrowdMob.  Only register on the first run of this app.
        if (FirstRun.isFirstRun(context)) {
        	String uuid = UniqueDeviceId.getUniqueDeviceId(context);
        	String securityHash = computeSecurityHash(privateKey, publicKey, appId, bidPriceInCents, uuid);
        	new AsyncRegisterWithCrowdMob().execute(publicKey, appId, bidPriceInCents, uuid, securityHash);
			// FirstRun.completedFirstRun(context);
        }
	}

    private static String computeSecurityHash(String privateKey, String publicKey, String appId, String bidPriceInCents, String uuid) {
    	final String[] components = {publicKey, appId, bidPriceInCents, uuid};
		String concatenated = TextUtils.join(DELIMITER, components);
		String securityHash = Hash.hash("SHA-256", privateKey, concatenated);
		return securityHash;
    }
}

class FirstRun {
	private static final String PREFS_NAME = "RegisterWithCrowdMobPrefsFile";
	private static final String TAG = "FirstRun";

	static boolean isFirstRun(Context context) {
    	Log.d(TAG, "has app been run before?");
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
        boolean firstRun = settings.getBoolean("firstRun", true);
        Log.d(TAG, firstRun ? "app hasn't been run before" : "app has been run before");
        return firstRun;
    }

    static void completedFirstRun(Context context) {
    	Log.d(TAG, "successfully completed first run");
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
    	SharedPreferences.Editor editor = settings.edit();
    	editor.putBoolean("firstRun", false);
    	editor.commit();
    	Log.d(TAG, "saved successful completion of first run");
    }
}

class UniqueDeviceId {
	private static final String TAG = "UniqueDeviceId";

	class Strategies {
		private Context context;

		public Strategies(Context context) {
			this.context = context;
		}

		String androidId() {
			Log.i(TAG, "trying to get Android ID");
			return null;
		}
	
		String serialNumber() {
			Log.i(TAG, "trying to get serial number");
			return null;
		}
	
		String macAddressHash() {
			Log.i(TAG, "trying to get MAC address hash");

	    	Log.d(TAG, "getting wifi manager");
	    	WifiManager wifiManager = (WifiManager) this.context.getSystemService(Context.WIFI_SERVICE);
	    	Log.d(TAG, "got wifi manager: " + wifiManager);
	    	
	    	Log.d(TAG, "getting wifi info");
	    	WifiInfo wifiInfo = wifiManager.getConnectionInfo();
	    	Log.d(TAG, "got wifi info: " + wifiInfo);
	    	
	    	Log.d(TAG, "getting MAC address");
	    	String macAddress = wifiInfo.getMacAddress();
	    	Log.d(TAG, "got MAC address: " + macAddress);
	
	    	if (macAddress == null) {
	    		Log.w(TAG, "couldn't get MAC address (wifi disabled?)");
	    		return null;
	    	}
	
	       	Log.d(TAG, "hashing MAC address");
	    	String macAddressHash = Hash.hash("SHA-256", "", macAddress);
	       	Log.d(TAG, "hashed MAC address: " + macAddressHash);

	       	Log.i(TAG, "got MAC address hash");
	    	return macAddressHash;
		}
	
		String deviceId() {
			Log.i(TAG, "trying to get device ID");
			return null;
		}

		String getTelephonyId() {
			Log.i(TAG, "trying to get telephony ID");
			return null;
		}
	}

	static String getUniqueDeviceId(Context context) {
		// For more info, see: http://android-developers.blogspot.com/2011/03/identifying-app-installations.html
		Strategies strategies = new UniqueDeviceId().new Strategies(context);
		String uniqueDeviceId = null;
		for (Method method : strategies.getClass().getDeclaredMethods()) {
			try {
				method.invoke(strategies);
			} catch (InvocationTargetException e) {
				e.getTargetException().printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
			if (uniqueDeviceId != null) {
				break;
			}
		}
		return uniqueDeviceId;
	}
}

class Hash {
	private static final String TAG = "Hash";

    static String hash(String algorithm, String salt, String message) {
    	Log.d(TAG, algorithm + " hashing salt " + salt + " and message " + message);
    	MessageDigest digest = null;
    	try {
    		digest = MessageDigest.getInstance(algorithm);
    	} catch (NoSuchAlgorithmException e) {
    		Log.w(TAG, "no such algorithm: " + algorithm);
    		e.printStackTrace();
    		return null;
    	}
    	digest.reset();
    	if (salt.length() > 0) {
    		digest.update(salt.getBytes());
    	}
    	byte[] messageDigest = digest.digest(message.getBytes());

    	// Convert the message digest to hex.  For more info, see: http://stackoverflow.com/a/332101
    	StringBuffer hexBuffer = new StringBuffer();
    	for (int j = 0; j < messageDigest.length; j++) {
    		String hexByte = Integer.toHexString(0xFF & messageDigest[j]);
    		if (hexByte.length() == 1) {
    			hexBuffer.append("0");
    		}
    		hexBuffer.append(hexByte);
    	}
    	String hexString = hexBuffer.toString();
    	Log.d(TAG, algorithm + " hashed to hex string " + hexString);
    	return hexString;
    }
}

class AsyncRegisterWithCrowdMob extends AsyncTask<String, Void, Integer> {
	private static final String CROWDMOB_URL = "https://deals.crowdmob.com/loot/installs";	// Over HTTPS.
	private static final String TAG = "AsyncRegisterWithCrowdMob";

	@Override
	protected Integer doInBackground(String... params) {
		String publicKey = params[0];
		String appId = params[1];
		String bidPriceInCents = params[2];
		String uuid = params[3];
		String securityHash = params[4];

		// Issue a POST request to register the app installation with CrowdMob.
		Log.d(TAG, "registering app installation with CrowdMob");
    	HttpClient client = new DefaultHttpClient();
    	HttpPost post = new HttpPost(CROWDMOB_URL);
    	List<NameValuePair> pairs = new ArrayList<NameValuePair>();
    	pairs.add(new BasicNameValuePair("public_key", publicKey));
    	pairs.add(new BasicNameValuePair("app_id", appId));
    	pairs.add(new BasicNameValuePair("bid_price_in_cents", bidPriceInCents));
    	pairs.add(new BasicNameValuePair("uuid", uuid));
    	pairs.add(new BasicNameValuePair("security_hash", securityHash));
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
			Log.e(TAG, "caught IOException (no internet access?)");
		}
    	if (statusCode != null) {
    		Log.d(TAG, "registered app installation with CrowdMob, HTTP status code " + statusCode);
    	}
    	return statusCode;
	}	
}