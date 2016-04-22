package project.context.localization.common.helper;


import android.util.Log;

public class PositionHelper {
    private static final String TAG = PositionHelper.class.getSimpleName();

    public static final int ITEM_SCAN11 = 1;
    public static final int ITEM_SCAN12 = 2;
    public static final int ITEM_SCAN21 = 3;
    public static final int ITEM_SCAN22 = 4;
    public static final int ITEM_SCAN31 = 5;
    public static final int ITEM_SCAN32 = 6;
    public static final int ITEM_SCAN41 = 7;
    public static final int ITEM_SCAN42 = 8;

    private static int currentZone;

    public static final String[] bluePlaces = {"11"}; // placeIds in cell of blue beacon
    public static String[] redPlaces = {"21"}; // placeIds in cell of red beacon
    public static String[] yellowPlaces = {"31"}; // placeIds in cell of yellow beacon

    public static String getMenuLabelForPosition(int position) {
        switch (position) {
            case ITEM_SCAN11:
                return "11";
            case ITEM_SCAN12:
                return "12";
            case ITEM_SCAN21:
                return "21";
            case ITEM_SCAN22:
                return "22";
            case ITEM_SCAN31:
                return "31";
            case ITEM_SCAN32:
                return "32";
            case ITEM_SCAN41:
                return "41";
            case ITEM_SCAN42:
                return "42";
            default:
                return "00";
        }
    }

    public static boolean getZoneWithPriorAndCurrentPlace(String priorPlace, String currentPlace) {
        int iPriorPlace = -1;
        int iFoundPlace = -2;

        if (!priorPlace.isEmpty()) {
            iPriorPlace = Integer.valueOf(priorPlace);
        }
        if (!currentPlace.isEmpty()) {
            iFoundPlace = Integer.valueOf(currentPlace);
        }

        int iPriorZone = (iPriorPlace / 10);
        int iFoundZone = (iFoundPlace / 10);
        Log.d(TAG, "iPriorZone: " + iPriorZone + " iFoundZone: " + iFoundZone);

        currentZone = iFoundZone;

        return iPriorZone != iFoundZone;
    }


    /**
     * Returns the information to display the user at the current found place.
     *
     * @return String to give a user a hint where to go next
     */
    public static String getCurrentZoneDescription() {
        String sDescription = "";
        switch (currentZone) {
            case 1:
                sDescription = "Verlasse das Zimmer und gehe nach links";
                break;
            case 2:
                sDescription = "Gehe durch die Glastür";
                break;
            case 3:
                sDescription = "Gehe nach rechts";
                break;
            case 4:
                sDescription = "Gehe durch die Notfalltür";
                break;
        }
        return sDescription;
    }
}
