package com.matchrace.matchrace.classes;

import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.matchrace.matchrace.R;
import com.matchrace.matchrace.modules.JsonReader;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * AsyncTask for getting the sailor's locations from DB and adding them to the google map.
 *
 */
public class GetSailorsTask extends AsyncTask<String, Integer, Map<String, LatLng>> {

	// Application variables.
	private String name = "", fullUserName = "", event = "";

	// Views.
	private List<Marker> sailorMarkers = new ArrayList<Marker>();
	private GoogleMap googleMap;

	public GetSailorsTask(String name, GoogleMap googleMap, List<Marker> sailorMarkers, String fullUserName, String event) {
		super();
		this.name = name;
		this.googleMap = googleMap;
		this.sailorMarkers = sailorMarkers;
		this.fullUserName = fullUserName;
		this.event = event;
	}

    /**
     * The function get String of urls and return a Map with all the loctions
     * for a specified usename ans event.
     * * @param urls
     * @return
     */
	protected  Map<String, LatLng> doInBackground(String... urls) {
		Map<String, LatLng> sailorsLatLng = new HashMap<String, LatLng>();
		try {
			JSONObject json = JsonReader.readJsonFromUrl(urls[0]);
			JSONArray jsonArray = json.getJSONArray("positions");
			for (int i = 0; i < jsonArray.length(); i++) {
				JSONObject jsonObj = (JSONObject) jsonArray.get(i);
				if (jsonObj.getString("info").startsWith(C.SAILOR_PREFIX)) {
					if (jsonObj.getString("event").equals(event)) {
						String sailorFullName = jsonObj.getString("info");
						if (sailorFullName.equals((fullUserName))) {
							continue;
						}
						String lat = jsonObj.getString("lat");
						String lng = jsonObj.getString("lon");
						if (Double.parseDouble(lat) == 0 || Double.parseDouble(lng) == 0) {
							continue;
						}
						String sailorName = sailorFullName.split("_")[0].substring(6);
						sailorsLatLng.put(sailorName, new LatLng(Double.parseDouble(lat), Double.parseDouble(lng)));

						Log.i(name + " " + sailorName + " " + event, "Lat: " + lat + ", Lng: " + lng);
					}
				}
			}
			return sailorsLatLng;
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

    /**
     * geting a new map, deleting all the old markers and adding th new form sailorsLatLng;
     * @param sailorsLatLng a new map to pot on the showed one.
     */
	protected  void onPostExecute(Map<String, LatLng> sailorsLatLng) {
		if (sailorsLatLng != null) {
			// Removes from map all previous sailors.
			if (!sailorMarkers.isEmpty()) {
				for (Marker markerSailor : sailorMarkers) {
					markerSailor.remove();
				}
				sailorMarkers.clear();
			}

			// Adds to map new sailors with new locations.
			for (Map.Entry<String, LatLng> entry :  sailorsLatLng.entrySet()) {
				String sailorName = entry.getKey();
				LatLng sailorLatLng = entry.getValue();
				LatLng latLng  = new LatLng(sailorLatLng.latitude, sailorLatLng.longitude);
				sailorMarkers.add(googleMap.addMarker(new MarkerOptions().position(latLng).title(sailorName).icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_sailor_low))));
			}
		}
	}
}
