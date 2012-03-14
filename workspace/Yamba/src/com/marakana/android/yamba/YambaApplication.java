package com.marakana.android.yamba;

import winterwell.jtwitter.Twitter;
import android.app.Application;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.preference.PreferenceManager;
import android.util.Log;

public class YambaApplication extends Application implements OnSharedPreferenceChangeListener {
	private static final String TAG = "YambaApplication";

	private static YambaApplication instance;
	
	private SharedPreferences prefs;
	private Twitter twitter;

	public static YambaApplication getInstance() {
		return instance;
	}

	public synchronized Twitter getTwitter() {
		if (twitter == null) {
			// Retrieve latest preference values for the connection
			String user = prefs.getString("PREF_USER", null);
			String password = prefs.getString("PREF_PASSWORD", null);
			String url = prefs.getString("PREF_SITE_URL", "http://yamba.marakana.com/api");

			// Initialize the Twitter object
			twitter = new Twitter(user, password);
			twitter.setAPIRootUrl(url);
		}
		return twitter;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		
		instance = this;
		
		prefs = PreferenceManager.getDefaultSharedPreferences(this);
		prefs.registerOnSharedPreferenceChangeListener(this);
	}

	@Override
	public synchronized void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		Log.v(TAG, "Preference changed: " + key);
		twitter = null;
	}
}
