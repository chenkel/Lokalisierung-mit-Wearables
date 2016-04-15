package smartwatch.context.project.graphic;

// TODO: 15.04.16 Delete this class
class LocationCoordinates {

    private String name;
    private int xcoord;
    private int ycoord;

    public LocationCoordinates(String name, int x, int y) {
        this.name = name;
        this.xcoord = x;
        this.ycoord = y;
    }

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


}
