package dev.heliosclient.util;

public class TickTimer {
    private int ticks = 0;
    private boolean hasTimerStarted = false;

    public TickTimer(int defaultTicks) {
        ticks = defaultTicks;
    }

    public TickTimer() {
    }

    public TickTimer(boolean start) {
        if(start)
           startTicking();
    }

    public void startTicking() {
        if (hasTimerStarted) return;
        ticks = 0;
        hasTimerStarted = true;
    }

    public void setTicks(int ticks) {
        this.ticks = ticks;
    }

    /**
     * @return elapsed ticks since the last reset  / start
     */
    public double getElapsedTicks() {
        return ticks;
    }

    // Resets the timer
    public void resetTimer() {
        ticks = 0;
        hasTimerStarted = false;
    }

    // Restarts the timer
    public void restartTimer() {
        resetTimer();
        startTicking();
    }

    public void increment(){
        if(!hasTimerStarted){
            throw new IllegalStateException("Timer has not started yet");
        }
        ticks++;
    }

    // Checks if ticks have passed since the last reset
    public boolean every(int ticks) {
        boolean didTimerLapse = getElapsedTicks() >= ticks;
        if (didTimerLapse) {
            restartTimer();
        }
        return didTimerLapse;
    }


    public boolean incrementAndEvery(int ticks) {
        increment();
        return every(ticks);
    }

    public boolean incrementAndEvery(int ticks, Runnable task) {
        increment();
        return every(ticks,task);
    }

    public boolean every(int ticks, Runnable task) {
        boolean didTimerLapse = getElapsedTicks() >= ticks;

        if (didTimerLapse) {
            task.run();
            restartTimer();
        }
        return didTimerLapse;
    }
}
