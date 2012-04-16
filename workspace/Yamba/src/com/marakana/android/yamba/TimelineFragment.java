package com.marakana.android.yamba;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.text.format.DateUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

public class TimelineFragment extends ListFragment
				implements SimpleCursorAdapter.ViewBinder,
				LoaderManager.LoaderCallbacks<Cursor> {

	private static final String[] FROM = {
		StatusProvider.KEY_USER,
		StatusProvider.KEY_CREATED_AT,
		StatusProvider.KEY_MESSAGE
	};
	
	private static final int[] TO = {
		R.id.data_user,
		R.id.data_date,
		R.id.data_msg
	};
	
	private SimpleCursorAdapter mAdapter;
	
	private TimelineReceiver mTimelineReceiver;
	private IntentFilter mIntentFilter;
	
	private NotificationManager mNotificationManager;
	private SharedPreferences mTimelineStatePrefs;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
		
		// Initialize the broadcast receiver and intent filter
		// for receiving status update notifications.
		mTimelineReceiver = new TimelineReceiver();
		mIntentFilter = new IntentFilter(YambaApplication.ACTION_NEW_STATUS);
		
		mNotificationManager = (NotificationManager) getActivity().getSystemService(Context.NOTIFICATION_SERVICE);

		mTimelineStatePrefs = getActivity().getSharedPreferences(YambaApplication.TIMELINE_STATE_PREFERENCES, Context.MODE_PRIVATE);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		// Create an adapter mapping the cursor data to the layout
		mAdapter = new SimpleCursorAdapter(getActivity(),
				R.layout.timeline_row, null, FROM, TO, 0);
		mAdapter.setViewBinder(this);
		
		setListAdapter(mAdapter);
		
		// Initialize the cursor with the data
		getLoaderManager().initLoader(0, null, this);
	}

	@Override
	public void onResume() {
		super.onResume();
		
		/*
		 * Request the activity register our broadcast receiver.
		 * 
		 * Restrict it to "trusted" senders whose application holds our custom
		 * permission. Currently, this is just the Yamba application itself.
		 */
		getActivity().registerReceiver(mTimelineReceiver, mIntentFilter,
				YambaApplication.RECEIVE_NEW_STATUS, null);

		// If visible, cancel any pending notification.
		cancelNewStatusNotification();
		
		// And refresh with the latest data.
		updateDisplay();
	}
	
	@Override
	public void onHiddenChanged(boolean hidden) {
		// Cancel any pending notification if we become visible while in the "resumed" state.
		cancelNewStatusNotification();
	}

	@Override
	public void onPause() {
		super.onPause();
		
		// Request the activity unregister our broadcast receiver.
		getActivity().unregisterReceiver(mTimelineReceiver);
	}

	// Options menu/action bar handling methods.
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.options_timeline_fragment, menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		switch (id) {
		case R.id.menu_refresh:
			// Start UpdaterService to refresh the timeline
			Intent intent = new Intent(getActivity(), UpdaterService.class);
			getActivity().startService(intent);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	// ViewBinder method implementation
	
	@Override
	public boolean setViewValue(View v, Cursor cursor, int columnIndex) {
		int id = v.getId();
		switch (id) {
		case R.id.data_date:
			// Present the date in a more human-friendly fashion
			TextView tv = (TextView) v;
			long timestamp = cursor.getLong(columnIndex);
			CharSequence relTime = DateUtils.getRelativeTimeSpanString(timestamp);
			tv.setText(relTime);
			return true;
		default:
			return false;
		}
	}
	
	// LoaderCallbacks method implementations

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		// We need to return a new loader.
		return new CursorLoader(getActivity().getApplicationContext(),
				StatusProvider.CONTENT_URI, null, null, null, null);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		// We've got a new cursor. Install it in the adapter.
		mAdapter.swapCursor(cursor);
		
		// Persistently save the timestamp of the most recent status.
		long timestamp = 0;
		if (cursor.moveToFirst()) {
			timestamp = cursor.getLong(cursor.getColumnIndex(StatusProvider.KEY_CREATED_AT));
		}
		mTimelineStatePrefs.edit()
			.putLong(YambaApplication.PREF_LAST_VIEWED_TIMESTAMP, timestamp)
			.apply();
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		// Release our reference to the cursor, without closing it.
		mAdapter.swapCursor(null);
	}
	
	/**
	 * Cancel any pending notification if we are currently visible and in the "resumed" state.
	 */
	private void cancelNewStatusNotification() {
		if (isResumed() && !isHidden()) {
			mNotificationManager.cancel(YambaApplication.NEW_STATUS_NOTIFICATION);
		}
	}
	
	// Method and broadcast receiver class for handling new status notifications.
	
	public void updateDisplay() {
		// Reset the cursor loader to get a new cursor.
		getLoaderManager().restartLoader(0, null, this);
	}
	
	private class TimelineReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			// When we receive a new status notification,
			// get an updated Cursor for the adapter.
			updateDisplay();
			cancelNewStatusNotification();
		}
		
	}
	
}
