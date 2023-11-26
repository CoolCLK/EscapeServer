package coolclk.escape.api;

public class Vector2f extends Vector2<Float> {
    public Vector2f(Float x, Float y) {
        super(x, y);
    }

    @Override
    public void addX(Float x) {
        this.setX(this.getX() + x);
    }

    @Override
    public void addY(Float y) {
        this.setY(this.getY() + y);
    }
}
