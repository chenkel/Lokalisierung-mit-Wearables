package smartwatch.context.project.graphic;

import java.util.HashMap;

/**
 * Created by jan on 02.04.16.
 */
public class LocationCoordinates {

    private String name;
    private int xcoord;
    private int ycoord;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getXcoord() {
        return xcoord;
    }

    public void setXcoord(int xcoord) {
        this.xcoord = xcoord;
    }

    public int getYcoord() {
        return ycoord;
    }

    public void setYcoord(int ycoord) {
        this.ycoord = ycoord;
    }

    public LocationCoordinates(String name, int x, int y){
        this.name = name;
        this.xcoord = x;
        this.ycoord = y;
    }






}
