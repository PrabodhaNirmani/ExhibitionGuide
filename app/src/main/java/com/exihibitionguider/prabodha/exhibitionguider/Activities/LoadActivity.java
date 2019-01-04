package com.exihibitionguider.prabodha.exhibitionguider.Activities;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import com.exihibitionguider.prabodha.exhibitionguider.R;
import com.exihibitionguider.prabodha.exhibitionguider.Resources.BoundariesUOM;
import com.google.android.gms.location.LocationListener;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class LoadActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener{

    private ProgressDialog progress;
    private GoogleApiClient googleApiClient;
    private final static int REQUEST_CHECK_SETTINGS_GPS=0x1;
    private final static int REQUEST_ID_MULTIPLE_PERMISSIONS=0x2;
    private Location usersLocation;

    private static final String TAG = "LoadActivity";
    public static final String EXTRA_DATA_FIRST_LOAD = "extra_data_first_load";

    private Context context;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_load);
        context = this.getApplicationContext();

        Intent intent = getIntent();
        boolean isFirst = intent.getBooleanExtra(EXTRA_DATA_FIRST_LOAD,true);
        if(isFirst){
            createProgressDialog();
            setUpGClient();

        }else {
            updateUI(getResources().getString(R.string.load_act_error_msg2));
        }


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
                Intent intent = new Intent(this, HomeActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.putExtra(HomeActivity.EXTRA_DATA_LAT, usersLocation.getLatitude());
                intent.putExtra(HomeActivity.EXTRA_DATA_LNG,usersLocation.getLongitude());
                startActivity(intent);
                finish();
                dismissProgressDialog();

            }
            else {
                //outside premises
                dismissProgressDialog();

                updateUI(getResources().getString(R.string.load_act_error_msg1));
            }
        }
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

    private void findLatLng(){
        Thread thread = new Thread() {
            @Override
            public void run() {
                Geocoder geocoder = new Geocoder(context, Locale.getDefault());

                try {

                    if(Geocoder.isPresent()){
                        String [] lst = getResources().getStringArray(R.array.location_array);
                        for (String name:lst){
                            List addressList = geocoder.getFromLocationName(name+", University of Moratuwa", 1);
                            if (addressList != null && addressList.size() > 0) {
                                Address address = (Address) addressList.get(0);
                                Double lat = address.getLatitude();
                                Double lng = address.getLongitude();
                                Log.d(name,": Result  :  "+lat+" "+lng);

                            }
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
    private void getMyLocation(){
        if(googleApiClient!=null) {
            if (googleApiClient.isConnected()) {
                int permissionLocation = ContextCompat.checkSelfPermission(LoadActivity.this,
                        Manifest.permission.ACCESS_FINE_LOCATION);
                if (permissionLocation == PackageManager.PERMISSION_GRANTED) {
                    usersLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
                    LocationRequest locationRequest = new LocationRequest();
                    locationRequest.setInterval(3000);
                    locationRequest.setFastestInterval(3000);
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
                                            .checkSelfPermission(LoadActivity.this,
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
                                        status.startResolutionForResult(LoadActivity.this,
                                                REQUEST_CHECK_SETTINGS_GPS);
                                    } catch (IntentSender.SendIntentException e) {
                                        // Ignore the error.
                                        e.printStackTrace();
                                    }
                                    break;
                                case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                                    dismissProgressDialog();

                                    updateUI(getResources().getString(R.string.load_act_error_msg2));

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
                        updateUI(getResources().getString(R.string.load_act_error_msg2));
                        break;
                }
                break;
        }
    }

    private void checkPermissions(){
        int permissionLocation = ContextCompat.checkSelfPermission(LoadActivity.this,
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
        int permissionLocation = ContextCompat.checkSelfPermission(LoadActivity.this,
                Manifest.permission.ACCESS_FINE_LOCATION);
        if (permissionLocation == PackageManager.PERMISSION_GRANTED) {
            getMyLocation();
        }
    }


    private void createProgressDialog() {
        progress = new ProgressDialog(this);
        progress.setTitle(getResources().getString(R.string.load_act_progress_title));

        progress.setMessage(getResources().getString(R.string.load_act_progress_msg));
        progress.setCancelable(false); // disable dismiss by tapping outside of the dialog
        progress.show();
    }

    private void updateUI(String message){
        TextView msgTitle = findViewById(R.id.error_msg_title);
        msgTitle.setVisibility(View.VISIBLE);
        TextView msg = findViewById(R.id.error_msg);
        msg.setText(message);
        msg.setVisibility(View.VISIBLE);
    }

    private void dismissProgressDialog() {
        progress.dismiss();
    }


}
