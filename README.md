CrowdMob Android Integration and App Installs Tracking
======================================================

CrowdMob (http://deals.crowdmob.com/) drives installs of your Android mobile
app.  You can integrate your mobile app with CrowdMob's installs tracking
service by following these integration instructions.



Integration Instructions
------------------------

1. Download the file: [`RegisterWithCrowdMob.java`](https://raw.github.com/crowdmob/integrate-android/master/src/com/crowdmob/integrate/RegisterWithCrowdMob.java)
2. Copy `RegisterWithCrowdMob.java` into the same directory containing your main Android app activity.  (Usually: `YourApp/src/com/yourcompany/yourapp`)
3. Edit `RegisterWithCrowdMob.java`, and change the package from `com.crowdmob.installs` to the package containing your main app activity.  (Usually: `com.yourcompany.yourapp`)
4. Edit your main activity class:
  1. Add a class-level constant for your app's secret key.  (Like: `private static final String SECRET_KEY = "d2ef7da8a45891f2fee33747788903e7";`)
  2. Add a class-level constant for your app's permalink.  (Like: `private static final String PERMALINK = "your-app";`)
  3. At the very bottom of your `onCreate()` method, call the code to register your app's install with CrowdMob: `RegisterWithCrowdMob.trackAppInstallation(this, SECRET_KEY, PERMALINK);`
  4. Confirm your work against this [example main activity class](https://github.com/crowdmob/integrate-android/blob/master/src/com/crowdmob/integrate/IntegrateAndroidActivity.java).
5. Edit your `AndroidManifest.xml` file:
  1. At the very bottom, within the `<manifest>` tag, add the following lines:
    1. `<uses-permission android:name="android.permission.ACCESS_WIFI_STATE" />`
    2. `<uses-permission android:name="android.permission.INTERNET" />`
    3. `<uses-permission android:name="android.permission.READ_PHONE_STATE" />`
    4. `<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />`
  2. Confirm your work against this [example `AndroidManifest.xml` file](https://github.com/crowdmob/integrate-android/blob/master/AndroidManifest.xml).

Congratulations!  You've integrated your Android app with CrowdMob to track installs.



Testing Your Integration
------------------------

Now that you've integrated with CrowdMob to track installs, confirm that everything is working:

1. Launch your app in your Android emulator, and examine the logs using LogCat.
2. Look for the following log entry: `registered app installation with CrowdMob, HTTP status code 200`
