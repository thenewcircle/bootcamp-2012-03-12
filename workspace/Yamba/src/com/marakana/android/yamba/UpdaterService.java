package com.marakana.android.yamba;

import java.util.Date;
import java.util.List;

import winterwell.jtwitter.Twitter;
import winterwell.jtwitter.TwitterException;
import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;
import android.database.SQLException;
import android.database.sqlite.SQLiteException;
import android.util.Log;

public class UpdaterService extends IntentService {
	private static final String TAG = "UpdaterService";

	public UpdaterService() {
		super("UpdaterService");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		Log.v(TAG, "onHandleIntent() invoked");
		// Fetch timeline data
		YambaApplication app = YambaApplication.getInstance();
		int count = 0;
		try {
			List<Twitter.Status> timeline;
			try {
				timeline = app.getTwitter().getHomeTimeline();
			} catch (NullPointerException e) {
				/*
				 * I *really* hate to do this, but jTwitter seems to have a bug
				 * that randomly throws NullPointerExceptions. When running the
				 * UpdaterService frequently, you typically encounter it within
				 * a couple of minutes. So, until we find and fix the bug in
				 * the library, ignoring this exception is about our only choice. :-(
				 */
				return;
			}
			ContentValues values = new ContentValues();
			
			for (Twitter.Status status: timeline) {
				long id = status.id;
				String name = status.user.name;
				String msg = status.text;
				Date createdAt = status.createdAt;
				
				Log.v(TAG, id + ": " + name + " posted at " + createdAt + ": " + msg);
				
				// Try to insert the status into the Timeline database
				values.clear();
				values.put(StatusProvider.KEY_ID, id);
				values.put(StatusProvider.KEY_USER, name);
				values.put(StatusProvider.KEY_MESSAGE, msg);
				values.put(StatusProvider.KEY_CREATED_AT, createdAt.getTime());
				
				// Insert the status in the StatusProvider
				try {
					getContentResolver().insert(StatusProvider.CONTENT_URI, values);
					count++;
				} catch (SQLException e) {
					// Ignore, assuming that it's a duplicate row.
				}
			}
		} catch (TwitterException e) {
			Log.w(TAG, "Unable to fetch timeline data");
		} catch (SQLiteException e) {
			Log.e(TAG, "Unable to open timeline database");
		}
		if (count > 0) {
			newStatusNotification(count);
		}
	}
	
	private void newStatusNotification(int count) {
		/*
		 * Send broadcast intent announcing new status messages available.
		 * Restrict it to "trusted" receivers whose application holds our custom
		 * permission. Currently, this is just the Yamba application itself.
		 */
		
		Intent broadcast = new Intent(YambaApplication.ACTION_NEW_STATUS);
		broadcast.putExtra(YambaApplication.EXTRA_NEW_STATUS_COUNT, count);
		sendBroadcast(broadcast, YambaApplication.RECEIVE_NEW_STATUS);
	}
	
}
