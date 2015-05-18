package com.matchrace.matchrace;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import com.matchrace.matchrace.classes.C;
import com.matchrace.matchrace.classes.SaveKmlTask;

/**
 * Menu activity. Shows the menu screen.
 *
 */
public class MenuActivity extends Activity implements OnClickListener {

	// Application variables.
	private String user = "", pass = "", event = "", fullUserName = "";

	// Views.
	private Button bMap, bKml1, bKml2, bLogout, bExit;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_menu);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

		// Disables lock-screen and keeps screen on.
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		initialize();
	}

	/**
	 * Initialize components.
	 */
	private void initialize() {
		// The user name, password and event number connected to the application.
		user = getIntent().getStringExtra(C.USER_NAME);
		pass = getIntent().getStringExtra(C.USER_PASS);
		event = getIntent().getStringExtra(C.EVENT_NUM);
		fullUserName = user + "_" + pass + "_" + event;

		// Initializing Buttons.
		bMap = (Button) findViewById(R.id.bMap);
		bKml1 = (Button) findViewById(R.id.bKml1);
		bKml2 = (Button) findViewById(R.id.bKml2);
		bLogout = (Button) findViewById(R.id.bLogout);
		bExit = (Button) findViewById(R.id.bExit);

		bMap.setOnClickListener(this);
		bKml1.setOnClickListener(this);
		bKml2.setOnClickListener(this);
		bLogout.setOnClickListener(this);
		bExit.setOnClickListener(this);
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		finish();
	}

	@Override
	public void onClick(View v) {
		Intent intent;
		switch (v.getId()) {
		case R.id.bMap:
			intent = new Intent(MenuActivity.this, MainActivity.class);
			intent.putExtra(C.USER_NAME, user);
			intent.putExtra(C.USER_PASS, pass);
			intent.putExtra(C.EVENT_NUM, event);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);
			break;
		case R.id.bKml1:
			bKml1.setEnabled(false);
			Toast.makeText(this, "Please wait few seconds...", Toast.LENGTH_SHORT).show();

			// AsyncTask for saving the KML file on SD memory (with time stamp).
			SaveKmlTask saveKml1 = new SaveKmlTask(this, "SaveKmlTask", fullUserName, 1);
			saveKml1.execute(C.URL_HISTORY_TABLE+"&event="+event, C.URL_CLIENTS_TABLE+"&event="+event);
			break;
		case R.id.bKml2:
			bKml2.setEnabled(false);
			Toast.makeText(this, "Please wait few seconds...", Toast.LENGTH_SHORT).show();

			// AsyncTask for saving the KML file on SD memory (only path).
			SaveKmlTask saveKml2 = new SaveKmlTask(this, "SaveKmlTask", fullUserName, 2);
			saveKml2.execute(C.URL_HISTORY_TABLE+"&event="+event, C.URL_CLIENTS_TABLE+"&event="+event);
			break;
		case R.id.bLogout:
			// Updates the SharedPreferences.
			SharedPreferences sp = getSharedPreferences(C.PREFS_USER, MODE_PRIVATE);
			SharedPreferences.Editor spEdit = sp.edit();
			spEdit.putString(C.PREFS_FULL_USER_NAME, "Anonymous");
			spEdit.commit();
			intent = new Intent(MenuActivity.this, LoginActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);
			finish();

			break;
		case R.id.bExit:
			finish();
			break;
		}
	}
}
