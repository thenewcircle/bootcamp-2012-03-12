package com.marakana.android.yamba;

import winterwell.jtwitter.TwitterException;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class ComposeFragment extends Fragment implements OnClickListener {
	private static final String TAG = "ComposeFragment";

	private EditText mEditMsg;
	private Toast mToast;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// Retain this instance of the fragment across Activity restarts
		setRetainInstance(true);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		View top = inflater.inflate(R.layout.compose_fragment, container, false);
		
        // Get references to our View objects
        mEditMsg = (EditText) top.findViewById(R.id.edit_msg);
        Button buttonUpdate = (Button) top.findViewById(R.id.button_update);
        
        // Use the Fragment as the button click listener
        buttonUpdate.setOnClickListener(this);
        
        // Initialize the Toast notification
        mToast = Toast.makeText(getActivity().getApplicationContext(), null, Toast.LENGTH_LONG);

		return top;
	}

	@Override
	public void onClick(View v) {
		int id = v.getId();
		switch (id) {
		case R.id.button_update:
			// User clicked the update button
			Log.v(TAG, "Update button clicked");
			
			// Read the content of the EditText
			String msg = mEditMsg.getText().toString();
			Log.v(TAG, "User entered: " + msg);
			
			// Clear the content of the EditText
			mEditMsg.setText("");
			
			// Post the message only if the user provided one
			if (!TextUtils.isEmpty(msg)) {
				new PostToTwitter().execute(msg);
			}
			break;
		default:
			// Unknown view was clicked? We should never get here!
		}
	}
	
	private class PostToTwitter extends AsyncTask<String, Void, Integer> {

		@Override
		protected Integer doInBackground(String... params) {
			int result = R.string.post_status_success;
			try {
				// Post the message to Yamba (Twitter)
				YambaApplication app = YambaApplication.getInstance();
				app.getTwitter().setStatus(params[0]);
			} catch (TwitterException e) {
				Log.w(TAG, "Failed to post message", e);
				result = R.string.post_status_fail;
			}
			return result;
		}

		@Override
		protected void onPostExecute(Integer result) {
			mToast.setText(result);
			mToast.show();
		}
		
	}
}
