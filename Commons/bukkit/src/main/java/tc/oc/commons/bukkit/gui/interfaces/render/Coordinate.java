package tc.oc.commons.bukkit.gui.interfaces.render;

public class Coordinate {

    private double x = 0;
    private double y = 0;

    public Coordinate(double x, double y) {
        setX(x);
        setY(y);
    }

    public double getX() {
        return this.x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return this.y;
    }

    public void setY(double y) {
        this.y = y;
    }

}
