package com.gamecodeschool.snakegame;

import android.view.SurfaceView;
import android.content.Context;
public abstract class GameManaging extends SurfaceView implements Runnable{
    protected volatile boolean isRunning = false;
    protected volatile boolean isPaused = true;

    private Thread gameThread;

    public GameManaging(Context context){
        super(context);
    }

    public synchronized void resume(){
        isPaused = false;
        isRunning = true;
        gameThread = new Thread(this);
        gameThread.start();
    }

    public synchronized void pause(){
        isRunning = false;
        try {
            gameThread.join();
        } catch (InterruptedException e){
            Thread.currentThread().interrupt();
        }
    }

    protected abstract void newGame();

    @Override
    public abstract void run();
}
