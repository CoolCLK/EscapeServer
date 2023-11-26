package coolclk.escape.api;

public class Vector2d extends Vector2<Double> {
    public Vector2d(Double x, Double y) {
        super(x, y);
    }

    @Override
    public void addX(Double x) {
        this.setX(this.getX() + x);
    }

    @Override
    public void addY(Double y) {
        this.setY(this.getY() + y);
    }
}
