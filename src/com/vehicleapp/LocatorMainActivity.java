package com.vehicleapp;

import com.parse.ParseUser;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;

public class LocatorMainActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_locator_main);
		ParseUser currentUser = ParseUser.getCurrentUser();
		if (currentUser != null) {
			  // do stuff with the user
			} else {
			  // show the signup or login screen
			}
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_locator_main, menu);
		return true;
	}

}
