package dev.heliosclient.util;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class TimerUtils {
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private long startTime;
    private boolean hasTimerStarted;

    // Starts the timer
    public void startTimer() {
        if(hasTimerStarted) return;
        startTime = System.currentTimeMillis();
        hasTimerStarted = true;
    }

    // Returns the elapsed time (in seconds) since the timer was started
    public double getElapsedTime() {
        long endTime = System.currentTimeMillis();
        return (endTime - startTime) / 1000.0;
    }

    // Resets the timer
    public void resetTimer() {
        startTime = System.currentTimeMillis();
        hasTimerStarted = false;
    }

    // Schedules a task to reset the timer every ms milliseconds
    public void scheduleReset(long ms) {
        final Runnable resetter = this::resetTimer;
        scheduler.scheduleAtFixedRate(resetter, ms, ms, TimeUnit.MILLISECONDS);
    }

    // Schedules a task to reset the timer once after ms milliseconds
    public void scheduleSingleReset(long ms) {
        final Runnable resetter = this::resetTimer;
        scheduler.schedule(resetter, ms, TimeUnit.MILLISECONDS);
    }

    // Checks if ms milliseconds have passed since the last reset
    public boolean every(long ms) {
        if (getElapsedTime() >= ms / 1000.0)
            resetTimer();
        return getElapsedTime() >= ms / 1000.0;
    }

}
