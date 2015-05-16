package com.matchrace.matchrace.classes;

import android.os.Environment;

/**
 * The application's constants.
 *
 */
public class C {

	// Location constants.
	public static final long MIN_TIME = 4000;
	public static final float MIN_DISTANCE = 10f;

	// Map constants.
	public static final float RADIUS_BUOY = 40f;
	public static final float ZOOM_LEVEL = 17.0f;
	public static final int MAX_BUOYS = 3;

	// Users constants.
	public static final String SAILOR_PREFIX = "Sailor";
	public static final String BUOY_PREFIX = "BuoyNum";

	// Login constants.
	public static final String USER_NAME = "user_name";
	public static final String USER_PASS = "user_pass";
	public static final String EVENT_NUM = "event_num";
	public static final String PREFS_USER = "user_prefs";
	public static final String PREFS_FULL_USER_NAME = "full_user_name";


	// DB constants1.
    ///http://openweathermap.org/current
    public static final String WEATHER_API = "http://api.openweathermap.org/data/2.5/weather?";//lat=35&lon=139
    public static final String URL_INSERT_CLIENT = "http://matala3.bugs3.com/insertClient.php";
    public static final String URL_INSERT_HISTORY = "http://matala3.bugs3.com/insertHistory.php";
    public static final String URL_USER_TABLE = "http://matala3.bugs3.com/Registration.php?table=users";
    public static final String URL_CLIENTS_TABLE = "http://matala3.bugs3.com/json-clients.php?table=clients";//USERs Table
    public static final String URL_CLIENTS_LOGIN= "http://matala3.bugs3.com/login.php?table=users";//USERs Table
    public static final String URL_CLIENTS_EVENT= "http://matala3.bugs3.com/evet.php?table=clients";//USERs Table
    public static final String URL_HISTORY_TABLE = "http://matala3.bugs3.com/json-history.php?table=history";

	// Data constants.
	public static final String APP_DIR = Environment.getExternalStorageDirectory().getPath() + "/BlindMatchRace/";
}
