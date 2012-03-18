package com.marakana.android.yamba;

import java.util.Date;
import java.util.List;

import winterwell.jtwitter.Twitter;
import winterwell.jtwitter.TwitterException;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
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
		
		// Create and post a notification to the user.
		
		// We'll cheat and use the chat notification icon.
		int icon = android.R.drawable.stat_notify_chat;
		String tickerText = getString(R.string.notify_new_status_ticker_text);
		long when = System.currentTimeMillis();
		Notification notification = new Notification(icon, tickerText, when);
		
		// Extended status title and description
		String contentTitle = getString(R.string.notify_new_status_content_title);
		String contentText = getResources().getQuantityString(R.plurals.notify_new_status_content_text, count, count);
		
		// Pending intent to display the MainActivity
		Intent showMessagesIntent = new Intent(this, MainActivity.class);
		PendingIntent showMessagesPendingIntent
			= PendingIntent.getActivity(this, YambaApplication.SHOW_NEW_STATUS_PENDING_INTENT,
					showMessagesIntent, PendingIntent.FLAG_UPDATE_CURRENT);
		
		notification.setLatestEventInfo(this, contentTitle, contentText, showMessagesPendingIntent);
		
		// Automatically cancel the notification when the user selects it.
		notification.flags |= Notification.FLAG_AUTO_CANCEL;
		if (count > 1) {
			// Add a badge if there are more than one new message.
			notification.number = count;
		}
		
		// Finally post the notification.
		NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		nm.notify(YambaApplication.NEW_STATUS_NOTIFICATION, notification);
	}
	
}
