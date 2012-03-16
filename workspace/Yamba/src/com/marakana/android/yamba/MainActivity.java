package com.marakana.android.yamba;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

public class MainActivity extends FragmentActivity {
	private static final String TAG = "MainActivity";
	
	private static final int UNKNOWN_FRAGMENT_VISIBLE = -1;
	private static final int COMPOSE_FRAGMENT_VISIBLE = 1;
	private static final int TIMELINE_FRAGMENT_VISIBLE = 2;
	private int mFragmentVisible;
	private static final String FRAGMENT_VISIBLE = "FRAGMENT_VISIBLE";
	
	private ComposeFragment mComposeFragment;
	private TimelineFragment mTimelineFragment;
	private FragmentManager mFragmentManager;
	
	private MenuItem mComposeMenuItem;
	private MenuItem mTimelineMenuItem;
	
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
        	mFragmentVisible = TIMELINE_FRAGMENT_VISIBLE;
        }
        else {
        	// Activity bounced. Find the re-created fragments.
        	mComposeFragment = (ComposeFragment) mFragmentManager.findFragmentByTag("compose");
        	mTimelineFragment = (TimelineFragment) mFragmentManager.findFragmentByTag("timeline");
        	
        	// Restore fragment visibility flag.
        	mFragmentVisible = savedInstanceState.getInt(FRAGMENT_VISIBLE, UNKNOWN_FRAGMENT_VISIBLE);
        	if (mFragmentVisible == UNKNOWN_FRAGMENT_VISIBLE) {
        		// If for any reason we can't determine which fragment was visible,
        		// default to the TimelineFragment.
        		showTimelineFragment();
        	}
        }
    }

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		
		// Save fragment visibility state so that the action bar/options menu state
		// can be restored.
		outState.putInt(FRAGMENT_VISIBLE, mFragmentVisible);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.options_main, menu);
		inflater.inflate(R.menu.options_main_fragment_controls, menu);
		
		// Find references to the menu items to show the ComposeFragment and TimelineFragment
		// so that we can toggle their visibility.
		mComposeMenuItem = menu.findItem(R.id.menu_compose);
		mTimelineMenuItem = menu.findItem(R.id.menu_timeline);
		
		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		super.onPrepareOptionsMenu(menu);
		// Set visibility of the item depending on which fragment is visible.
		switch (mFragmentVisible) {
		case COMPOSE_FRAGMENT_VISIBLE:
			mComposeMenuItem.setVisible(false);
			mTimelineMenuItem.setVisible(true);
			break;
		default:
			// Assume TimelineFragment visible.
			mTimelineMenuItem.setVisible(false);
			mComposeMenuItem.setVisible(true);
		}
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
    	mFragmentVisible = COMPOSE_FRAGMENT_VISIBLE;
    	if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
    		// Force refresh of action bar
    		invalidateOptionsMenu();
    	}
	}
	
	private void showTimelineFragment() {
		mFragmentManager.beginTransaction()
			.hide(mComposeFragment)
			.show(mTimelineFragment)
			.commit();
    	mFragmentVisible = TIMELINE_FRAGMENT_VISIBLE;
    	if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
    		// Force refresh of action bar
    		invalidateOptionsMenu();
    	}
	}
}

