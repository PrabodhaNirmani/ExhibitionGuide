package com.exihibitionguider.prabodha.exhibitionguider.Activities;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.drm.ProcessedData;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import com.exihibitionguider.prabodha.exhibitionguider.R;
import com.exihibitionguider.prabodha.exhibitionguider.Resources.DevicesResource;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MasterActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {


    private Context context;
    WifiManager mainWifi;
    DevicesResource accessPoints;
    ProgressDialog progress;


    private final BroadcastReceiver receivedWifiScansListener = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            ArrayList<ScanResult> availableAP = new ArrayList<>();
            if(mainWifi!=null){
                for(ScanResult result : mainWifi.getScanResults()){

                    availableAP.add(result);

                }
                if(availableAP.size()>0){

                    List<ScanResult> sortedWifiList = new ArrayList<ScanResult>(availableAP);

                    // Create Comparator to sort by level
                    Comparator<ScanResult> comparator =
                            new Comparator<ScanResult>() {

                                @Override
                                public int compare(ScanResult lhs, ScanResult rhs) {
                                    return (lhs.level < rhs.level ? 1 :
                                            (lhs.level == rhs.level ? 0 : -1));
                                }
                            };

                    // Apply Comparator and sort
                    Collections.sort(sortedWifiList, comparator);

                    int ap = accessPoints.hasValue(sortedWifiList.get(0).BSSID);

                    if(ap!=0){
                        AlertDialog.Builder builder = new AlertDialog.Builder(MasterActivity.this);
                        builder.setTitle(getResources().getString(R.string.master_act_dialog_title)+" CSE");
                        builder.setMessage(getResources().getString(R.string.master_act_dialog_body)
                                +" Computer Science and Engineering Department");
                        builder.setPositiveButton(R.string.master_act_dialog_btn_neutral_text,
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        Intent intent = new Intent(getApplicationContext(), MapActivity.class);
                                        startActivity(intent);

                                    }
                                });

                        builder.setCancelable(false);
                        builder.show();
                        dismissProgressDialog();

                    }
                    else{
                        showUnavailability();
                    }

                }else {
                    showUnavailability();
                }

            }

        }
    };

    private void showUnavailability(){

        dismissProgressDialog();
        AlertDialog.Builder builder = new AlertDialog.Builder(MasterActivity.this);
        builder.setTitle(R.string.master_act_error1_dialog_title);
        builder.setMessage(getResources().getString(R.string.master_act_error1_dialog_body));
        builder.setPositiveButton(R.string.master_act_error1_dialog_btn_neutral_text,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });

        builder.setCancelable(false);
        builder.show();

    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_master);
        initializeUI();
        context = getApplicationContext();

    }


    private void initializeUI(){
        initiateSpinner();
        Button btn_search = findViewById(R.id.btn_search);
        btn_search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                Intent intent = new Intent(getApplicationContext(), MapActivity.class);
                startActivity(intent);

            }
        });
    }
    private void initiateSpinner() {
        Spinner spinner = findViewById(R.id.places_spinner);
        spinner.setOnItemSelectedListener(this);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.location_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {

        if(position !=0){
            if(position == 1){
                checkConnectionInfo();

            }else {
                AlertDialog.Builder builder = new AlertDialog.Builder(MasterActivity.this);
                builder.setTitle(R.string.master_act_error_dialog_title);
                builder.setMessage(getResources().getString(R.string.master_act_error_dialog_body));
                builder.setPositiveButton(R.string.master_act_error_dialog_btn_neutral_text,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        });

                builder.setCancelable(false);
                builder.show();
            }
        }

    }

    private void checkConnectionInfo(){

        accessPoints = new DevicesResource();
        accessPoints.createHashMap();
        WifiManager wifi = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        assert wifi != null;
        if (!wifi.isWifiEnabled())
        {
            Toast.makeText(getApplicationContext(), getResources().getString(R.string.master_act_wifi_enable_toast_text), Toast.LENGTH_LONG).show();
            wifi.setWifiEnabled(true);
        }
        mainWifi = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);

        assert mainWifi != null;
        mainWifi.startScan();

        createProgressDialog();




    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(receivedWifiScansListener,
                new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));

    }

    @Override
    protected void onPause() {
        unregisterReceiver(receivedWifiScansListener);
        super.onPause();
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

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


//    private class CustomComparator implements Comparator<ScanResult> {
//        @Override
//        public int compare(ScanResult o1, ScanResult o2) {
//            return o1.level.compareTo(o2.level);
//        }
//    }



}
