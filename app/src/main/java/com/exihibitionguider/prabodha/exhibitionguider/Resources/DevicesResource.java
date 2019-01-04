package com.exihibitionguider.prabodha.exhibitionguider.Resources;

import java.util.HashMap;

public class DevicesResource {
    private static HashMap<String,Integer> hmap;

    public static void createHashMap(){
        hmap = new HashMap<>();
//        hmap.put("f4:7f:35:98:82:a0", 1);
//        hmap.put("18:8b:45:10:79:60", 2);
//        hmap.put("00:b0:e1:99:44:c0", 3);
//        hmap.put("28:52:61:c0:67:d0", 5);

//
        hmap.put("f4:7f:35:98:82:a0", 1);     //fpy
        hmap.put("02:0a:00:1a:57:a3", 2);     //tab
        hmap.put("00:b0:e1:99:44:c0", 3);     //coridor
        hmap.put("f2:98:9d:98:30:be", 5);     //sajinie phone
//        hmap.put("28:52:61:c0:67:d0", 5);     //ice
        hmap.put("18:8b:45:11:33:c0", 4);     //ice


//        hmap.put("00:b0:e1:a1:3f:a0", 1); //hostel
//
//        hmap.put("02:0a:00:1a:57:a3", 2);   //tab
//        hmap.put("5c:03:39:46:c1:df", 3);   //madhushika
//        hmap.put("00:b0:e1:a1:1d:80", 4);   //hostel
////        hmap.put("74:a5:28:9b:c5:f4", 5);   //sajine router
//        hmap.put("f2:98:9d:98:30:be", 5);   //sajine phone


        hmap.put("00:6b:f1:a3:66:20", 11);
        hmap.put("18:8b:45:11:e2:40", 15);
        hmap.put("18:8b:45:10:90:20", 16);



    }


    public static int hasValue(String key){
        if(hmap.containsKey(key)){
            return hmap.get(key);
        }
        else {
            return 0;
        }

    }
}
