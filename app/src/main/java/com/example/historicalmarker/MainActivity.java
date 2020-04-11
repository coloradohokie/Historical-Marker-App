package com.example.historicalmarker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    LocationManager locationManager;
    LocationListener locationListener;
    public static ListView markerListView;
    Boolean appActive;
    Button startButton;
    ArrayList<HashMap<String, String>> markerList;


    public void handleClick(View view) {
        if (appActive) {
            startButton.setText("Start");
            appActive = false;
        } else {
            startButton.setText("Stop");
            appActive = true;
            new GetMarkers().execute();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i("connection", "connected");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        appActive = false;
        startButton = findViewById(R.id.startButton);
        startButton.setText("Start");
        markerList = new ArrayList<>();
        markerListView = findViewById(R.id.markerListView);
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                updateLocationInfo(location);

            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        };


        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        } else {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
            Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (lastKnownLocation != null) {
                updateLocationInfo(lastKnownLocation);
            }
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startListening();
        }
    }

    public void startListening() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
        }
    }

    public void updateLocationInfo(Location location) {
        TextView latTextView = findViewById(R.id.latTextView);
        TextView addressTextView = findViewById(R.id.addressTextView);

        latTextView.setText(String.format("%.6g", location.getLatitude()) + ", " + String.format("%.6g", location.getLongitude()));
        String address = "Unknown Location";
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());

        try {
            List<Address> listAddresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
            address = "";
            if (listAddresses != null && listAddresses.size() >0 ) {
                if (listAddresses.get(0).getLocality() != null) {
                    address += listAddresses.get(0).getLocality() + ", ";
                }
                if (listAddresses.get(0).getAdminArea() != null) {
                    address += listAddresses.get(0).getAdminArea();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        addressTextView.setText(address);

//        if(markerList.size() > 0) {
//            getDistanceToMarker();
//        }
    }

    class GetMarkers extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Toast.makeText(MainActivity.this, "Getting Markers", Toast.LENGTH_LONG).show();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            HttpHandler sh = new HttpHandler();
            String url = "https://historical-marker.herokuapp.com/markers";
            String jsonStr = sh.makeServiceCall(url);

            if (jsonStr != null) {
                try {
                    JSONArray markers = new JSONArray(jsonStr);
                    for (int i=0; i<markers.length(); i++) {
                        JSONObject marker = markers.getJSONObject(i);
                        String id = marker.getString("id");
                        String marker_id = marker.getString("marker_id");
                        String title = marker.getString("title");
                        String subtitle1 = marker.getString("subtitle1");
                        String subtitle2 = marker.getString("subtitle2");
                        String year = marker.getString("year");
                        String erected_by = marker.getString("erected_by");
                        String latitude = marker.getString("latitude");
                        String longitude = marker.getString("longitude");
                        String address = marker.getString("address");
                        String town = marker.getString("town");
                        String county = marker.getString("county");
                        String state = marker.getString("state");
                        String location = marker.getString("location");
                        String link = marker.getString("url");
                        String inscription = marker.getString("inscription");

                        HashMap<String, String> m = new HashMap<>();
                        m.put("id", id);
                        m.put("marker_id", marker_id);
                        m.put("title", title);
                        m.put("subtitle1", subtitle1);
                        m.put("subtitle2", subtitle2);
                        m.put("year", year);
                        m.put("erected_by", erected_by);
                        m.put("latitude", latitude);
                        m.put("longitude", longitude);
                        m.put("address", address);
                        m.put("town", town);
                        m.put("county", county);
                        m.put("state", state);
                        m.put("location", location);
                        m.put("url", link);
                        m.put("inscription", inscription);
                        markerList.add(m);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
                Log.i("message", "can't get the json from the server");
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            Log.i("info", "JSON done");
            getDistanceToMarker();
            ListAdapter listAdapter = new SimpleAdapter(MainActivity.this, markerList, R.layout.list_item, new String[]{"title", "distance"}, new int[]{R.id.marker_title, R.id.distance});
            markerListView.setAdapter(listAdapter);
        }
    }

    public void getDistanceToMarker() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        } else {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
            Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (lastKnownLocation != null) {
                updateLocationInfo(lastKnownLocation);
                for (int i=0; i<markerList.size(); i++) {
                    HashMap marker = markerList.get(i);
                    if ((markerList.get(i).get("latitude") != null) && (marker.get("longitude") != null)){
                        double latitude = Double.parseDouble((String) Objects.requireNonNull(marker.get("latitude")));
                        double longitude = Double.parseDouble((String) Objects.requireNonNull(marker.get("longitude")));
                        double distance = 0;
                        Location markerLocation = new Location("markerLocation");
                        markerLocation.setLatitude(latitude);
                        markerLocation.setLongitude(longitude);
                        distance = lastKnownLocation.distanceTo(markerLocation) / 1000 * 0.621371;
                        Log.i("marker distance", Double.toString(distance));
                        marker.put("distance", (String.format("%.3g", distance) + " mi. away"));
                    }
                }
            }
        }

    }

}
