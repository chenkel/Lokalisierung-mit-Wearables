package project.context.localization.common.helper;


/**
 * The Position helper offers commonly used methods and attributes for resolving
 * information about places and their relating zone descriptions.
 */
public class PositionsHelper {
    /**
     * The constant ITEM_SCAN11 indicates its position in the menu at 1.
     */
    public static final int ITEM_SCAN11 = 1;
    /**
     * The constant ITEM_SCAN12 indicates its position in the menu at 2.
     */
    public static final int ITEM_SCAN12 = 2;
    /**
     * The constant ITEM_SCAN21 indicates its position in the menu at 3.
     */
    public static final int ITEM_SCAN21 = 3;
    /**
     * The constant ITEM_SCAN22 indicates its position in the menu at 4.
     */
    public static final int ITEM_SCAN22 = 4;
    /**
     * The constant ITEM_SCAN31 indicates its position in the menu at 5.
     */
    public static final int ITEM_SCAN31 = 5;
    /**
     * The constant ITEM_SCAN32 indicates its position in the menu at 6.
     */
    public static final int ITEM_SCAN32 = 6;
    /**
     * The constant ITEM_SCAN41 indicates its position in the menu at 7.
     */
    public static final int ITEM_SCAN41 = 7;
    /**
     * The constant ITEM_SCAN42 indicates its position in the menu at 8.
     */
    public static final int ITEM_SCAN42 = 8;

    private static int currentZone;

    /**
     * The constant bluePlaces contains all places in the cell of the blue beacon.
     */
    public static final String[] bluePlaces = {"11"};
    /**
     * The constant redPlaces contains all places in the cell of the red beacon.
     */
    public static String[] redPlaces = {"21"};
    /**
     * The constant yellowPlaces contains all places in the cell of the yellow beacon.
     */
    public static String[] yellowPlaces = {"31"};

    /**
     * Gets menu label for a scrolling position.
     *
     * @param position the position in the menu
     * @return the menu label for position
     */
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

    /**
     * Gets the zone (aggregation of places) by comparing prior and current place.
     *
     * @param priorPlace   the prior place during localization
     * @param currentPlace the current place during localization
     * @return if the the current zone changed
     */
    public static boolean isZoneDifferentWithPriorAndCurrentPlace(String priorPlace, String currentPlace) {
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

        currentZone = iFoundZone;

        return iPriorZone != iFoundZone;
    }


    /**
     * Returns the information to display the user at the current found place.
     *
     * The currentZone is stored in the PositionsHelper, thus, does not need
     * to be passed into this method.
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
