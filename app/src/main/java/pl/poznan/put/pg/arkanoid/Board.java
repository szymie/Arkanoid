package pl.poznan.put.pg.arkanoid;

import android.graphics.RectF;

public class Board {

    public static final int STOP = 0;
    public static final int LEFT = 1;
    public static final int RIGHT = 2;

    private RectF rect;
    private int width;
    private int height;
    private float x;
    private float y;
    private float speed;
    private int movementState = STOP;
    private int screenWidth;

    public Board(int boardWidth, int boardHeight, int screenWidth, int screenHeight, int speed) {

        width = boardWidth;
        height = boardHeight;

        x = screenWidth / 2;
        y = screenHeight - height;

        rect = new RectF(x, y, x + width, y + height);

        this.screenWidth = screenWidth;
        this.speed = speed;
    }

    public RectF getRect() {
        return rect;
    }

    public void setMovementState(int movementState) {
        this.movementState = movementState;
    }

    public void update(long fps) {

        if(movementState == LEFT) {
            x = Math.max(x - speed / fps, 0);
        } else if(movementState == RIGHT) {
            x = Math.min(x + speed / fps, screenWidth - width);
        }

        rect.left = x;
        rect.right = x + width;
    }

    public void reset(int x, int y) {
        rect.left = x;
        rect.top = y;
        rect.right = rect.left + width;
        rect.bottom = rect.top + height;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }
}
