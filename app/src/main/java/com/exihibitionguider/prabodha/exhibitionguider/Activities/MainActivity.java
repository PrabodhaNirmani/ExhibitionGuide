package com.exihibitionguider.prabodha.exhibitionguider.Activities;

import android.content.Context;
import android.net.DhcpInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.format.Formatter;
import android.util.Log;

import com.exihibitionguider.prabodha.exhibitionguider.R;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    List<ScanResult> wifiList;

    public static final String EXTRA_DATA_LOCATION = "data_extra_location";
    private String department = "";
    private static final String TAG = "MainActivity";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        department = getIntent().getStringExtra(EXTRA_DATA_LOCATION);
        //discoverClients();
        //scanWifiList();
    }

    private void scanWifiList() {
        WifiManager mainWifi = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);

        mainWifi.startScan();
        wifiList = mainWifi.getScanResults();

        // Create Temporary HashMap
        for (ScanResult res : wifiList){
            Log.d(TAG,res.BSSID);
            Log.d(TAG,res.SSID);
            Log.d(TAG,res.frequency+"");
            Log.d(TAG,res.level+"");
            Log.d(TAG,"     \n");


        }

        WifiManager wifiMan = (WifiManager) this.getApplicationContext().getSystemService(
                Context.WIFI_SERVICE);
        assert wifiMan != null;
        WifiInfo wifiInf = wifiMan.getConnectionInfo();
        String macAddr = wifiInf.getBSSID();
        Log.d(TAG, macAddr);



    }

    private void discoverClients(){
        //clientsList.clear();
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader("/proc/net/arp"));
            String line;

            while ((line = br.readLine()) != null) {
                String[] clientInfo = line.split(" +");
                String mac = clientInfo[3];
                int i=0;
                for (String client:clientInfo){
                    Log.d(TAG,i+"  "+line);
                    i++;
                }
//                if (mac.matches("..:..:..:..:..:..")) { // To make sure its not the title
//                    //Client client = new Client(clientInfo[0],clientInfo[3]);
//                    //clientsList.add(client);
//                }

            }
        } catch (java.io.IOException aE) {
            aE.printStackTrace();

        }
    }
}
