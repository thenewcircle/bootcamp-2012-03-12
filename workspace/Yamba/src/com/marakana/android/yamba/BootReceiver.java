package com.marakana.android.yamba;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BootReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		// Register an alarm to trigger the UpdaterService on a repeating basis.
		
		// The intent we want to use to start the UpdaterService
		Intent startService = new Intent(context, UpdaterService.class);
		
		// Create the PendingIntent "authorization"
		PendingIntent pi = PendingIntent.getService(context, 1, startService,
								PendingIntent.FLAG_UPDATE_CURRENT);
		
		AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		am.setRepeating(AlarmManager.RTC, System.currentTimeMillis() + 10, 15000, pi);
	}

}
