/*---------------------------------------------------------------------------*\
 |  InstallsAndroidActivity.java                                             |
 |                                                                           |
 |  Copyright (c) 2012, CrowdMob, Inc., original authors.                    |
 |                                                                           |
 |      File created/modified by:                                            |
 |          Raj Shah <raj@crowdmob.com>                                      |
\*---------------------------------------------------------------------------*/

package com.crowdmob.installs;

import android.app.Activity;
import android.os.Bundle;

public class InstallsAndroidActivity extends Activity {
	private static final String APP_ID = "0";
	private static final String BID_PRICE_IN_CENTS = "0";

	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        RegisterWithCrowdMob.trackAppInstallation(this, APP_ID, BID_PRICE_IN_CENTS);
    }
}