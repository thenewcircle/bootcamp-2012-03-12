package com.marakana.android.yamba;

import winterwell.jtwitter.Twitter;
import winterwell.jtwitter.TwitterException;
import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends Activity implements OnClickListener {
	private static final String TAG = "MainActivity";
	
	private EditText mEditMsg;
	private Toast mToast;
	
	private Twitter twitter;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.v(TAG, "onCreate() invoked");
        setContentView(R.layout.main);
        
        // Get references to our View objects
        mEditMsg = (EditText) findViewById(R.id.edit_msg);
        Button buttonUpdate = (Button) findViewById(R.id.button_update);
        
        // Use the Activity as the button click listener
        buttonUpdate.setOnClickListener(this);
        
        // Initialize the Toast notification
        mToast = Toast.makeText(this, null, Toast.LENGTH_LONG);

        // Initialize the Twitter object
        twitter = new Twitter("student", "password");
        twitter.setAPIRootUrl("http://yamba.marakana.com/api");
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
				twitter.setStatus(params[0]);
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

