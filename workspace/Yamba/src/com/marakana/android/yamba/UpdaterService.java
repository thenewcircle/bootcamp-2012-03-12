package com.marakana.android.yamba;

import java.util.Date;
import java.util.List;

import winterwell.jtwitter.Twitter;
import winterwell.jtwitter.TwitterException;
import android.app.IntentService;
import android.content.Intent;
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
			List<Twitter.Status> timeline = app.getTwitter().getHomeTimeline();
			for (Twitter.Status status: timeline) {
				long id = status.id;
				String name = status.user.name;
				String msg = status.text;
				Date createdAt = status.createdAt;
				
				Log.v(TAG, id + ": " + name + " posted at " + createdAt + ": " + msg);
			}
		} catch (TwitterException e) {
			Log.w(TAG, "Unable to fetch timeline data");
		}
	}
	
}
