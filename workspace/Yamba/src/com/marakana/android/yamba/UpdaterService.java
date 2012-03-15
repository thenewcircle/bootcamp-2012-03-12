package com.marakana.android.yamba;

import java.util.Date;
import java.util.List;

import winterwell.jtwitter.Twitter;
import winterwell.jtwitter.TwitterException;
import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
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
		try {
			SQLiteDatabase db = app.getDb();
			List<Twitter.Status> timeline = app.getTwitter().getHomeTimeline();
			ContentValues values = new ContentValues();
			
			for (Twitter.Status status: timeline) {
				long id = status.id;
				String name = status.user.name;
				String msg = status.text;
				Date createdAt = status.createdAt;
				
				Log.v(TAG, id + ": " + name + " posted at " + createdAt + ": " + msg);
				
				// Try to insert the status into the Timeline database
				values.clear();
				values.put(TimelineHelper.KEY_ID, id);
				values.put(TimelineHelper.KEY_USER, name);
				values.put(TimelineHelper.KEY_MESSAGE, msg);
				values.put(TimelineHelper.KEY_CREATED_AT, createdAt.getTime());
				db.insert(TimelineHelper.T_TIMELINE, null, values);
			}
		} catch (TwitterException e) {
			Log.w(TAG, "Unable to fetch timeline data");
		} catch (SQLiteException e) {
			Log.e(TAG, "Unable to open timeline database");
		}
	}
	
}
