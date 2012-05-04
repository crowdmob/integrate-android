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
import android.os.Build;
import android.provider.Settings;
import android.provider.Settings.Secure;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;

public class RegisterWithCrowdMob {
	private static final String DELIMITER = ",";
	private static final String TAG = "RegisterWithCrowdMob";

	public static void trackAppInstallation(Context context, String privateKey, String publicKey, String bidPriceInCents) {
        // Register this Android app installation with CrowdMob.  Only register on the first run of this app.
        if (FirstRun.isFirstRun(context)) {
        	String uuid = UniqueDeviceId.getUniqueDeviceId(context);
        	String securityHash = computeSecurityHash(privateKey, publicKey, bidPriceInCents, uuid);
        	new AsyncRegisterWithCrowdMob().execute(publicKey, bidPriceInCents, uuid, securityHash);
			// FirstRun.completedFirstRun(context);
        }
	}

    private static String computeSecurityHash(String privateKey, String publicKey, String bidPriceInCents, String uuid) {
    	final String[] components = {publicKey, bidPriceInCents, uuid};
		String concatenated = TextUtils.join(DELIMITER, components);
		String securityHash = Hash.hash("SHA-256", privateKey, concatenated);
		return securityHash;
    }
}

class FirstRun {
	private static final String PREFS_NAME = "RegisterWithCrowdMobPrefsFile";
	private static final String TAG = "FirstRun";

	static boolean isFirstRun(Context context) {
    	Log.i(TAG, "has app been run before?");
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
        boolean firstRun = settings.getBoolean("firstRun", true);
        Log.i(TAG, firstRun ? "app hasn't been run before" : "app has been run before");
        return firstRun;
    }

    static void completedFirstRun(Context context) {
    	Log.i(TAG, "successfully completed first run");
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
    	SharedPreferences.Editor editor = settings.edit();
    	editor.putBoolean("firstRun", false);
    	editor.commit();
    	Log.i(TAG, "saved successful completion of first run");
    }
}

class UniqueDeviceId {
	private static final String TAG = "UniqueDeviceId";

	class Strategies {
		private Context context;

		// For more info, see: http://android-developers.blogspot.com/2011/03/identifying-app-installations.html

		public Strategies(Context context) {
			this.context = context;
		}

		String aAndroidId() {
			Log.i(TAG, "trying to get Android ID");
			Secure secureSettings = new Settings.Secure();
			String androidId = null;
			try {
				androidId = (String) secureSettings.getClass().getField("ANDROID_ID").get(secureSettings);
			} catch (Exception e) {
				Log.w(TAG, "couldn't get Android ID (exception thrown, stack trace follows)");
				e.printStackTrace();
				return null;
			}
			Log.i(TAG, "got Android ID " + androidId);
			return androidId;
		}

		String bSerialNumber() {
			Log.i(TAG, "trying to get serial number");
			Build build = new android.os.Build();
			String serialNumber = null;
			try {
				serialNumber = (String) build.getClass().getField("SERIAL").get(build);
			} catch (Exception e) {
				Log.w(TAG, "couldn't get serial number (exception thrown, stack trace follows)");
				e.printStackTrace();
				return null;
			}
			Log.i(TAG, "got serial number " + serialNumber);
			return serialNumber;
		}

		String cMacAddressHash() {
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

	       	Log.i(TAG, "got MAC address hash " + macAddressHash);
	    	return macAddressHash;
		}

		String dTelephonyDeviceId() {
			Log.i(TAG, "trying to get telephony ID");

			Log.d(TAG, "getting telephony manager");
			TelephonyManager telephonyManager = (TelephonyManager) this.context.getSystemService(Context.TELEPHONY_SERVICE);
			Log.d(TAG, "got telephony manager: " + telephonyManager);

			Log.d(TAG, "getting telephony device ID");
			String telephonyDeviceId = telephonyManager.getDeviceId();
			Log.d(TAG, "got telephony device ID: " + telephonyDeviceId);

			Log.i(TAG,  "got telephony ID " + telephonyDeviceId);
			return telephonyDeviceId;
		}
	}

	static String getUniqueDeviceId(Context context) {
		Strategies strategies = new UniqueDeviceId().new Strategies(context);
		String uniqueDeviceId = null;
		for (Method method : strategies.getClass().getDeclaredMethods()) {
			try {
				uniqueDeviceId = (String) method.invoke(strategies);
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
		String bidPriceInCents = params[1];
		String uuid = params[2];
		String securityHash = params[3];

		// Issue a POST request to register the app installation with CrowdMob.
		Log.i(TAG, "registering app installation with CrowdMob");
    	HttpClient client = new DefaultHttpClient();
    	HttpPost post = new HttpPost(CROWDMOB_URL);
    	List<NameValuePair> pairs = new ArrayList<NameValuePair>();
    	pairs.add(new BasicNameValuePair("public_key", publicKey));
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
    		Log.i(TAG, "registered app installation with CrowdMob, HTTP status code " + statusCode);
    	}
    	return statusCode;
	}	
}