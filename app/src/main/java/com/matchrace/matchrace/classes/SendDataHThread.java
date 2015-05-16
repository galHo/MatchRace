package com.matchrace.matchrace.classes;

import android.os.HandlerThread;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * HandlerThread for sending the data to DB.
 */
public class SendDataHThread extends HandlerThread {

    private HttpURLConnection urlConnection;
    private String lat, lng, speed, bearing, event;
    private String name, fullUserName, password, user;
    private URL url;
    private String Finish_Status = "OK";
    private boolean ServerRespose = false;

    public SendDataHThread(String name) {
        super(name);
        this.name = name;
    }

    @Override
    public void run() {
        httpConnSendData();
    }

    /**
     * Creates the HTTP connection for sending data to DB.
     */
    private void httpConnSendData() {

        try {
            //	URL url = new URL(C.URL_INSERT_CLIENT + "&Latitude=" + lat +"&Longitude=" + lng +"&Pressure="+ speed + "&Azimuth="+ bearing + "&Bearing=" + bearing + "&Information=" + fullUserName + "&Event=" + event);
            urlConnection = (HttpURLConnection) url.openConnection();
            try {
                InputStream in = new BufferedInputStream(urlConnection.getInputStream());


                BufferedReader br = new BufferedReader(new InputStreamReader(in));
                String result = br.readLine();
                ServerRespose=true;
                if (!result.startsWith("OK")) { // Something is wrong.
                    Log.i(name, "Not OK!");
                    Finish_Status = result;

                } else { // Data sent.AD
                    Log.i(name, "OK!");

                }
            } catch (IOException e) {
                Log.i(name, "IOException");
                urlConnection.disconnect();
            }

        } catch (MalformedURLException e) {
            Log.i(name, "MalformedURLException");
        } catch (IOException e) {
            Log.i(name, "IOException");
        } finally {
            urlConnection.disconnect();

        }
    }


    public boolean ServerRespondig(){
        return ServerRespose;
    }

    // Getters and Setters.

    public void setUrl(String _url) {
        try {
            url = new URL(_url);
        } catch (IOException e) {
            Log.i(name, "IOException");
            urlConnection.disconnect();
        }

    }


    public String getFinish_Status() {
        String ans = Finish_Status;
        Finish_Status = "OK";
        return ans;
    }

    public String getFullUserName() {
        return fullUserName;
    }

    public void setFullUserName(String fullUserName) {
        this.fullUserName = fullUserName;
    }

    public String getPassword() {
        return this.password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUser() {
        return this.user;
    }

    public void setUser(String _user) {
        this.user = _user;
    }

    public String getLat() {
        return lat;
    }

    public void setLat(String lat) {
        this.lat = lat;
    }

    public String getLng() {
        return lng;
    }

    public void setLng(String lng) {
        this.lng = lng;
    }

    public String getSpeed() {
        return speed;
    }

    public void setSpeed(String speed) {
        this.speed = speed;
    }

    public String getBearing() {
        return bearing;
    }

    public void setBearing(String bearing) {
        this.bearing = bearing;
    }

    public String getEvent() {
        return event;
    }

    public void setEvent(String event) {
        this.event = event;
    }

}
