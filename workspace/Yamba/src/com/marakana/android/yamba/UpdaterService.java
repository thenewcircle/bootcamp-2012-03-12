package com.marakana.android.yamba;

import java.util.Date;
import java.util.List;

import winterwell.jtwitter.Twitter;
import winterwell.jtwitter.TwitterException;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
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
		
		YambaApplication app = YambaApplication.getInstance();
		ContentResolver resolver = getContentResolver();
		
		// Fetch timeline data
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
					resolver.insert(StatusProvider.CONTENT_URI, values);
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
			// Get the timestamp of the last status viewed.
			SharedPreferences prefs = getSharedPreferences(YambaApplication.TIMELINE_STATE_PREFERENCES, Context.MODE_PRIVATE);
			long lastViewedTimestamp = prefs.getLong(YambaApplication.PREF_LAST_VIEWED_TIMESTAMP, 0);
			
			// Find out how many statuses have been received since then.
			Cursor cursor = resolver.query(StatusProvider.CONTENT_URI,
							new String[]{ StatusProvider.KEY_CREATED_AT },
							StatusProvider.KEY_CREATED_AT + " > " + lastViewedTimestamp,
							null, null);
			int unreadCount = cursor.getCount();
			Log.v(TAG, "After inserts, unread count = " + unreadCount);
			
			// If any, post a status and send a broadcast message.
			if (unreadCount > 0) {
				newStatusNotification(unreadCount);
				newStatusBroadcast(unreadCount);
			}
		}
	}
	
	/**
	 * Create and post a notification to the user.
	 * 
	 * @param unreadCount	Number of unread status messages.
	 */
	private void newStatusNotification(int unreadCount) {
		// We'll cheat and use the chat notification icon.
		int icon = android.R.drawable.stat_notify_chat;
		String tickerText = getString(R.string.notify_new_status_ticker_text);
		long when = System.currentTimeMillis();
		Notification notification = new Notification(icon, tickerText, when);
		
		// Extended status title and description
		String contentTitle = getString(R.string.notify_new_status_content_title);
		String contentText = getResources().getQuantityString(R.plurals.notify_new_status_content_text, unreadCount, unreadCount);
		
		// Create a pending intent to display the MainActivity
		Intent showMessagesIntent = new Intent(this, MainActivity.class);
		
		// If there are activities on top of MainActivity (such as PrefsActivity), clear them
		// if the user navigates to MainActivity via the notification.
		showMessagesIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		
		PendingIntent showMessagesPendingIntent
			= PendingIntent.getActivity(this, YambaApplication.SHOW_NEW_STATUS_PENDING_INTENT,
					showMessagesIntent, PendingIntent.FLAG_UPDATE_CURRENT);
		
		notification.setLatestEventInfo(this, contentTitle, contentText, showMessagesPendingIntent);
		
		// Automatically cancel the notification when the user selects it.
		notification.flags |= Notification.FLAG_AUTO_CANCEL;
		
		if (unreadCount > 1) {
			// Add a badge if there are more than one unread message.
			notification.number = unreadCount;
		}
		
		// Finally post the notification.
		NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		nm.notify(YambaApplication.NEW_STATUS_NOTIFICATION, notification);
	}
	
	/**
	 * Send a broadcast intent announcing new status messages available.
	 * Restrict it to "trusted" receivers whose application holds our custom
	 * permission. Currently, this is just the Yamba application itself.
	 * 
	 * @param unreadCount	Number of unread status messages.
	 */
	private void newStatusBroadcast(int unreadCount) {
		Intent broadcast = new Intent(YambaApplication.ACTION_NEW_STATUS);
		broadcast.putExtra(YambaApplication.EXTRA_NEW_STATUS_COUNT, unreadCount);
		sendBroadcast(broadcast, YambaApplication.RECEIVE_NEW_STATUS);
	}
	
}
