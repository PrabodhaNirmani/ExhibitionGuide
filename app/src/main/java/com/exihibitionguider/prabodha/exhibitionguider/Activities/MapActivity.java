package com.exihibitionguider.prabodha.exhibitionguider.Activities;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.provider.MediaStore;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.exihibitionguider.prabodha.exhibitionguider.Models.Dijkstra;
import com.exihibitionguider.prabodha.exhibitionguider.Models.Vertex;
import com.exihibitionguider.prabodha.exhibitionguider.R;
import com.exihibitionguider.prabodha.exhibitionguider.Resources.DevicesResource;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class MapActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener{

    private HashMap<Integer,ImageView> nodes;
    private HashMap<String,ArrayList<ImageView>> edges;
    private List<Vertex> prevPath = null;
    private int prevDest = 0;
    private static final String TAG = "MapActivity";
    private static final int REQUEST_VIDEO_CAPTURE = 1;
    private int prevSource = 0;

    private Context context;
    WifiManager mainWifi;
    DevicesResource accessPoints;
    ProgressDialog progress;



    private final BroadcastReceiver receivedWifiScansListener = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //                if(result.SSID.equalsIgnoreCase("UoM_Wireless")){
            //                }
            ArrayList<ScanResult> availableAP = new ArrayList<>(mainWifi.getScanResults());
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
                int source = accessPoints.hasValue(sortedWifiList.get(0).BSSID);
                if(source !=0){
//                    source available
                    if(prevSource!=source){

                        //source changed
                        if(prevSource!=0){
                            ImageView sourceView = nodes.get(prevSource);
                            sourceView.setImageDrawable(getResources().getDrawable(R.drawable.place));
                        }


                        ImageView newSourceView = nodes.get(source);
                        newSourceView.setImageDrawable(getResources().getDrawable(R.drawable.origin));

                        String currentPlace = getResources().getStringArray(R.array.exhibits_array)[source];





                        if(prevDest == source){
                            prevDest = 0;
                            AlertDialog.Builder builder = new AlertDialog.Builder(MapActivity.this);
                            builder.setTitle(R.string.map_act_dialog_title);
                            builder.setMessage(getResources().getString(R.string.map_act_dialog_body));
                            builder.setNegativeButton(R.string.map_act_dialog_btn_neutral_text,
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {

                                        }
                                    });

                            builder.setPositiveButton(R.string.map_act_dialog_btn_video_text,
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                            dispatchTakeVideoIntent();
                                        }
                                    });

                            builder.setCancelable(false);
                            builder.show();
                            constructPath(prevPath,false);

                        }else {
                            Toast.makeText(context,"You are near to "+currentPlace+" stole",
                                    Toast.LENGTH_LONG).show();
                            if(prevDest!=0){
                                constructPath(prevPath,false);
                                List<Vertex> path = getPath(source,prevDest);
                                constructPath(path, true);
                                prevPath = path;
                            }

                        }

                        prevSource = source;

                    }

                }
                else{
                    //showUnavailability();
                }

            }else {
                //showUnavailability();
            }

        }

    };

    private void dispatchTakeVideoIntent() {
        Intent takeVideoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        if (takeVideoIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takeVideoIntent, REQUEST_VIDEO_CAPTURE);
        }
    }

    private List<Vertex> getPath(int source, int dest){
        Dijkstra graph = new Dijkstra();
        graph.createGraph();


        graph.computePaths(source);
        Toast.makeText(context,"You are "+
                        graph.getVertex(dest).minDistance+"m away from your destination",
                Toast.LENGTH_LONG).show();
        return graph.getShortestPathTo(dest);

    }

    private void showUnavailability(){
        AlertDialog.Builder builder = new AlertDialog.Builder(MapActivity.this);
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
        setContentView(R.layout.activity_map);
        context = getApplicationContext();

        initializeNodes();
        initializeEdges();
        initiateSpinner();

        final long period = 10000;
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                // do your task here
                checkConnectionInfo();
            }
        }, 0, period);

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

    private void initiateSpinner() {
        Spinner spinner = findViewById(R.id.map_act_spinner);
        spinner.setOnItemSelectedListener(this);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.exhibits_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long l) {
        //int start = 19;
        if(prevPath != null){
            constructPath(prevPath,false);
        }
        if(prevDest != 0){
            ImageView dest = nodes.get(prevDest);
            dest.setImageDrawable(getResources().getDrawable(R.drawable.place));

        }
//        if(prevSource != 0){
//            ImageView source = nodes.get(prevSource);
//            source.setImageDrawable(getResources().getDrawable(R.drawable.place));
//
//        }
        if(pos!=0){

//            ImageView source = nodes.get(start);
//            source.setImageDrawable(getResources().getDrawable(R.drawable.origin));

            ImageView dest = nodes.get(pos);
            dest.setImageDrawable(getResources().getDrawable(R.drawable.destination));


            Toast.makeText(context,"Your destination set to "
                            +adapterView.getItemAtPosition(pos)+" stole",
                            Toast.LENGTH_LONG).show();

            List<Vertex> path = getPath(prevSource,pos);
            constructPath(path, true);
            prevPath = path;
            prevDest = pos;



        }
//        prevSource = start;


    }


    private void constructPath(List<Vertex> path,boolean isDraw){
        for (int i=0;i<path.size()-1;i++){

            String edge = path.get(i).name+" "+path.get(i+1);
            Log.d("path", edge);

            if(!edges.containsKey(edge)){
                String[] a = edge.split(" ");
                edge= a[1]+" "+a[0];

            }
            ArrayList<ImageView> edgesList = edges.get(edge);

            Log.d("null",edgesList.size()+"");
            for (ImageView edgeView:edgesList){
                if(isDraw){
                    edgeView.setImageDrawable(
                            getResources().getDrawable(R.drawable.path_selected));
                }
                else {
                    edgeView.setImageDrawable(
                            getResources().getDrawable(R.drawable.path_unselected));
                }


            }


        }
    }



    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

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

        //createProgressDialog();




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
    private void initializeNodes(){

        nodes = new HashMap<>();
        nodes.put(1,(ImageView)findViewById(R.id.a));
        nodes.put(2,(ImageView)findViewById(R.id.b));
        nodes.put(3,(ImageView)findViewById(R.id.c));
        nodes.put(4,(ImageView)findViewById(R.id.d));
        nodes.put(5,(ImageView)findViewById(R.id.e));
        nodes.put(6,(ImageView)findViewById(R.id.f));
        nodes.put(7,(ImageView)findViewById(R.id.g));
        nodes.put(8,(ImageView)findViewById(R.id.h));
        nodes.put(9,(ImageView)findViewById(R.id.i));
        nodes.put(10,(ImageView)findViewById(R.id.j));
        nodes.put(11,(ImageView)findViewById(R.id.k));
        nodes.put(12,(ImageView)findViewById(R.id.l));
        nodes.put(13,(ImageView)findViewById(R.id.m));
        nodes.put(14,(ImageView)findViewById(R.id.n));
        nodes.put(15,(ImageView)findViewById(R.id.o));
        nodes.put(16,(ImageView)findViewById(R.id.p));
        nodes.put(17,(ImageView)findViewById(R.id.q));
        nodes.put(18,(ImageView)findViewById(R.id.r));
        nodes.put(19,(ImageView)findViewById(R.id.s));
    }

    private void initializeEdges(){
        edges = new HashMap<>();
        ArrayList<ImageView> lst1 = new ArrayList<>();
        lst1.add((ImageView)findViewById(R.id.a_route));
        lst1.add((ImageView)findViewById(R.id.b_route));
        edges.put("1 2",lst1);

        ArrayList<ImageView> lst2 = new ArrayList<>();
        lst2.add((ImageView)findViewById(R.id.b_route));
        lst2.add((ImageView)findViewById(R.id.c_route));
        edges.put("2 3",lst2);

        ArrayList<ImageView> lst3 = new ArrayList<>();
        lst3.add((ImageView)findViewById(R.id.d_route));
        lst3.add((ImageView)findViewById(R.id.c_route));
        edges.put("3 4",lst3);

        ArrayList<ImageView> lst4 = new ArrayList<>();
        lst4.add((ImageView)findViewById(R.id.d_route));
        lst4.add((ImageView)findViewById(R.id.e_route));
        edges.put("4 5",lst4);

        ArrayList<ImageView> lst5 = new ArrayList<>();
        lst5.add((ImageView)findViewById(R.id.f_route));
        lst5.add((ImageView)findViewById(R.id.e_route));
        lst5.add((ImageView)findViewById(R.id.fe_route));
        edges.put("5 6",lst5);


        ArrayList<ImageView> lst6 = new ArrayList<>();
        lst6.add((ImageView)findViewById(R.id.f_route));
        lst6.add((ImageView)findViewById(R.id.g_route));
        edges.put("6 7",lst6);

        ArrayList<ImageView> lst7 = new ArrayList<>();
        lst7.add((ImageView)findViewById(R.id.h_route));
        lst7.add((ImageView)findViewById(R.id.g_route));
        edges.put("7 8",lst7);

        ArrayList<ImageView> lst8 = new ArrayList<>();
        lst8.add((ImageView)findViewById(R.id.i_route));
        lst8.add((ImageView)findViewById(R.id.h_route));
        edges.put("8 9",lst8);

        ArrayList<ImageView> lst9 = new ArrayList<>();
        lst9.add((ImageView)findViewById(R.id.i_route));
        lst9.add((ImageView)findViewById(R.id.j_route));
        edges.put("9 10",lst9);

        ArrayList<ImageView> lst10 = new ArrayList<>();
        lst10.add((ImageView)findViewById(R.id.j_route));
        lst10.add((ImageView)findViewById(R.id.k_route));
        edges.put("10 11",lst10);

        ArrayList<ImageView> lst11 = new ArrayList<>();
        lst11.add((ImageView)findViewById(R.id.k_route));
        lst11.add((ImageView)findViewById(R.id.l_route));
        edges.put("11 12",lst11);

        ArrayList<ImageView> lst12 = new ArrayList<>();
        lst12.add((ImageView)findViewById(R.id.l_route));
        lst12.add((ImageView)findViewById(R.id.m_route));
        edges.put("12 13",lst12);

        ArrayList<ImageView> lst13 = new ArrayList<>();
        lst13.add((ImageView)findViewById(R.id.m_route));
        lst13.add((ImageView)findViewById(R.id.n_route));
        edges.put("13 14",lst13);

        ArrayList<ImageView> lst14 = new ArrayList<>();
        lst14.add((ImageView)findViewById(R.id.n_route));
        lst14.add((ImageView)findViewById(R.id.o_route));
        lst14.add((ImageView)findViewById(R.id.on_route));
        edges.put("14 15",lst14);

        ArrayList<ImageView> lst15 = new ArrayList<>();
        lst15.add((ImageView)findViewById(R.id.p_route));
        lst15.add((ImageView)findViewById(R.id.o_route));
        edges.put("15 16",lst15);

        ArrayList<ImageView> lst16 = new ArrayList<>();
        lst16.add((ImageView)findViewById(R.id.p_route));
        lst16.add((ImageView)findViewById(R.id.q_route));
        edges.put("16 17",lst16);

        ArrayList<ImageView> lst17 = new ArrayList<>();
        lst17.add((ImageView)findViewById(R.id.c_route));
        lst17.add((ImageView)findViewById(R.id.sd_route));
        lst17.add((ImageView)findViewById(R.id.s_route));
        edges.put("3 19",lst17);

        ArrayList<ImageView> lst18 = new ArrayList<>();
        lst18.add((ImageView)findViewById(R.id.r_route));
        lst18.add((ImageView)findViewById(R.id.s_route));
        edges.put("18 19",lst18);

        ArrayList<ImageView> lst19 = new ArrayList<>();
        lst19.add((ImageView)findViewById(R.id.r_route));
        lst19.add((ImageView)findViewById(R.id.kr_route));
        lst19.add((ImageView)findViewById(R.id.k_route));
        edges.put("11 18",lst19);

        ArrayList<ImageView> lst20 = new ArrayList<>();
        lst20.add((ImageView)findViewById(R.id.q_route));
        lst20.add((ImageView)findViewById(R.id.qa1_route));
        lst20.add((ImageView)findViewById(R.id.qa2_route));
        edges.put("17 1",lst20);

        ArrayList<ImageView> lst21 = new ArrayList<>();
        lst21.add((ImageView)findViewById(R.id.d_route));
        lst21.add((ImageView)findViewById(R.id.sd_route));
        lst21.add((ImageView)findViewById(R.id.s_route));
        edges.put("4 19",lst21);

        ArrayList<ImageView> lst22 = new ArrayList<>();
        lst22.add((ImageView)findViewById(R.id.r_route));
        lst22.add((ImageView)findViewById(R.id.kr_route));
        lst22.add((ImageView)findViewById(R.id.l_route));
        edges.put("12 18",lst22);



        //}


    }
}
