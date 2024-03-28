package com.gamecodeschool.snakegame;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Typeface;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import java.io.IOException;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.BitmapFactory;

interface IDraw {
    void draw();

}

class SnakeGame extends GameManaging implements IDraw{
    // Objects for the game loop/thread
    private Thread mThread = null;
    // Control pausing between updates
    private long mNextFrameTime;
    // Is the game currently playing and or paused?
    private volatile boolean mPlaying = false;
    private volatile boolean mPaused = true;

    // for playing sound effects
    private SoundPool mSP;
    private int mEat_ID = -1;
    private int mCrashID = -1;

    // The size in segments of the playable area
    private final int NUM_BLOCKS_WIDE = 40;
    private int mNumBlocksHigh;

    // How many points does the player have
    private int mScore;

    // Objects for drawing
    private Canvas mCanvas;
    private SurfaceHolder mSurfaceHolder;
    private Paint mPaint;

    // A snake ssss
    private Snake mSnake;
    // And an apple
    private Apple mApple;

    private Bitmap mBitmapBackground;
    private Rect pauseButtonRect;
    private boolean isGamePaused = false;

    private Bitmap pauseButtonBitmap;


    // This is the constructor method that gets called
    // from SnakeActivity
    public SnakeGame(Context context, Point size) {
        super(context);

        // Work out how many pixels each block is
        int blockSize = size.x / NUM_BLOCKS_WIDE;
        // How many blocks of the same size will fit into the height
        mNumBlocksHigh = size.y / blockSize;
        // managing sounds
        managingSound();

        int pauseButtonSize = 100; // The size (width and height) of the pause button
        int pauseButtonMargin = 20; // The margin from the bottom and left edges

        // Adjusting the pauseButtonRect in the SnakeGame constructor
        pauseButtonRect = new Rect(pauseButtonMargin,
                size.y - pauseButtonSize - pauseButtonMargin, // Adjust for bottom
                pauseButtonSize + pauseButtonMargin,
                size.y - pauseButtonMargin); // Adjust for bottom

        try {
            AssetManager assetManager = context.getAssets();
            AssetFileDescriptor descriptor;

            // Prepare the sounds in memory
            descriptor = assetManager.openFd("get_apple.ogg");
            mEat_ID = mSP.load(descriptor, 0);

            descriptor = assetManager.openFd("snake_death.ogg");
            mCrashID = mSP.load(descriptor, 0);

        } catch (IOException e) {
            // Error
        }

        // Initialize the drawing objects
        mSurfaceHolder = getHolder();
        mPaint = new Paint();

        // Call the constructors of our two game objects
        mApple = new Apple(context,
                new Point(NUM_BLOCKS_WIDE,
                        mNumBlocksHigh),
                blockSize);

        mSnake = new Snake(context,
                new Point(NUM_BLOCKS_WIDE,
                        mNumBlocksHigh),
                blockSize);

    }

    public int getmScore() {
        return mScore;
    }

    public void setmScore(int mScore) {
        this.mScore = mScore;
    }

    public void managingSound() {
        final boolean isSDKINT = Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;
        // Initialize the SoundPool
        if (isSDKINT) {
            AudioAttributes audioAttributes = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build();

            mSP = new SoundPool.Builder()
                    .setMaxStreams(5)
                    .setAudioAttributes(audioAttributes)
                    .build();
        } else {
            mSP = new SoundPool(5, AudioManager.STREAM_MUSIC, 0);
        }
    }


    // Called to start a new game
    public void newGame() {

        // reset the snake
        mSnake.reset(NUM_BLOCKS_WIDE, mNumBlocksHigh);

        // Get the apple ready for dinner
        mApple.spawn();

        // Reset the mScore
        mScore = 0;

        // Setup mNextFrameTime so an update can triggered
        mNextFrameTime = System.currentTimeMillis();

        resume();
    }



    // Handles the game loop
    @Override
    public void run() {
        while (isRunning) {
            if(!isPaused) {
                // Update 10 times a second
                if (updateRequired()) {
                    update();
                }
            }

            draw();
        }
    }


    // Check to see if it is time for an update
    public boolean updateRequired() {

        // Run at 10 frames per second
        final long TARGET_FPS = 10;
        // There are 1000 milliseconds in a second
        final long MILLIS_PER_SECOND = 1000;

        final boolean isNextFrameTime = mNextFrameTime <= System.currentTimeMillis();
        // Are we due to update the frame
        if(isNextFrameTime){
            // Tenth of a second has passed

            // Setup when the next update will be triggered
            mNextFrameTime =System.currentTimeMillis()
                    + MILLIS_PER_SECOND / TARGET_FPS;

            // Return true so that the update and draw
            // methods are executed
            return true;
        }

        return false;
    }


    // Update all the game objects
    public void update() {

        // Move the snake
        mSnake.move();

        // Did the head of the snake eat the apple?
        if(mSnake.checkDinner(mApple.getLocation())){
            // This reminds me of Edge of Tomorrow.
            // One day the apple will be ready!
            mApple.spawn();

            // Add to  mScore
            mScore = mScore + 1;

            // Play a sound
            mSP.play(mEat_ID, 1, 1, 0, 0, 1);
        }

        // Did the snake die?
        if (mSnake.detectDeath()) {
            // Pause the game ready to start again
            mSP.play(mCrashID, 1, 1, 0, 0, 1);

            isPaused =true;
        }

    }


    // Do all the drawing
    @Override
    public void draw() {
        // Get a lock on the mCanvas
        if (mSurfaceHolder.getSurface().isValid()) {

            mCanvas = mSurfaceHolder.lockCanvas();

            //setting background
            settingBackground();

            // Set the size and color of the mPaint for the text
            settingColorandSize(Color.argb(255, 255, 255, 255), 120);

            // Draw the score
            mCanvas.drawText("" + mScore, 20, 120, mPaint);

            // Draw the apple and the snake
            drawingappleandsnake();



            // Draw some text while paused
            mpaused();

            printingNames();

            // Define the width and spacing of the pause bars
            int barWidth = pauseButtonRect.width() / 4;
            int barSpacing = barWidth / 2;

            // Set the paint for drawing the bars
            mPaint.setColor(Color.BLACK); // White color for the bars
            mPaint.setStyle(Paint.Style.FILL); // Solid fill

            //Draw the first bar
            mCanvas.drawRect(pauseButtonRect.left + barSpacing,
                    pauseButtonRect.top + barSpacing,
                    pauseButtonRect.left + barSpacing + barWidth,
                    pauseButtonRect.bottom - barSpacing, mPaint);

            // Draw the second bar
            mCanvas.drawRect(pauseButtonRect.right - barSpacing - barWidth,
                    pauseButtonRect.top + barSpacing,
                    pauseButtonRect.right - barSpacing,
                    pauseButtonRect.bottom - barSpacing, mPaint);

            // Unlock the mCanvas and reveal the graphics for this frame
            mSurfaceHolder.unlockCanvasAndPost(mCanvas);
        }
    }
    public void printingNames() {
        mPaint.setTextSize(90);
        drawingText("Nancy Zhu", 1850, 100);
        drawingText("Jaime Montanez", 1700, 190);
    }
    public void settingBackground() {
        mBitmapBackground = BitmapFactory.decodeResource(getContext().getResources(), R.drawable.background);
        mBitmapBackground = Bitmap.createScaledBitmap(mBitmapBackground, 2300, 1080, true);
        mCanvas.drawBitmap(mBitmapBackground,
                0, 0 , mPaint);
    }
    public void drawingappleandsnake() {
        mApple.draw(mCanvas, mPaint);
        mSnake.draw(mCanvas, mPaint);
    }
    public void mpaused() {
        if(isPaused){
            // Set the size and color of the mPaint for the text
            settingColorandSize(Color.argb(255, 255, 255, 255), 250);

            mPaint.setTypeface(Typeface.create("cursive", Typeface.NORMAL));
            // Draw the message
            // We will give this an international upgrade soon
            drawingText("Tap To Play!", 200, 700);
        }
    }
    public void settingColorandSize(int color, int size) {
        mPaint.setColor(color);
        mPaint.setTextSize(size);
    }
    public void drawingText(String text, int x, int y) {
        mCanvas.drawText(text, x, y, mPaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int x = (int) event.getX();
        int y = (int) event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                // Check if the pause area was touched
                if (pauseButtonRect.contains(x, y)) {
                    // Toggle the pause state
                    isPaused = !isPaused;
                    if (isPaused) {
                        // Pause the game logic
                        pause();
                    } else {
                        // Resume the game logic
                        resume();
                    }
                    return true; // This touch event is handled
                } else if (isPaused) {
                    newGame();
                    isPaused = false; // Ensure the game is no longer marked as paused
                    return true; // This touch event is handled
                } else {
                    // If the game is not paused, let the Snake class handle the input for changing direction
                    mSnake.switchHeading(event);
                }
                break;
        }
        return super.onTouchEvent(event);
    }


    // Stop the thread
    public void pause() {
        isRunning = false;
        try {
            mThread.join();
        } catch (InterruptedException e) {
            // Error
        }
    }


    public void resume() {
        isRunning = true;
        mThread = new Thread(this);
        mThread.start();

    }
}