package com.marakana.android.yamba;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.widget.SimpleCursorAdapter;
import android.text.format.DateUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

public class TimelineFragment extends ListFragment
				implements SimpleCursorAdapter.ViewBinder {

	private static final String[] FROM = {
		TimelineHelper.KEY_USER,
		TimelineHelper.KEY_CREATED_AT,
		TimelineHelper.KEY_MESSAGE
	};
	
	private static final int[] TO = {
		R.id.data_user,
		R.id.data_date,
		R.id.data_msg
	};
	
	private Cursor mCursor;
	private SimpleCursorAdapter mAdapter;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		YambaApplication app = YambaApplication.getInstance();
		
		// Query the Timeline database. Note that this blocks!
		mCursor = app.getDb().query(TimelineHelper.T_TIMELINE,
				null, null, null, null, null,
				TimelineHelper.KEY_CREATED_AT + " desc");
		
		// Create an adapter mapping the cursor data to the layout
		mAdapter = new SimpleCursorAdapter(getActivity(),
				R.layout.timeline_row, mCursor, FROM, TO, 0);
		mAdapter.setViewBinder(this);
		
		setListAdapter(mAdapter);
	}

	@Override
	public void onResume() {
		super.onResume();
		mCursor.requery();
		mAdapter.notifyDataSetChanged();
	}

	@Override
	public void onPause() {
		super.onPause();
		mCursor.deactivate();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		mCursor.close();
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
	
	
}
