package com.vehicleapp;


import com.parse.ParseObject;
import com.parse.ParseUser;
import com.parse.PushService;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class Vehicle_Data extends Activity {

	private static final int Request_code = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_vehicle__data);
		
		Button btn_add_vehicle = (Button) findViewById(R.id.btn_add_vehicle);
		PushService.subscribe(this, "Alarms", LocatorMainActivity.class);
		
		
	

		OnClickListener lsn_add_vehicle = new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				EditText vehicle_name = (EditText)findViewById(R.id.txt_vehicle_name);
				final String Vehicle = vehicle_name.getText().toString();
				
				if (Vehicle != null)
				{
					ParseObject vehicle_object = new ParseObject("Vehicle");
					vehicle_object.put("vehicle_name", Vehicle);
					vehicle_object.put("user_id", ParseUser.getCurrentUser().getObjectId());
					vehicle_object.saveInBackground();
					
					
					System.out.println(Vehicle);
			
					

					Intent intent = new Intent();
					//Bundle bun = new Bundle();
					//bun.putString("vehicle_name", Vehicle);
					//intent.putExtras(bun);
					intent.setClass(Vehicle_Data.this,
							LocatorMainActivity.class);
					startActivityForResult(intent,
							Request_code);
					finish();
					
					
				}
				
			}
		};
		
		btn_add_vehicle.setOnClickListener(lsn_add_vehicle);
		
		

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.vehicle__data, menu);
		return true;
	}

}
