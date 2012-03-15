package com.marakana.android.yamba;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.widget.SimpleCursorAdapter;

public class TimelineFragment extends ListFragment {

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
	
	
}
