package com.matchrace.matchrace.classes;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.matchrace.matchrace.modules.JsonReader;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

/**
 * AsyncTask for saving the KML file on SD memory.
 *
 */
public class SaveKmlTask extends AsyncTask<String, Integer, Map<Long, LatLng>> {

	// Application variables.
	private Context context;
	private String name, fullUserName;
	private int kmlVer;

	public SaveKmlTask(Context context, String name, String fullUserName, int kmlVer) {
		super();
		this.context = context;
		this.name = name;
		this.fullUserName = fullUserName;
		this.kmlVer = kmlVer;
	}

	@Override
	protected Map<Long, LatLng> doInBackground(String... urls) {
		Map<Long, LatLng> sortedLatLngs = new TreeMap<Long, LatLng>();
		try {
			JSONObject jsonHistory = JsonReader.readJsonFromUrl(urls[0]);
			JSONArray jsonArray = jsonHistory.getJSONArray("positions");
			for (int i = 0; i < jsonArray.length(); i++) {
				JSONObject jsonObj = (JSONObject) jsonArray.get(i);
				if (jsonObj.getString("info").equals(fullUserName)) {
					String lat = jsonObj.getString("lat");
					String lng = jsonObj.getString("lon");
					if (Double.parseDouble(lat) == 0 || Double.parseDouble(lng) == 0) {
						continue;
					}
					String time = jsonObj.getString("time");
					LatLng latLng = new LatLng(Double.parseDouble(lat), Double.parseDouble(lng));

					// Adds sailor's data to TreeMap.
					sortedLatLngs.put(Long.parseLong(time), latLng);

					Log.i(fullUserName, "Lat: " + lat + ", Lng: " + lng);
				}
			}

			JSONObject jsonClients = JsonReader.readJsonFromUrl(urls[1]);
			jsonArray = jsonClients.getJSONArray("positions");
			for (int i = 0; i < jsonArray.length(); i++) {
				JSONObject jsonObj = (JSONObject) jsonArray.get(i);
				if (jsonObj.getString("info").equals(fullUserName)) {
					String lat = jsonObj.getString("lat");
					String lng = jsonObj.getString("lon");
					if (Double.parseDouble(lat) == 0 || Double.parseDouble(lng) == 0) {
						break;
					}
					String time = jsonObj.getString("time");
					LatLng latLng = new LatLng(Double.parseDouble(lat), Double.parseDouble(lng));

					// Adds sailor's last data to TreeMap.
					sortedLatLngs.put(Long.parseLong(time), latLng);

					Log.i(fullUserName, "Lat: " + lat + ", Lng: " + lng);
					break;
				}
			}

			return sortedLatLngs;
		}
		catch (JSONException e) {
			Log.i(name, "JSONException");
			// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~  have to check !!!!   ~~~~~~~~~~~~~~~~~~~~~~~~~
			//                                this is what make it works
			return sortedLatLngs;
		}
		catch (IOException e) {
			Log.i(name, "IOException");
			return null;
		}
	}

	protected void onPostExecute(Map<Long, LatLng> sortedLatLngs) {
		if (sortedLatLngs != null && sortedLatLngs.size() > 1) {
			if (kmlVer == 1) {
				Iterator<Map.Entry<Long, LatLng>> iter = sortedLatLngs.entrySet().iterator();
				Map.Entry<Long, LatLng> entry = (Map.Entry<Long, LatLng>) iter.next();

				// Builds the KML content.
				StringBuilder kmlBuilder = new StringBuilder();
				kmlBuilder.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
				kmlBuilder.append("<kml xmlns=\"http://earth.google.com/kml/2.2\">\n");
				kmlBuilder.append("<Document>\n");
				kmlBuilder.append("\t<name>Path</name>\n\t<description><![CDATA[]]></description>\n");
				kmlBuilder.append("\t<Style id=\"style1\">\n\t\t<IconStyle>\n\t\t\t<Icon>\n\t\t\t\t<href>http://maps.google.com/mapfiles/kml/paddle/grn-blank.png</href>\n");
				kmlBuilder.append("\t\t\t</Icon>\n\t\t</IconStyle>\n\t</Style>\n\t<Style id=\"style2\">\n");
				kmlBuilder.append("\t\t<IconStyle>\n\t\t\t<Icon>\n\t\t\t\t<href>http://maps.google.com/mapfiles/kml/paddle/wht-blank.png</href>\n");
				kmlBuilder.append("\t\t\t</Icon>\n\t\t</IconStyle>\n\t</Style>\n\t<Style id=\"style3\">\n\t\t<IconStyle>\n");
				kmlBuilder.append("\t\t\t<Icon>\n\t\t\t\t<href>http://maps.google.com/mapfiles/kml/paddle/red-stars.png</href>\n\t\t\t</Icon>\n\t\t</IconStyle>\n\t</Style>\n");

				// Starting time stamp.
				kmlBuilder.append("\t<Placemark>\n\t\t<name>FROM</name>\n\t\t<TimeStamp>\n");
				long starttime = entry.getKey();
				kmlBuilder.append("\t\t\t<when>" + String.valueOf(starttime) + "</when>\n\t\t</TimeStamp>\n");
				kmlBuilder.append("\t\t<styleUrl>#style1</styleUrl>\n\t\t<Point>\n\t\t\t<coordinates>");

				// Starting coordinate.
				double startLat = entry.getValue().latitude;
				double startLng = entry.getValue().longitude;
				kmlBuilder.append(String.valueOf(startLng) + "," + String.valueOf(startLat) + ",0.000000");
				kmlBuilder.append("</coordinates>\n\t\t</Point>\n\t</Placemark>\n");

				// Inserts all of path's coordinates.
				while (iter.hasNext()) {
					entry = (Map.Entry<Long, LatLng>) iter.next();
					if (!iter.hasNext()) {
						break;
					}
					kmlBuilder.append("\t<Placemark>\n\t\t<TimeStamp>\n");
					long time = entry.getKey();
					kmlBuilder.append("\t\t\t<when>" + String.valueOf(time) + "</when>\n\t\t</TimeStamp>\n");
					kmlBuilder.append("\t\t<styleUrl>#style2</styleUrl>\n\t\t<Point>\n\t\t\t<coordinates>");

					double lat = entry.getValue().latitude;
					double lng = entry.getValue().longitude;
					kmlBuilder.append(String.valueOf(lng) + "," + String.valueOf(lat) + ",0.000000");
					kmlBuilder.append("</coordinates>\n\t\t</Point>\n\t</Placemark>\n");
				}

				// Inserts the latest time stamp.
				kmlBuilder.append("\t<Placemark>\n\t\t<name>TO</name>\n\t\t<TimeStamp>\n");
				long time = entry.getKey();
				kmlBuilder.append("\t\t\t<when>" + String.valueOf(time) + "</when>\n\t\t</TimeStamp>\n");
				kmlBuilder.append("\t\t<styleUrl>#style3</styleUrl>\n\t\t<Point>\n\t\t\t<coordinates>");

				// Inserts the latest destination reached.
				double lat = entry.getValue().latitude;
				double lng = entry.getValue().longitude;
				kmlBuilder.append(String.valueOf(lng) + "," + String.valueOf(lat) + ",0.000000");
				kmlBuilder.append("</coordinates>\n\t\t</Point>\n\t</Placemark>\n");

				kmlBuilder.append("</Document>\n</kml>");

				// Creates a new directory for the application on SD memory.
				File file = new File(C.APP_DIR + "KMLFiles/");
				if (!file.exists()) {
					file.mkdirs();
				}

				// Creates a unique KML file by time stamp.
				String timeStamp = new SimpleDateFormat("ddMMyy_HHmmss", Locale.US).format(new Date());
				String user = fullUserName.split("_")[0];
				String event = fullUserName.split("_")[2];
				file = new File(C.APP_DIR + "KMLFiles/" + user + "_" + event + "_" + timeStamp + "_WithTimeStamp.kml");
				boolean success = false;
				try {
					FileWriter fwriter = new FileWriter(file);
					BufferedWriter bwriter = new BufferedWriter(fwriter);
					bwriter.write(kmlBuilder.toString());

					bwriter.close();
					fwriter.close();

					success = true;
				}
				catch (Exception e) {
					e.printStackTrace();
				}
				if (success) {
					Toast.makeText(context, "KML File Created!", Toast.LENGTH_LONG).show();
				}
				else {
					Toast.makeText(context, "ERROR creating KML file!", Toast.LENGTH_LONG).show();
				}
			}
			else if (kmlVer == 2) {
				Iterator<Map.Entry<Long, LatLng>> iter = sortedLatLngs.entrySet().iterator();

				// Builds the KML content.
				StringBuilder kmlBuilder = new StringBuilder();
				kmlBuilder.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
				kmlBuilder.append("<kml xmlns=\"http://earth.google.com/kml/2.2\">\n");
				kmlBuilder.append("<Document>\n");
				kmlBuilder.append("\t<name>Path</name>\n\t<description><![CDATA[]]></description>\n");
				kmlBuilder.append("\t<Style id=\"style3\">\n\t\t<IconStyle>\n\t\t\t<Icon>\n\t\t\t\t<href>http://maps.google.com/mapfiles/kml/paddle/grn-blank.png</href>\n");
				kmlBuilder.append("\t\t\t</Icon>\n\t\t</IconStyle>\n\t</Style>\n\t<Style id=\"style2\">\n");
				kmlBuilder.append("\t\t<LineStyle>\n\t\t\t<color>73FF0000</color>\n\t\t\t<width>5</width>\n");
				kmlBuilder.append("\t\t</LineStyle>\n\t</Style>\n\t<Style id=\"style1\">\n\t\t<IconStyle>\n");
				kmlBuilder.append("\t\t\t<Icon>\n\t\t\t\t<href>http://maps.google.com/mapfiles/kml/paddle/red-stars.png</href>\n\t\t\t</Icon>\n\t\t</IconStyle>\n\t</Style>\n");
				kmlBuilder.append("\t<Placemark>\n\t\t<name>FROM</name>\n\t\t<styleUrl>#style3</styleUrl>\n\t\t<Point>\n\t\t\t<coordinates>");

				// Starting coordinate.
				Map.Entry<Long, LatLng> entry = (Map.Entry<Long, LatLng>) iter.next();
				double startLat = entry.getValue().latitude;
				double startLng = entry.getValue().longitude;
				kmlBuilder.append(String.valueOf(startLng) + "," + String.valueOf(startLat) + ",0.000000</coordinates>\n\t\t</Point>\n\t</Placemark>\n");

				kmlBuilder.append("\t<Placemark>\n\t\t<name>PATH</name>\n\t\t<styleUrl>#style2</styleUrl>\n\t\t<ExtendedData>\n\t\t\t<Data name=\"_SnapToRoads\">\n");
				kmlBuilder.append("\t\t\t\t<value>true</value>\n\t\t\t</Data>\n\t\t</ExtendedData>\n\t\t<LineString>\n\t\t\t<tessellate>1</tessellate>\n");
				kmlBuilder.append("\t\t\t<coordinates>\n");

				// Inserts all of path's coordinates.
				while (iter.hasNext()) {
					entry = (Map.Entry<Long, LatLng>) iter.next();
					if (!iter.hasNext()) {
						break;
					}
					double lat = entry.getValue().latitude;
					double lng = entry.getValue().longitude;
					kmlBuilder.append("\t\t\t\t" + String.valueOf(lng) + "," + String.valueOf(lat) + ",0.000000\n");
				}
				kmlBuilder.append("\t\t\t</coordinates>\n\t\t</LineString>\n\t</Placemark>\n");
				kmlBuilder.append("\t<Placemark>\n\t\t<name>TO</name>\n\t\t<styleUrl>#style1</styleUrl>\n\t\t<Point>\n\t\t\t<coordinates>");

				// Inserts the latest destination reached.
				double lat = entry.getValue().latitude;
				double lng = entry.getValue().longitude;
				kmlBuilder.append(String.valueOf(lng) + "," + String.valueOf(lat) + ",0.000000");

				kmlBuilder.append("</coordinates>\n\t\t</Point>\n\t</Placemark>\n");
				kmlBuilder.append("</Document>\n</kml>");

				// Creates a new directory for the application on SD memory.
				File file = new File(C.APP_DIR + "KMLFiles/");
				if (!file.exists()) {
					file.mkdirs();
				}

				// Creates a unique KML file by time stamp.
				String timeStamp = new SimpleDateFormat("ddMMyy_HHmmss", Locale.US).format(new Date());
				String user = fullUserName.split("_")[0];
				String event = fullUserName.split("_")[2];
				file = new File(C.APP_DIR + "KMLFiles/" + user + "_" + event + "_" + timeStamp + "_OnlyPath.kml");
				boolean success = false;
				try {
					FileWriter fwriter = new FileWriter(file);
					BufferedWriter bwriter = new BufferedWriter(fwriter);
					bwriter.write(kmlBuilder.toString());

					bwriter.close();
					fwriter.close();

					success = true;
				}
				catch (Exception e) {
					e.printStackTrace();
				}
				if (success) {
					Toast.makeText(context, "KML File Created!", Toast.LENGTH_LONG).show();
				}
				else {
					Toast.makeText(context, "ERROR creating KML file!", Toast.LENGTH_LONG).show();
				}
			}
		}
	}

}
