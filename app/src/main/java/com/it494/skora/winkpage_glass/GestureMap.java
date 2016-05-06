package com.it494.skora.winkpage_glass;

/**
 * Created by satokokora on 4/4/16.
 */
public class GestureMap {


    public static String WINK = "WINK";
    public static String NOD ="NOD";
    public static String SHAKE_LEFT ="SHAKELEFT";
    public static String SHAKE_RIGHT ="SHAKERIGHT";
    public static String LOOKUP ="LOOKUP";
//    public static int DOUBLE_WINK =1;
//    public static int DOUBLE_BLINK =2;
//    public static int HEAD_SWING_LEFT =3;
//    public static int HEAD_SWING_RIGHT =4;
//    public static int LOOKUP =5;
//    public static int LOOKDOWN =6;

    public static int DOUBLE_NOD =8;
    public static int NONE =9;

    private static String nextPageGesture=WINK;
    private static String prevPageGesture=SHAKE_RIGHT;
//    private static String topPageGesture=LOOKUP;
//    private static String lastPageGesture=NOD;
    public GestureMap()
    {
        nextPageGesture=WINK;
        prevPageGesture=SHAKE_RIGHT;
//        topPageGesture=LOOKUP;
//        lastPageGesture=NOD;

    }

    public static String getNextPageGesture() {
        return nextPageGesture;
    }

    public static void setNextPageGesture(String gesture) {
        nextPageGesture = gesture;
    }

    public static String getPrevPageGesture() {
        return prevPageGesture;
    }

    public static void setPrevPageGesture(String gesture) {
        prevPageGesture = gesture;
    }

//    public static String getTopPageGesture() {
//        return topPageGesture;
//    }
//
//    public static void setTopPageGesture(String gesture) {
//        topPageGesture = gesture;
//    }
//
//    public static String getLastPageGesture() {
//        return lastPageGesture;
//    }
//
//    public static void setLastPageGesture(String gesture) {
//        lastPageGesture = gesture;
//    }
}
