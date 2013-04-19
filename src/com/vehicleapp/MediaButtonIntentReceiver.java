package com.vehicleapp;


import com.parse.ParsePush;
import com.parse.ParseUser;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.view.KeyEvent;

public class MediaButtonIntentReceiver extends BroadcastReceiver {
	
	@Override
	public void onReceive(Context context, Intent intent) {
		System.out.println("media paaaaaaaaaaas in action");

		String intentAction = intent.getAction();
		System.out.println("media paaaaaaaaaaas in main");
		if (Intent.ACTION_MEDIA_BUTTON.equals(intentAction)) {
			KeyEvent event = (KeyEvent) intent
					.getParcelableExtra(Intent.EXTRA_KEY_EVENT);

			if (event == null) {
				return;
			}

			int keycode = event.getKeyCode();
			int action = event.getAction();

			if (keycode == KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE
					|| keycode == KeyEvent.KEYCODE_HEADSETHOOK || keycode == KeyEvent.ACTION_DOWN || keycode == KeyEvent.ACTION_UP) {
			
				if (action == KeyEvent.ACTION_DOWN) {
					// Start your app here!
					
					ParsePush push = new ParsePush();
					
					
						//System.out.println(device);
						push.setChannel(ParseUser.getCurrentUser().getObjectId());
						push.setMessage("Alaaaaaaaaaaarm");
						push.sendInBackground();
						
					}
					

					if (isOrderedBroadcast()) {
						System.out.println("palaaaaas");
						abortBroadcast();
					}
				
			}
		}
	}
}
