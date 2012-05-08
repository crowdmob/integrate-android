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
	private static final String SECRET_KEY = "d2ef7da8a45891f2fee33747788903e7";
	private static final String PERMALINK = "the-impossible-game";

	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        RegisterWithCrowdMob.trackAppInstallation(this, SECRET_KEY, PERMALINK);
    }
}