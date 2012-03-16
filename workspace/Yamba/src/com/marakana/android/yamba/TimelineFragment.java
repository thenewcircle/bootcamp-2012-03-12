package com.marakana.android.yamba;

import android.content.Intent;
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

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
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
		
		// Reset the cursor loader to get a new cursor.
		getLoaderManager().restartLoader(0, null, this);
	}

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
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		// Release our reference to the cursor, without closing it.
		mAdapter.swapCursor(null);
	}
	
}
