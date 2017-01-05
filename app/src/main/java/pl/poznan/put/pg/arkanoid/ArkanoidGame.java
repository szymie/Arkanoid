package pl.poznan.put.pg.arkanoid;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.text.InputType;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.EditText;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.api.GoogleApiClient;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ArkanoidGame extends BaseActivity {

    private static final String TAG = "Arkanoid";
    private ArkanoidView arkanoidView;
    private OrientationData orientationData;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        arkanoidView = new ArkanoidView(this, 4, 8, 3);
        orientationData = new OrientationData(this);
        orientationData.register();
        Log.v(TAG, "onCreate");
        setContentView(arkanoidView);
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    public Action getIndexApiAction() {
        Thing object = new Thing.Builder()
                .setName("ArkanoidGame Page") // TODO: Define a title for the content shown.
                // TODO: Make sure this auto-generated URL is correct.
                .setUrl(Uri.parse("http://[ENTER-YOUR-URL-HERE]"))
                .build();
        return new Action.Builder(Action.TYPE_VIEW)
                .setObject(object)
                .setActionStatus(Action.STATUS_TYPE_COMPLETED)
                .build();
    }

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        AppIndex.AppIndexApi.start(client, getIndexApiAction());
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        AppIndex.AppIndexApi.end(client, getIndexApiAction());
        client.disconnect();
    }

    private class ArkanoidView extends SurfaceView implements Runnable {

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

        private int backgroundColor = ContextCompat.getColor(ArkanoidGame.this, R.color.backgroundColor);
        private int brushColor =  ContextCompat.getColor(ArkanoidGame.this, R.color.brushColor);
        private int brickColor = ContextCompat.getColor(ArkanoidGame.this, R.color.brickColor);

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

        public ArkanoidView(Context context, int numberOfRows, int numberOfColumns, int lives) {

            super(context);

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

            Display display = getWindowManager().getDefaultDisplay();
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

                float boardSpeed = -pitch * screenWidth / 1000f;

                float delta = Math.abs(boardSpeed * elapsedTime) > 3 ? boardSpeed * elapsedTime : 0;
                board.update(delta);
            }

            ball.update(fps);

            handleBricksCollision();
            handleBoardCollision();
            handleBottomCollision();
            handleOtherSidesCollision();

            if (currentBricksNumber == 0) {
                paused = true;
                ArkanoidGame.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        handleGameEnd(points, currentLivesNumber);
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
                    ball.increaseSpeed(10);

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
                    ArkanoidGame.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            handleGameEnd(points, currentLivesNumber);
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

    private void handleGameEnd(final int points, final int lives) {

        AlertDialog.Builder builder = new AlertDialog.Builder(ArkanoidGame.this);
        builder.setTitle("Enter your nick");

        final EditText input = new EditText(ArkanoidGame.this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String nick = input.getText().toString();

                Result result = new Result(nick, points, lives);

                List<Result> results = readResults();

                if(results == null) {
                    results = new ArrayList<>();
                }

                results.add(result);

                Collections.sort(results);

                writeResults(results);

                switchToStartScreenActivity();
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switchToStartScreenActivity();
            }
        });

        builder.show();
    }

    private void switchToStartScreenActivity() {
        Intent intent = new Intent(ArkanoidGame.this, StartScreen.class);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onPause() {
        super.onPause();
        arkanoidView.pause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        arkanoidView.resume();
    }
}
