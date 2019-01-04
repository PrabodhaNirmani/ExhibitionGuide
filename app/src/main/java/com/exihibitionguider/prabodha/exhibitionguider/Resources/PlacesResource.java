package com.exihibitionguider.prabodha.exhibitionguider.Resources;

import com.exihibitionguider.prabodha.exhibitionguider.R;
import com.google.android.gms.maps.model.LatLng;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class PlacesResource {

    private static HashMap<Integer,LatLng> hmap;

    private static void createHashMap(){
        hmap = new HashMap<>();
//        Computer Science   :  6.7969719 79.9002586
//        Electronic and  :  6.7963888 79.9014382
//        Electrical   :  6.7966909 79.9002412
//        Civil   6.7985009 79.9024342
//        Mechanical   :  6.795808 79.8989296
//        Chemical and : : Result  :  6.7960251 79.899517
//        Material Scie: : Result  :  6.7964059 79.899855
//        Textile and  : Result  :  6.7983175 79.90155949999999
//        Earth ResourcesResult  :  6.7964033 79.8997772
//        Transport and  :  6.7977946 79.9018602

        hmap.put(1, new LatLng(6.7966,79.9002));
        hmap.put(2, new LatLng(6.7963,79.9014));
        hmap.put(3, new LatLng(6.7967,79.9002));
        hmap.put(4, new LatLng(6.7985,79.9024));
        hmap.put(5, new LatLng(6.7958,79.8989));

        hmap.put(6, new LatLng(6.7960,79.8995));
        hmap.put(7, new LatLng(6.7964,79.8998));
        hmap.put(8, new LatLng(6.7983,79.9015));
        hmap.put(9, new LatLng(6.7964,79.8998));
        hmap.put(10, new LatLng(6.7977,79.9018));



    }


    public static LatLng hasValue(Integer key){
        createHashMap();
        if(hmap.containsKey(key)){
            return hmap.get(key);
        }
        else {
            return null;
        }

    }

}
