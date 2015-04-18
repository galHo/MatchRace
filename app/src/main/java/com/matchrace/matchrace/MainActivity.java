package com.matchrace.matchrace;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Process;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.matchrace.matchrace.classes.C;
import com.matchrace.matchrace.classes.GetBuoysTask;
import com.matchrace.matchrace.classes.GetSailorsTask;
import com.matchrace.matchrace.classes.SendDataHThread;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Main activity. Shows a google map with the sailors, buoys and current position.
 * 
 */
public class MainActivity extends FragmentActivity implements LocationListener {

	// Application variables.
	private String user = "", pass = "", event = "", fullUserName = "";
	private LocationManager locationManager;
	private Circle[] buoyRadiuses = new Circle[C.MAX_BUOYS];
	private MediaPlayer buoyBeep;
	private boolean disableLocation = false;

	// Views.
	private Marker currentPosition;
	private List<Marker> sailorMarkers = new ArrayList<Marker>();
	private GoogleMap googleMap;
	private TextView tvLat, tvLng, tvUser, tvSpeed, tvDirection, tvEvent;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
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

		// Initialize location ability.
		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		Criteria criteria = new Criteria();
		criteria.setAccuracy(Criteria.ACCURACY_COARSE);
		String provider = locationManager.getBestProvider(criteria, true);
		locationManager.requestLocationUpdates(provider, C.MIN_TIME, C.MIN_DISTANCE, this);

		// Initialize map.
		FragmentManager fm = getSupportFragmentManager();
		googleMap = ((SupportMapFragment) fm.findFragmentById(R.id.map)).getMap();

		// Adds location button in the top-right screen.
		googleMap.setMyLocationEnabled(true);

		// Focus the camera on the latLng location.
		LatLng latLng = new LatLng(32.056286, 34.824598);
		CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, C.ZOOM_LEVEL);
		googleMap.animateCamera(cameraUpdate);

		// Initializing TextViews.
		tvLat = (TextView) findViewById(R.id.tvLat);
		tvLng = (TextView) findViewById(R.id.tvLng);
		tvSpeed = (TextView) findViewById(R.id.tvSpeed);
		tvDirection = (TextView) findViewById(R.id.tvDirection);
		tvUser = (TextView) findViewById(R.id.tvUser);
		tvEvent = (TextView) findViewById(R.id.tvEvent);
		tvUser.setText(user.substring(6));
		tvEvent.setText(event);

		// Loads the buoy warning beep sound.
		buoyBeep = MediaPlayer.create(this, R.raw.buoy_warning_beep);

		// AsyncTask for getting the buoy's locations from DB and adding them to the google map.
		GetBuoysTask getBuoys = new GetBuoysTask("GetBuoysTask", googleMap, buoyRadiuses, event);
		getBuoys.execute(C.URL_CLIENTS_TABLE);

		// AsyncTask for getting the sailor's locations from DB and adding them to the google map.
		GetSailorsTask getSailors = new GetSailorsTask("GetSailorsTask", googleMap, sailorMarkers, fullUserName, event);
		getSailors.execute(C.URL_CLIENTS_TABLE);
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();

		// Disables the location changed code.
		disableLocation = true;
		finish();
	}

	@Override
	public void onLocationChanged(Location location) {
		if (!disableLocation) {
			// HandlerThread for sending the current location to DB.
			SendDataHThread thread = new SendDataHThread("SendGPS");
			thread.setPriority(Process.THREAD_PRIORITY_BACKGROUND);

			String lat = new DecimalFormat("##.######").format(location.getLatitude());
			String lng = new DecimalFormat("##.######").format(location.getLongitude());
			String speed = "" + location.getSpeed();
			String bearing = "" + location.getBearing();

			thread.setFullUserName(fullUserName);
			thread.setLat(lat);
			thread.setLng(lng);
			thread.setSpeed(speed);
			thread.setBearing(bearing);
			thread.setEvent(event);
            thread.setUrl(C.URL_INSERT_CLIENT + "&Latitude=" + thread.getLat() +"&Longitude=" + thread.getLng() +"&Pressure="+ thread.getSpeed() + "&Azimuth="+ thread.getBearing() + "&Bearing=" + thread.getBearing() + "&Information=" + thread.getFullUserName() + "&Event=" + thread.getEvent());
			thread.start();

			// AsyncTask for getting the sailor's locations from DB and adding them to the google map.
			GetSailorsTask getSailors = new GetSailorsTask("GetSailorsTask", googleMap, sailorMarkers, fullUserName, event);
			getSailors.execute(C.URL_CLIENTS_TABLE);

			// Updates TextViews in layout.
			tvLat.setText(lat);
			tvLng.setText(lng);
			tvSpeed.setText(speed);
			tvDirection.setText(bearing);

			// Adds currentPosition marker to the google map.
			LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
			if (currentPosition != null) {
				currentPosition.remove();
			}
			currentPosition = googleMap.addMarker(new MarkerOptions().position(latLng).title("Current Position").icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_sailor_user_low)));

			// Focus the camera on the currentPosition marker.
			CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, C.ZOOM_LEVEL);
			googleMap.animateCamera(cameraUpdate);

			// Checks if the user is near a buoy by measuring the distance between the currentPosition marker and the radius around the buoys. If near then a beep sound is made.
			float[] distance = new float[2];
			for (int i = 0; i < buoyRadiuses.length; i++) {
				if (buoyRadiuses[i] != null) {
					Location.distanceBetween(currentPosition.getPosition().latitude, currentPosition.getPosition().longitude, buoyRadiuses[i].getCenter().latitude, buoyRadiuses[i].getCenter().longitude, distance);
					if (distance[0] < buoyRadiuses[i].getRadius()) {
						buoyBeep.start();
						Toast.makeText(this, "Near a buoy! move away!", Toast.LENGTH_LONG).show();
						break;
					}
				}
			}
		}
	}

	@Override
	public void onProviderDisabled(String provider) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onProviderEnabled(String provider) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub

	}

}
