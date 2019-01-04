package com.exihibitionguider.prabodha.exhibitionguider.Activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import com.exihibitionguider.prabodha.exhibitionguider.Models.DirectionsJSONParser;
import com.exihibitionguider.prabodha.exhibitionguider.R;
import com.exihibitionguider.prabodha.exhibitionguider.Resources.BoundariesUOM;
import com.exihibitionguider.prabodha.exhibitionguider.Resources.PlacesResource;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class HomeActivity extends AppCompatActivity implements OnMapReadyCallback,AdapterView.OnItemSelectedListener,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private static final String TAG = "HomeActivity";
    private static final String MAP_VIEW_BUNDLE_KEY = "MapViewBundleKey";
    public static final String EXTRA_DATA_LAT = "data_extra_lat";
    public static final String EXTRA_DATA_LNG = "data_extra_lng";
    public static final String ACTION_LOCATION_RECEIVED = "action_location_received";
    public static final String ACTION_PATH_RECEIVED = "action_path_received";
    private MapView mapView;
    private GoogleMap gmap;
    private ProgressDialog progress;
    private IntentFilter receivedLocationFilter;
    private IntentFilter receivedPathFilter;
    private boolean isRegistered = false;

    private GoogleApiClient googleApiClient;
    private final static int REQUEST_CHECK_SETTINGS_GPS=0x1;
    private final static int REQUEST_ID_MULTIPLE_PERMISSIONS=0x2;
    private Location usersLocation;
    private LatLng markerDestLocation = null;
    private String markerDestination = "";




    private final BroadcastReceiver receivedLocationListener = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            dismissProgressDialog();
            if (markerDestLocation != null){
                updateUI();
            }
        }
    };

    private final BroadcastReceiver receivedPathListener = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            dismissProgressDialog();

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        initiateSpinner();
        initiateMap(savedInstanceState);
        //openDialog();
        receivedLocationFilter = new IntentFilter(ACTION_LOCATION_RECEIVED);
        registerReceiver(receivedLocationListener, receivedLocationFilter);
Log.d(TAG, "here");
//        receivedPathFilter = new IntentFilter(ACTION_PATH_RECEIVED);
//        registerReceiver(receivedPathListener, receivedPathFilter);

    }

    private void initiateSpinner() {
        Spinner spinner = findViewById(R.id.places_spinner);
        spinner.setOnItemSelectedListener(this);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.location_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }

    private void initiateMap(Bundle savedInstanceState){
        Bundle mapViewBundle = null;
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(MAP_VIEW_BUNDLE_KEY);
        }

        mapView = findViewById(R.id.map_view);
        mapView.onCreate(mapViewBundle);
        mapView.getMapAsync(this);
    }

    private void updateUI(){
        gmap.clear();
        createProgressDialog();
        if(!isRegistered){
            setUpGClient();
            isRegistered = true;
        }


    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long id) {

        if(pos != 0){
            markerDestination = adapterView.getItemAtPosition(pos).toString();

            markerDestLocation = PlacesResource.hasValue(pos);
            //getAddressFromLocation(markerDestination+", University of Moratuwa", getApplicationContext());
            createProgressDialog();
            Intent broadcast = new Intent(ACTION_LOCATION_RECEIVED);
            sendBroadcast(broadcast);

        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        Bundle mapViewBundle = outState.getBundle(MAP_VIEW_BUNDLE_KEY);
        if (mapViewBundle == null) {
            mapViewBundle = new Bundle();
            outState.putBundle(MAP_VIEW_BUNDLE_KEY, mapViewBundle);
        }

        mapView.onSaveInstanceState(mapViewBundle);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
        registerReceiver(receivedLocationListener,receivedLocationFilter);

    }

    @Override
    protected void onPause() {
        mapView.onPause();
        unregisterReceiver(receivedLocationListener);
        super.onPause();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mapView.onStop();
    }



    @Override
    protected void onDestroy() {
        mapView.onDestroy();
        super.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        gmap = googleMap;
        gmap.setMinZoomPreference(18);
        Intent intent = getIntent();
        LatLng latLng = new LatLng(intent.getDoubleExtra(EXTRA_DATA_LAT, 6.7968),
                intent.getDoubleExtra(EXTRA_DATA_LNG,79.9009));
        gmap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        MarkerOptions options = new MarkerOptions();
        options.position(latLng);
        options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
        gmap.addMarker(options);

    }

    private void getAddressFromLocation(final String locationAddress, final Context context) {
        Thread thread = new Thread() {
            @Override
            public void run() {
                Geocoder geocoder = new Geocoder(context, Locale.getDefault());

                try {

                    if(Geocoder.isPresent()){
                        List addressList = geocoder.getFromLocationName(locationAddress, 1);
                        if (addressList != null && addressList.size() > 0) {
                            Address address = (Address) addressList.get(0);
                            Double lat = address.getLatitude();
                            Double lng = address.getLongitude();
                            markerDestLocation = new LatLng(lat,lng);
                            Intent broadcast = new Intent(ACTION_LOCATION_RECEIVED);
                            sendBroadcast(broadcast);

                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
//                    Log.e(TAG, "Unable to connect to Geocoder", e);

                }
            }


        };

        thread.start();


    }

    private void createProgressDialog() {
        progress = new ProgressDialog(this);
        progress.setTitle(getResources().getString(R.string.load_act_progress_title));

        progress.setMessage(getResources().getString(R.string.load_act_progress_msg));
        progress.setCancelable(false); // disable dismiss by tapping outside of the dialog
        progress.show();
    }


    private void dismissProgressDialog() {
        progress.dismiss();
    }

    private synchronized void setUpGClient() {
        googleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, 0, this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        googleApiClient.connect();
    }


    @Override
    public void onLocationChanged(Location location) {
        usersLocation = location;
        if (usersLocation != null) {
            Double latitude= Math.round(usersLocation.getLatitude()*100.0)/100.0;
            Double longitude= Math.round(usersLocation.getLongitude()*100.0)/100.0;


            if(latitude<= BoundariesUOM.getRefLatitudeMax() &&
                    latitude>=BoundariesUOM.getRefLatitudeMin() &&
                    longitude<=BoundariesUOM.getRefLongitudeMax() &&
                    longitude>=BoundariesUOM.getRefLongitudeMin()){
                //inside premises
                //draw the path_selected
                //add red marker to destination
                //add green marker to start
                gmap.clear();
                ArrayList<LatLng> markerPoints= new ArrayList<>();

                LatLng markerOriginLocation = new LatLng(usersLocation.getLatitude(), usersLocation.getLongitude());

                markerPoints.add(markerOriginLocation);
                markerPoints.add(markerDestLocation);



                MarkerOptions options1 = new MarkerOptions();
                options1.position(markerOriginLocation);
                options1.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
                gmap.addMarker(options1);



                MarkerOptions options2 = new MarkerOptions();
                options2.position(markerDestLocation);
                options2.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
                gmap.addMarker(options2);

                Location locationA = new Location("point Start");

                locationA.setLatitude(markerOriginLocation.latitude);
                locationA.setLongitude(markerOriginLocation.longitude);

                Location locationB = new Location("point End");

                locationB.setLatitude(markerDestLocation.latitude);
                locationB.setLongitude(markerOriginLocation.longitude);

                float distance = locationA.distanceTo(locationB);

                if(distance>3){

                    // Getting URL to the Google Directions API
                    String url = getDirectionsUrl(markerOriginLocation, markerDestLocation);

                    DownloadTask downloadTask = new DownloadTask();

                    // Start downloading json data from Google Directions API
                    downloadTask.execute(url);


                }else {
                    //already near to the destination
                    dismissProgressDialog();
                    openDialog();

                }





            }
            else {
                //outside premises
                dismissProgressDialog();
                Intent intent = new Intent(this, LoadActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.putExtra(LoadActivity.EXTRA_DATA_FIRST_LOAD, false);
                startActivity(intent);
                finish();
            }
        }
    }

    private String getDirectionsUrl(LatLng origin, LatLng dest) {

        // Origin of route
        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;

        // Destination of route
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;

        // Sensor enabled
        String sensor = "sensor=false";
        String mode = "mode=driving";

        // Building the parameters to the web service
        String parameters = str_origin + "&" + str_dest + "&" + sensor + "&" + mode;

        // Output format
        String output = "json";

        // Building the url to the web service
        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters;


        return url;
    }


    @Override
    public void onConnected(Bundle bundle) {
        checkPermissions();
    }

    @Override
    public void onConnectionSuspended(int i) {
        //Do whatever you need
        //You can display a message here
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        //You can display a message here
    }

    private void getMyLocation(){
        if(googleApiClient!=null) {
            if (googleApiClient.isConnected()) {
                int permissionLocation = ContextCompat.checkSelfPermission(HomeActivity.this,
                        Manifest.permission.ACCESS_FINE_LOCATION);
                if (permissionLocation == PackageManager.PERMISSION_GRANTED) {
                    usersLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
                    LocationRequest locationRequest = new LocationRequest();
                    locationRequest.setInterval(5000);
                    locationRequest.setFastestInterval(5000);
                    locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
                    LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                            .addLocationRequest(locationRequest);
                    builder.setAlwaysShow(true);
                    LocationServices.FusedLocationApi
                            .requestLocationUpdates(googleApiClient, locationRequest, this);
                    PendingResult<LocationSettingsResult> result =
                            LocationServices.SettingsApi
                                    .checkLocationSettings(googleApiClient, builder.build());
                    result.setResultCallback(new ResultCallback<LocationSettingsResult>() {

                        @Override
                        public void onResult(LocationSettingsResult result) {
                            final Status status = result.getStatus();
                            //dismissProgressDialog();
                            switch (status.getStatusCode()) {
                                case LocationSettingsStatusCodes.SUCCESS:
                                    // All location settings are satisfied.
                                    // You can initialize location requests here.
                                    int permissionLocation = ContextCompat
                                            .checkSelfPermission(HomeActivity.this,
                                                    Manifest.permission.ACCESS_FINE_LOCATION);
                                    if (permissionLocation == PackageManager.PERMISSION_GRANTED) {
                                        usersLocation = LocationServices.FusedLocationApi
                                                .getLastLocation(googleApiClient);
                                    }
                                    break;
                                case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                                    dismissProgressDialog();

                                    // Location settings are not satisfied.
                                    // But could be fixed by showing the user a dialog.
                                    try {
                                        // Show the dialog by calling startResolutionForResult(),
                                        // and check the result in onActivityResult().
                                        // Ask to turn on GPS automatically
                                        status.startResolutionForResult(HomeActivity.this,
                                                REQUEST_CHECK_SETTINGS_GPS);
                                    } catch (IntentSender.SendIntentException e) {
                                        // Ignore the error.
                                        e.printStackTrace();
                                    }
                                    break;
                                case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                                    dismissProgressDialog();

                                    Intent intent = new Intent(HomeActivity.this, LoadActivity.class);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                    intent.putExtra(LoadActivity.EXTRA_DATA_FIRST_LOAD, false);
                                    startActivity(intent);
                                    finish();
                                    // Location settings are not satisfied.
                                    // However, we have no way
                                    // to fix the
                                    // settings so we won't show the dialog.
                                    break;
                            }

                        }
                    });
                }
            }
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        createProgressDialog();
        switch (requestCode) {
            case REQUEST_CHECK_SETTINGS_GPS:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        getMyLocation();
                        break;
                    case Activity.RESULT_CANCELED:
                        dismissProgressDialog();
                        Intent intent = new Intent(this, LoadActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        intent.putExtra(LoadActivity.EXTRA_DATA_FIRST_LOAD, false);
                        startActivity(intent);
                        finish();
                        break;
                }
                break;
        }
    }

    private void checkPermissions(){
        int permissionLocation = ContextCompat.checkSelfPermission(HomeActivity.this,
                android.Manifest.permission.ACCESS_FINE_LOCATION);
        List<String> listPermissionsNeeded = new ArrayList<>();
        if (permissionLocation != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(android.Manifest.permission.ACCESS_FINE_LOCATION);
            if (!listPermissionsNeeded.isEmpty()) {
                ActivityCompat.requestPermissions(this,
                        listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]), REQUEST_ID_MULTIPLE_PERMISSIONS);
            }
        }else{
            getMyLocation();
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        int permissionLocation = ContextCompat.checkSelfPermission(HomeActivity.this,
                Manifest.permission.ACCESS_FINE_LOCATION);
        if (permissionLocation == PackageManager.PERMISSION_GRANTED) {
            getMyLocation();
        }
    }


    @SuppressLint("SetTextI18n")
    private void openDialog(){

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.home_act_custom_progress_title);
        builder.setMessage(getResources().getString(R.string.home_act_custom_progress_body)
                +markerDestination);
        builder.setPositiveButton(R.string.home_act_custom_progress_btn_neutral_text,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(getApplicationContext(), MasterActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                        finish();

                    }
                });

        builder.setCancelable(false);
        builder.show();

    }

    private class DownloadTask extends AsyncTask<String,Void,String> {

        @Override
        protected String doInBackground(String... url) {

            String data = "";

            try {
                data = downloadUrl(url[0]);
            } catch (Exception e) {
                Log.d("Background Task", e.toString());
            }
            return data;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            ParserTask parserTask = new ParserTask();


            parserTask.execute(result);

        }
    }

    private class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String,String>>>> {

        // Parsing the data in non-ui thread
        @Override
        protected List<List<HashMap<String,String>>> doInBackground(String... jsonData) {

            JSONObject jObject;
            List<List<HashMap<String,String>>> routes = null;

            try {
                jObject = new JSONObject(jsonData[0]);
                DirectionsJSONParser parser = new DirectionsJSONParser();

                routes = parser.parse(jObject);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return routes;
        }

        @Override
        protected void onPostExecute(List<List<HashMap<String,String>>> result) {
            ArrayList<LatLng> points = null;
            PolylineOptions lineOptions = null;

            Log.d(TAG,result.get(0).size()+"");

            for (int i = 0; i < result.size(); i++) {
                points = new ArrayList<>();
                lineOptions = new PolylineOptions();

                List<HashMap<String, String>> path = result.get(i);

                for (int j = 0; j < path.size(); j++) {
                    HashMap<String, String> point = path.get(j);

                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat, lng);

                    points.add(position);
                }

                lineOptions.addAll(points);
                lineOptions.width(12);
                lineOptions.color(Color.RED);
                lineOptions.geodesic(true);

            }


// Drawing polyline in the Google Map for the i-th route
            gmap.addPolyline(lineOptions);
            dismissProgressDialog();
        }
    }

    private void findRoute(){

    }

    private String downloadUrl(String strUrl) throws IOException {
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL(strUrl);

            urlConnection = (HttpURLConnection) url.openConnection();

            urlConnection.connect();

            iStream = urlConnection.getInputStream();

            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));

            StringBuffer sb = new StringBuffer();

            String line = "";
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }

            data = sb.toString();

            br.close();

        } catch (Exception e) {
            Log.d("Exception", e.toString());
        } finally {
            iStream.close();
            urlConnection.disconnect();
        }
        return data;
    }




}
