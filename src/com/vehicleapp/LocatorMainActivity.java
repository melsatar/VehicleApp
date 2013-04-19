package com.vehicleapp;

import android.app.Activity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.AudioManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.widget.TextView;
import android.widget.Toast;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.PushService;

public class LocatorMainActivity extends Activity {
	private TextView mLatLng;
	private LocationManager mLocationManager;
	private TextView mAddress;
	private Handler mHandler;
	private boolean mGeocoderAvailable;
	private static final int UPDATE_ADDRESS = 1;
	private static final int UPDATE_LATLNG = 2;
	private static final int TEN_SECONDS = 10000;
	private static final int TEN_METERS = 10;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_locator_main);
		// Save the current Installation to Parse.
		ParseInstallation installation = ParseInstallation.getCurrentInstallation();
		installation.put("owner", ParseUser.getCurrentUser().getObjectId());
		installation.saveEventually();

		mAddress = (TextView) findViewById(R.id.tv_address);
		mLatLng = (TextView) findViewById(R.id.tv_latlng);
		mGeocoderAvailable = Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD && Geocoder.isPresent();
		mHandler = new Handler() {
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case UPDATE_ADDRESS:
					mAddress.setText((String) msg.obj);
					break;
				case UPDATE_LATLNG:
					mLatLng.setText((String) msg.obj);
					break;
				}
			}
		};

		// Get a reference to the LocationManager object.
		mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		//
		Location gpsLocation = null;
		Location networkLocation = null;

		mLocationManager.removeUpdates(listener);
		mLatLng.setText("");
		mAddress.setText("");

		gpsLocation = requestUpdatesFromProvider(LocationManager.GPS_PROVIDER,R.string.not_support_gps);
		// Update the UI immediately if a location is obtained.
		if (gpsLocation != null) {
			updateUILocation(gpsLocation);
			System.out.println("Location ok");
			int lat = (int) (gpsLocation.getLatitude() * 1E6);
			int lon = (int) (gpsLocation.getLongitude() * 1E6);

		}

		MediaButtonIntentReceiver mMediaButtonReceiver = new MediaButtonIntentReceiver();
		IntentFilter mediaFilter = new IntentFilter(Intent.ACTION_MEDIA_BUTTON);
		mediaFilter.setPriority(IntentFilter.SYSTEM_HIGH_PRIORITY);
		System.out.println("in activity");
		registerReceiver(mMediaButtonReceiver, mediaFilter);

		((AudioManager) getSystemService(AUDIO_SERVICE))
				.registerMediaButtonEventReceiver(new ComponentName(this,
						MediaButtonIntentReceiver.class));

	}

	private final LocationListener listener = new LocationListener() {

		@Override
		public void onLocationChanged(Location location) {
			// A new location update is received. Do something useful with it.
			// Update the UI with
			// the location update.
			updateUILocation(location);
		}

		@Override
		public void onProviderDisabled(String provider) {
		}

		@Override
		public void onProviderEnabled(String provider) {
		}

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {
		}
	};

	private Location requestUpdatesFromProvider(final String provider,
			final int errorResId) {
		Location location = null;
		if (mLocationManager.isProviderEnabled(provider)) {
			mLocationManager.requestLocationUpdates(provider, TEN_SECONDS,
					TEN_METERS, listener);
			location = mLocationManager.getLastKnownLocation(provider);
		} else {
			Toast.makeText(this, errorResId, Toast.LENGTH_LONG).show();
		}
		return location;
	}

	private void updateUILocation(final Location location) {
		// We're sending the update to a handler which then updates the UI with
		// the new
		// location.
		Message.obtain(mHandler, UPDATE_LATLNG,
				location.getLatitude() + ", " + location.getLongitude())
				.sendToTarget();

		ParseUser user = ParseUser.getCurrentUser();
		final String user_id = user.getObjectId();
		ParseQuery query = new ParseQuery("Vehicle");
		System.out.println("my user is " + user_id);
		query.whereEqualTo("user_id", user_id);
		query.getFirstInBackground(new GetCallback() {

			@Override
			public void done(ParseObject object, ParseException e) {
				// TODO Auto-generated method stub
				System.out.println(object.getObjectId());
				System.out.println(user_id);
				ParseObject vehicle_location = new ParseObject("Vehicle_GPS");
				vehicle_location.put("longitude",
						String.valueOf(location.getLongitude()));
				vehicle_location.put("latitude",
						String.valueOf(location.getLatitude()));
				String vehichle_id = object.getObjectId();
				vehicle_location.put("vehicle_id", vehichle_id);
				ParseGeoPoint point = new ParseGeoPoint(location.getLatitude(),
						location.getLongitude());
				vehicle_location.put("position", point);
				vehicle_location.saveInBackground();

			}
		});

		// Bypass reverse-geocoding only if the Geocoder service is available on
		// the device.
		if (mGeocoderAvailable)
			doReverseGeocoding(location);
	}

	private void doReverseGeocoding(Location location) {
		// Since the geocoding API is synchronous and may take a while. You
		// don't want to lock
		// up the UI thread. Invoking reverse geocoding in an AsyncTask.
		(new ReverseGeocodingTask(this)).execute(new Location[] { location });
	}

	private class ReverseGeocodingTask extends AsyncTask<Location, Void, Void> {
		Context mContext;

		public ReverseGeocodingTask(Context context) {
			super();
			mContext = context;
		}

		@Override
		protected Void doInBackground(Location... params) {
			Geocoder geocoder = new Geocoder(mContext, Locale.getDefault());

			Location loc = params[0];
			List<Address> addresses = null;
			try {
				addresses = geocoder.getFromLocation(loc.getLatitude(),
						loc.getLongitude(), 1);
			} catch (IOException e) {
				e.printStackTrace();
				// Update address field with the exception.
				Message.obtain(mHandler, UPDATE_ADDRESS, e.toString())
						.sendToTarget();
			}
			if (addresses != null && addresses.size() > 0) {
				Address address = addresses.get(0);
				// Format the first line of address (if available), city, and
				// country name.
				String addressText = String.format(
						"%s, %s, %s",
						address.getMaxAddressLineIndex() > 0 ? address
								.getAddressLine(0) : "", address.getLocality(),
						address.getCountryName());
				// Update address field on UI.
				Message.obtain(mHandler, UPDATE_ADDRESS, addressText)
						.sendToTarget();
			}
			return null;
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_locator_main, menu);
		return true;
	}

}
