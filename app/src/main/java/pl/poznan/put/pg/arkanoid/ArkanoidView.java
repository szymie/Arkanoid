package pl.poznan.put.pg.arkanoid;


import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.support.v4.content.ContextCompat;
import android.view.Display;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class ArkanoidView extends SurfaceView implements Runnable {

    private ArkanoidGame arkanoidGame;
    private OrientationData orientationData;
    private Thread thread;
    private SurfaceHolder holder;
    private volatile boolean playing;
    private boolean paused = true;
    private Canvas canvas;
    private Paint paint;
    private long fps;
    private long timeThisFrame;
    private long startFrameTime;
    private int screenWidth;
    private int screenHeight;

    private int backgroundColor;
    private int brushColor;
    private int brickColor;

    private Board board;
    private Ball ball;
    private Brick[] bricks;
    private int initialBricksNumber;
    private int currentBricksNumber;
    private int numberOfRows;
    private int numberOfColumns;

    private int points;
    private int initialLivesNumber;
    private int currentLivesNumber;
    private long printedFps = 0;

    private long lastPointTimestamp = 0;
    private long streak = 0;

    public ArkanoidView(ArkanoidGame arkanoidGame, OrientationData orientationData, int numberOfRows, int numberOfColumns, int lives) {

        super(arkanoidGame);

        this.arkanoidGame = arkanoidGame;
        this.orientationData = orientationData;

        backgroundColor = ContextCompat.getColor(arkanoidGame, R.color.backgroundColor);
        brushColor =  ContextCompat.getColor(arkanoidGame, R.color.brushColor);
        brickColor = ContextCompat.getColor(arkanoidGame, R.color.brickColor);

        holder = getHolder();
        paint = new Paint();

        initScreenDimensions();

        board = new Board(200, 400, screenWidth, screenHeight/*, 400*/);
        ball = new Ball(15, 15, 200, -400);
        this.numberOfRows = numberOfRows;
        this.numberOfColumns = numberOfColumns;
        initialBricksNumber = numberOfRows * numberOfColumns;
        bricks = new Brick[initialBricksNumber];

        initialLivesNumber = lives;

        currentLivesNumber = initialLivesNumber;
        points = 0;

        createBricksAndRestart();
    }

    private void createBricksAndRestart() {

        int brickWidth = screenWidth / numberOfColumns;
        int brickHeight = screenHeight / (numberOfColumns + 8); //+8

        currentBricksNumber = 0;

        for (int row = 0; row < numberOfRows; row++) {
            for (int column = 0; column < numberOfColumns; column++) {
                bricks[currentBricksNumber++] = new Brick(row, column, brickWidth, brickHeight, 2);
            }
        }

        ball.resetPosition(screenWidth / 2, screenHeight - board.getHeight() - ball.getHeight());
        ball.resetSpeed();
        board.reset(screenWidth / 2 - board.getWidth() / 2, screenHeight - 20);
    }

    private void initScreenDimensions() {

        Display display = arkanoidGame.getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);

        screenWidth = size.x;
        screenHeight = size.y;
    }

    @Override
    public void run() {

        while (playing) {

            long elapsedTime = System.currentTimeMillis() - startFrameTime;
            startFrameTime = System.currentTimeMillis();

            if (!paused) {
                update(elapsedTime);
            }

            draw();

            timeThisFrame = System.currentTimeMillis() - startFrameTime;

            if (timeThisFrame >= 1) {
                fps = 1000 / timeThisFrame;
            }
        }
    }

    public void update(long elapsedTime) {

        if(orientationData.getOrientation() != null && orientationData.getStartOrientation() != null) {

            float pitch = orientationData.getOrientation()[1] - orientationData.getStartOrientation()[1];

            float boardSpeed = -pitch * screenWidth / 800f;

            float delta = Math.abs(boardSpeed * elapsedTime) > 1 ? boardSpeed * elapsedTime : 0;
            board.update(delta);
        }

        ball.update(fps);

        handleBricksCollision();
        handleBoardCollision();
        handleBottomCollision();
        handleOtherSidesCollision();

        if (currentBricksNumber == 0) {
            paused = true;
            arkanoidGame.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    arkanoidGame.handleGameEnd(points, currentLivesNumber);
                }
            });
        }
    }

    private void handleBricksCollision() {

        for (int i = 0; i < initialBricksNumber; i++) {
            if (isBrickColliding(bricks[i])) {

                long currentTimestamp = System.currentTimeMillis();

                if(currentTimestamp - lastPointTimestamp < 500) {
                    streak++;
                } else {
                    streak = 0;
                }

                bricks[i].setInvisible();

                ball.reverseYVelocity();
                ball.increaseSpeed(15);

                points += 1 + streak;
                currentBricksNumber--;

                lastPointTimestamp = System.currentTimeMillis();
            }
        }
    }

    private void handleBoardCollision() {

        if (RectF.intersects(board.getRect(), ball.getRect())) {

            float boardMiddle = board.getRect().left + board.getWidth() / 2;
            float ballMiddle = ball.getRect().left + ball.getWidth() / 2;

            float margin = 0.1f;
            float delta = Math.abs(Math.abs(boardMiddle - ballMiddle) / (board.getWidth() / 2) - margin);

            if(ballMiddle < boardMiddle) {
                delta = -delta;
            }

            double angle = Math.acos(delta);
            Vector bounceVector = new Vector(ball.getCurrentSpeedVectorLength());

            ball.setCurrentSpeed(bounceVector.rotate(angle));
            ball.reverseYVelocity();

            ball.clearObstacleY(board.getRect().top - 2);
        }
    }

    private void handleBottomCollision() {

        if (ball.getRect().bottom > screenHeight && !RectF.intersects(board.getRect(), ball.getRect())) {

            ball.reverseYVelocity();
            ball.clearObstacleY(screenHeight - 4);

            currentLivesNumber--;

            if (currentLivesNumber == 0) {
                paused = true;
                arkanoidGame.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        arkanoidGame.handleGameEnd(points, currentLivesNumber);
                    }
                });
            }
        }
    }

    private void handleOtherSidesCollision() {

        if (ball.getRect().top < 0) {
            ball.reverseYVelocity();
            ball.clearObstacleY(ball.getHeight() + 2);
        }

        if (ball.getRect().left < 0) {
            ball.reverseXVelocity();
            ball.clearObstacleX(2);
        }

        if (ball.getRect().right > screenWidth - ball.getWidth()) {
            ball.reverseXVelocity();
            ball.clearObstacleX(screenWidth - (ball.getWidth() * 2 + 2));
        }
    }

    private boolean isBrickColliding(Brick brick) {
        return brick.isVisible() && RectF.intersects(brick.getRect(), ball.getRect());
    }

    public void draw() {

        if (holder.getSurface().isValid()) {

            canvas = holder.lockCanvas();

            canvas.drawColor(backgroundColor);

            paint.setColor(brushColor);

            canvas.drawRect(board.getRect(), paint);
            canvas.drawCircle(ball.getRect().centerX(), ball.getRect().centerY(), ball.getWidth() / 2, paint);

            drawBricks();

            drawHud();

            holder.unlockCanvasAndPost(canvas);
        }
    }

    private void drawBricks() {

        paint.setColor(brickColor);

        for (int i = 0; i < initialBricksNumber; i++) {
            if (bricks[i].isVisible()) {
                canvas.drawRect(bricks[i].getRect(), paint);
            }
        }
    }

    private void drawHud() {

        paint.setColor(brushColor);
        paint.setTextSize(40);
        updatePrintedFps(fps);
        canvas.drawText(String.format("Points: %d Lives: %d FPS: %d", points, currentLivesNumber, printedFps), 10, 40, paint);

        if (currentBricksNumber == 0) {
            paint.setTextSize(90);
            drawText(canvas, paint, "Victory!");
        }

        if (currentLivesNumber <= 0) {
            paint.setTextSize(90);
            drawText(canvas, paint, "Defeat!");
        }
    }

    private void updatePrintedFps(long fps) {
        if(Math.abs(printedFps - fps) > 5) {
            printedFps = fps;
        }
    }

    public void drawText(Canvas canvas, Paint paint, String text) {

        Rect bounds = new Rect();
        paint.getTextBounds(text, 0, text.length(), bounds);

        int x = (canvas.getWidth() / 2) - (bounds.width() / 2);
        int y = (canvas.getHeight() / 2) - (bounds.height() / 2);

        canvas.drawText(text, x, y, paint);
    }

    public void pause() {

        try {
            playing = false;
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void resume() {
        playing = true;
        thread = new Thread(this);
        thread.start();
    }

    @Override
    public boolean onTouchEvent(MotionEvent motionEvent) {

        switch (motionEvent.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:

                if (paused) {
                    paused = false;
                    currentLivesNumber = initialLivesNumber;
                    points = 0;
                    createBricksAndRestart();
                    orientationData.newGame();
                }

                    /*if (motionEvent.getX() > screenWidth / 2) {
                        board.setMovementState(Board.RIGHT);
                    } else {
                        board.setMovementState(Board.LEFT);
                    }*/

                break;
            case MotionEvent.ACTION_UP:
                //board.setMovementState(Board.STOP);
                break;
        }

        return true;
    }
}
