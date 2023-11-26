package coolclk.escape.api;

public abstract class Vector2<T extends Number> {
    private T x, y;

    public Vector2(T x, T y) {
        this.x = x;
        this.y = y;
    }

    public T getX() {
        return this.x;
    }

    public T getY() {
        return this.y;
    }

    public void setX(T x) {
        this.x = x;
    }

    public void setY(T y) {
        this.y = y;
    }

    public void setPosition(T x, T y) {
        this.setX(x);
        this.setY(y);
    }

    public abstract void addX(T x);

    public abstract void addY(T y);

    public void addPosition(T x, T y) {
        this.addX(x);
        this.addY(y);
    }

    public double distance(Vector2<?> position) {
        return Math.sqrt(Math.pow(position.getX().doubleValue(), 2) + Math.pow(position.getY().doubleValue(), 2));
    }
}
