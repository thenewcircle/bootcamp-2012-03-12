package com.marakana.android.yamba;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

public class MainActivity extends FragmentActivity {
	private static final String TAG = "MainActivity";
	
	private ComposeFragment mComposeFragment;
	private TimelineFragment mTimelineFragment;
	private FragmentManager mFragmentManager;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.v(TAG, "onCreate() invoked");
        setContentView(R.layout.main);
        
        mFragmentManager = getSupportFragmentManager();
        
        // Set up the activity's fragments
        if (savedInstanceState == null) {
        	// Initial activity creation. Create fragments.
        	mComposeFragment = new ComposeFragment();
        	mTimelineFragment = new TimelineFragment();
        	
        	// Add the fragments to the layout, and hide the ComposeFragment.
        	mFragmentManager.beginTransaction()
        	.add(R.id.fragment_container, mComposeFragment, "compose")
        	.add(R.id.fragment_container, mTimelineFragment, "timeline")
        	.hide(mComposeFragment)
        	.commit();
        }
        else {
        	// Activity bounced. Find the re-created fragments.
        	mComposeFragment = (ComposeFragment) mFragmentManager.findFragmentByTag("compose");
        	mTimelineFragment = (TimelineFragment) mFragmentManager.findFragmentByTag("timeline");
        }
    }

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.options_main, menu);
		inflater.inflate(R.menu.options_main_fragment_controls, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		Intent intent;
		switch (id) {
		case R.id.menu_preferences:
			// Display the PrefsActivity
			intent = new Intent(this, PrefsActivity.class);
			startActivity(intent);
			Log.v(TAG, "Settings menu item selected");
			return true;
		case R.id.menu_compose:
			// Show the ComposeFragment
			showComposeFragment();
			return true;
		case R.id.menu_timeline:
			// Show the TimelineFragment
			showTimelineFragment();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
	
	private void showComposeFragment() {
		mFragmentManager.beginTransaction()
			.hide(mTimelineFragment)
			.show(mComposeFragment)
			.commit();
	}
	
	private void showTimelineFragment() {
		mFragmentManager.beginTransaction()
			.hide(mComposeFragment)
			.show(mTimelineFragment)
			.commit();
	}
}

