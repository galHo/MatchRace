package com.matchrace.matchrace.classes;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.graphics.Color;
import android.os.AsyncTask;
import android.util.Log;

import com.matchrace.matchrace.R;
import com.matchrace.matchrace.modules.JsonReader;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

/**
 * AsyncTask for getting the buoy's locations from DB and adding them to the google map.
 *
 */
public class GetBuoysTask extends AsyncTask<String, Integer, Map<String, LatLng>> {

	// Application variables.
	private String name = "", event = "";
	private Circle[] buoyRadiuses = new Circle[C.MAX_BUOYS];

	// Views.
	private GoogleMap googleMap;

	public GetBuoysTask(String name, GoogleMap googleMap, Circle[] buoyRadiuses, String event) {
		super();
		this.name = name;
		this.googleMap = googleMap;
		this.buoyRadiuses = buoyRadiuses;
		this.event = event;
	}

	protected Map<String, LatLng> doInBackground(String... urls) {
		Map<String, LatLng> buoysLatLng = new HashMap<String, LatLng>();
		try {
			JSONObject json = JsonReader.readJsonFromUrl(urls[0]);
			JSONArray jsonArray = json.getJSONArray("positions");
			int countBouy = 0;
			for (int i = 0; i < jsonArray.length() && countBouy < C.MAX_BUOYS; i++) {
				JSONObject jsonObj = (JSONObject) jsonArray.get(i);
				if (jsonObj.getString("info").startsWith(C.BUOY_PREFIX)) {
					if (jsonObj.getString("event").equals(event)) {
						countBouy++;
						String buoyName = jsonObj.getString("info").split("_")[0];
						String lat = jsonObj.getString("lat");
						String lng = jsonObj.getString("lon");

						// Adds buoy with LatLng to HashMap.
						buoysLatLng.put(buoyName, new LatLng(Double.parseDouble(lat), Double.parseDouble(lng)));

						Log.i(name + " " + buoyName + " " + event, "Lat: " + lat + ", Lng: " + lng);
					}
				}
			}
			return buoysLatLng;
		}
		catch (JSONException e) {
			Log.i(name, "JSONException");
			return null;
		}
		catch (IOException e) {
			Log.i(name, "IOException");
			return null;
		}
	}

	protected void onPostExecute(Map<String, LatLng> buoysLatLng) {
		if (buoysLatLng != null) {
			// Random latitude and longitude.
			LatLng latLng = new LatLng(32.056286, 34.824598);
			int j = 0;
			for (Map.Entry<String, LatLng> entry : buoysLatLng.entrySet()) {
				if (j < buoyRadiuses.length) {
					String buoyName = entry.getKey();
					LatLng buoyLatLng = entry.getValue();

					// Adds a buoy on the google map.
					latLng = new LatLng(buoyLatLng.latitude, buoyLatLng.longitude);
					googleMap.addMarker(new MarkerOptions().position(latLng).title(buoyName).icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_buoy_low)));

					// Adds circles/radiuses around each buoy on the google map.
					buoyRadiuses[j++] = googleMap.addCircle(new CircleOptions()
					.center(latLng)
					.radius(C.RADIUS_BUOY)
					.strokeColor(Color.RED)
					.strokeWidth(1L)
					.fillColor(Color.argb(50, 0, 0, 255)));
				}
			}

			// Focus the camera on the latest buoy added to HashMap.
			CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, C.ZOOM_LEVEL);
			googleMap.animateCamera(cameraUpdate);
		}
	}

}
