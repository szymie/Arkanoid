package pl.poznan.put.pg.arkanoid;

public class Vector {

    public float x;
    public float y;

    public Vector(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public Vector(float length) {
        x = length;
        y = 0;
    }

    public int length() {
        return (int) Math.sqrt(x * x + y * y);
    }

    public Vector rotate(double angle) {
        float x1 = (float)(x * Math.cos(angle) - y * Math.sin(angle));
        float y1 = (float)(x * Math.sin(angle) + y * Math.cos(angle));
        return new Vector(x1, y1);
    }

    @Override
    public String toString() {
        return "[" + x + ";" + y + "]";
    }
}
