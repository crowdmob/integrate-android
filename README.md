CrowdMob Android Integration and App Installs Tracking
======================================================

CrowdMob (http://deals.crowdmob.com/) drives installs of your Android mobile
app.  You can integrate your mobile app with CrowdMob's installs tracking
service by following these integration instructions.



Integration Instructions
------------------------

1. Download the file [`RegisterWithCrowdMob.java`](https://raw.github.com/crowdmob/installs-android/master/src/com/crowdmob/installs/RegisterWithCrowdMob.java).
2. Copy `RegisterWithCrowdMob.java` into the same directory containing your main Android app activity.  (Usually: `YourApp/src/com/yourcompany/yourapp`)
3. Edit `RegisterWithCrowdMob.java`, and change the package from `com.crowdmob.installs` to the package containing your main app activity.  (Usually: `com.yourcompany.yourapp`)
4. Edit your main activity class:
  1. Add a class-level constant for your app's secret key.  (Like: `private static final String SECRET_KEY = "d2ef7da8a45891f2fee33747788903e7";`)
  2. Add a class-level constant for your app's permalink.  (Like: `private static final String PERMALINK = "your-app";`)
  3. At the very bottom of your `onCreate()` method, call the code to register your app's install with CrowdMob: `RegisterWithCrowdMob.trackAppInstallation(this, SECRET_KEY, PERMALINK);`
