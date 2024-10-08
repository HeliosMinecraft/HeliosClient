package dev.heliosclient.util.timer;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class TimerUtils {
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    public long startTime;
    private boolean hasTimerStarted = false;
    final Runnable resetter = this::resetTimer;

    public TimerUtils(boolean start) {
        if (start)
            startTimer();
    }

    public TimerUtils() {
    }

    // Starts the timer
    public void startTimer() {
        if (hasTimerStarted) return;
        startTime = System.currentTimeMillis();
        hasTimerStarted = true;
    }
    // Resets the timer
    public void resetTimer() {
        startTime = System.currentTimeMillis();
        hasTimerStarted = false;
    }
    // Restarts the timer
    public void restartTimer() {
        resetTimer();
        startTimer();
    }

    //Sets the time
    public void setTime(long time) {
        this.startTime = System.currentTimeMillis() - time;
    }

    /**
     * Returns the elapsed time (in seconds) since the timer was started
     */
    public double getElapsedTime() {
        long endTime = System.currentTimeMillis();
        return (endTime - startTime) / 1000.0;
    }

    /**
     * Returns the elapsed time (in milliseconds) since the timer was started
     */
    public double getElapsedTimeMS() {
        long endTime = System.currentTimeMillis();
        return endTime - startTime;
    }


    // Schedules a task to reset the timer every ms milliseconds
    public void scheduleReset(long ms) {
        scheduler.scheduleAtFixedRate(resetter, ms, ms, TimeUnit.MILLISECONDS);
    }

    // Schedules a task to reset the timer once after ms milliseconds
    public void scheduleSingleReset(long ms) {
        scheduler.schedule(resetter, ms, TimeUnit.MILLISECONDS);
    }

    // Checks if ms milliseconds have passed since the last reset
    public boolean every(long ms) {
        boolean didTimerLapse = getElapsedTime() >= ms / 1000.0;
        if (didTimerLapse)
            resetTimer();

        return didTimerLapse;
    }

    public boolean every(long ms, Runnable task) {
        boolean didTimerLapse = getElapsedTime() >= ms / 1000.0;
        if (didTimerLapse) {
            task.run();
            resetTimer();
        }
        return didTimerLapse;
    }
}
