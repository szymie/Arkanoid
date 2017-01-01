package pl.poznan.put.pg.arkanoid;

import android.graphics.RectF;

import java.util.Random;

public class Ball {

    private RectF rect;
    private Vector initialSpeed;
    private Vector currentSpeed;

    private int width;
    private int height;

    public Ball(int width, int height, int xSpeed, int ySpeed) {

        initialSpeed = new Vector(xSpeed, ySpeed);
        currentSpeed = new Vector(xSpeed, ySpeed);

        this.width = width;
        this.height = height;

        rect = new RectF();
    }

    public void update(long fps) {
        rect.left = rect.left + (currentSpeed.x / fps);
        rect.top = rect.top + (currentSpeed.y / fps);
        rect.right = rect.left + width;
        rect.bottom = rect.top - height;
    }

    public void reverseYVelocity() {
        currentSpeed.y = -currentSpeed.y;
    }

    public void reverseXVelocity() {
        currentSpeed.x = -currentSpeed.x;
    }

    public void setRandomXVelocity() {

        Random generator = new Random();

        if(generator.nextBoolean()) {
            reverseXVelocity();
        }
    }

    public void clearObstacleY(float y) {
        rect.bottom = y;
        rect.top = y - height;
    }

    public void clearObstacleX(float x) {
        rect.left = x;
        rect.right = x + width;
    }

    public void resetPosition(int x, int y) {
        rect.left = x;
        rect.top = y;
        rect.right = rect.left + width;
        rect.bottom = rect.top + height;
    }

    public void resetSpeed() {
        currentSpeed.x = initialSpeed.x;
        currentSpeed.y = initialSpeed.y;
    }

    public void increaseSpeed(int speed) {
        currentSpeed.x += speed;
        currentSpeed.y += speed;
    }

    public int getCurrentSpeedVectorLength() {
        return currentSpeed.length();
    }

    public void setCurrentSpeed(Vector currentSpeed) {
        this.currentSpeed = currentSpeed;
    }

    public RectF getRect() {
        return rect;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }
}
